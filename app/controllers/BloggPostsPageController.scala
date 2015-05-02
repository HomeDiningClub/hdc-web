package controllers

import models.files.ContentFile
import models.jsonmodels.{BlogPostBoxJSON, RecipeBoxJSON}
import org.springframework.stereotype.{Controller => SpringController}
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._
import securesocial.core.SecureSocial
import models.{BloggPosts, UserCredential, Recipe}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import services.{UserProfileService, ContentFileService, RecipeService, BloggPostsService}
import play.api.libs.Files.TemporaryFile
import enums.{ContentStateEnums, RoleEnums, FileTypeEnums}
import java.util.UUID
import utils.authorization.{WithRoleAndOwnerOfObject, WithRole}
import scala.Some
import models.viewmodels._
import utils.Helpers
import play.api.Logger
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer


@SpringController
class BloggPostsPageController extends Controller with SecureSocial {


  @Autowired
  private var bloggPostsService: BloggPostsService = _

  @Autowired
  private var userProfileService: UserProfileService = _

  @Autowired
  private var fileService: ContentFileService = _


  val recForm = Form(
    mapping(
      "blogpostid" -> optional(text),
      "title" -> optional(text),
      "maintext" -> optional(text),
      "mainimage" -> optional(text)
    )(BlogPostsForm.apply)(BlogPostsForm.unapply)
  )


  // Change to parameter
  def viewBloggPosts() = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>
    Ok(views.html.blogg.bloggBox("freddy"))
  }


  def add() = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>
    Ok(views.html.blogg.addOrEdit(bloggPostForm = recForm, extraValues = setExtraValues(None)))
  }

  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>


    print("addSubmit ... 1... ")

    val currentUser: Option[UserCredential] = Helpers.getUserFromRequest

    print("addSubmit ... 2... ")

    if (currentUser.nonEmpty)
      Unauthorized("Not authorized to perform this function")

    print("addSubmit ... 3... ")

    recForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("recipe.add.error")
        BadRequest(views.html.blogg.addOrEdit(errors, extraValues = setExtraValues(None))).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        print("addSubmit ... 4... ")

        val newRec: Option[BloggPosts] = contentData.id match {
          case Some(id) =>
            bloggPostsService.findById(UUID.fromString(id)) match {
              case None => None
              case Some(item) =>


                item.isEditableBy(currentUser.get.objectId).asInstanceOf[Boolean] match {
                  case true =>
                    item.setTitle(contentData.title.get)
                    Some(item)
                  case false =>
                    None
                }
            }
          case None =>
            print("addSubmit ... 5 ... No in data ....")
            Some(new BloggPosts())
        }

        print("addSubmit ... 5 ... ")

        if (newRec.isEmpty) {

          newRec.get.setTitle("test title")
          newRec.get.setText(contentData.maintext.get)

          print("addSubmit ... 6 ... ")

          Logger.debug("Error saving Recipe: User used a non-existing, or someone elses Recipe")
          val errorMessage = Messages("recipe.add.error")
          BadRequest(views.html.blogg.addOrEdit(recForm.fill(contentData), extraValues = setExtraValues(None))).flashing(FlashMsgConstants.Error -> errorMessage)
        }

        print("addSubmit ... 7 ... ")

        // Recipe main image
        contentData.mainImage match {
          case Some(imageId) => UUID.fromString(imageId) match {
            case imageUUID: UUID =>
              fileService.getFileByObjectIdAndOwnerId(imageUUID, currentUser.get.objectId) match {
                case Some(item) => newRec.get.setAndRemoveMainImage(item)
                case _ => None
              }
          }
          case None =>
            //newRec.get.deleteMainImage()
            None
        }





        //newRec.get.setMainBody(contentData.mainBody.getOrElse(""))
        //newRec.get.setPreAmble(contentData.preAmble.getOrElse(""))
        newRec.get.contentState = ContentStateEnums.PUBLISHED.toString



        newRec.get.setTitle(contentData.title.get)
        newRec.get.setText(contentData.maintext.get)


        val savedBlogPost = bloggPostsService.add(newRec.get)
        val savedProfile = userProfileService.addBloggPostsToProfile(currentUser.get, savedBlogPost)
        var bloggPostObjectId = savedBlogPost.objectId



        // Redirect(controllers.routes.RecipePageController.viewRecipeByNameAndProfile(currentUser.get.profiles.iterator.next.profileLinkName,savedRecipe.getLink)).flashing(FlashMsgConstants.Success -> successMessage)
        //todo
        //Ok("todo")
        Redirect(controllers.routes.BloggPostsPageController.edit(bloggPostObjectId))
      }
    )

  }

  private def setExtraValues(bloggPosts: Option[BloggPosts] = None): EditBloggPostsExtraValues = {

    if(bloggPosts.isDefined){
      // Other values not fit to be in form-classes
      val mainImage = bloggPosts.get.getMainImage match {
        case null => None
        case image => Some(image.getStoreId)
      }



      EditBloggPostsExtraValues(
        mainImage match {
          case Some(item) => Some(List(routes.ImageController.imgChooserThumb(item).url))
          case None => None
        }, 1)
    }else{

      // Not brilliant, consider moving config
      //@todo new constructor new BloggPosts("temporary")
      var tempRec = new BloggPosts()
      val maxMainImage = tempRec.getMaxNrOfMainImages

      tempRec = null

      EditBloggPostsExtraValues(None, 1)
    }
  }


  def viewBloggPostByNameAndProfile(profileName: String, bloggPostId: String) = UserAwareAction { implicit request =>

    println("###############################")
    println("profileName = " + profileName)
    println("bloggPostId = " + bloggPostId)
    println("###############################")



    // Try getting the recipe from name, if failure show 404

    bloggPostsService.findById(UUID.fromString(bloggPostId)) match {
    //recipeService.findByownerProfileProfileLinkNameAndRecipeLinkName(profileName,recipeName) match {
      case Some(recipe) =>
        // Ok(views.html.recipe.recipe(recipe, metaData = buildMetaData(recipe, request), recipeBoxes = recipeService.getRecipeBoxes(recipe.getOwnerProfile.getOwner), isThisMyRecipe = isThisMyRecipe(recipe)))
        Ok("test")
      case None =>
        val errMess = "Cannot find recipe using name:" + "recipeName" + " and profileName:" + profileName
        Logger.debug(errMess)
        BadRequest(errMess)
    }
  }


  def viewListOfBloggPosts(profileName: String, page: Int) = UserAwareAction { implicit request =>

    var t: Option[List[BloggPostBox]] = None
    var antal : Int = 0
    print("1 ok")

    userProfileService.findByprofileLinkName(profileName) match {
      case Some(profile) => {
        try {
          t = bloggPostsService.getBlogPostsBoxesPage(profile.getOwner, page)

        } catch {
          case  ex: Exception =>
            Logger.error("Could not get list of Recipe boxes: " + ex.getMessage)
        }

      }
      case None => {}

    }

    print("2 ok, antal = " + t.size )




    try {
      // loop ....
      t match {
        case Some(t) => {
          for (e: BloggPostBox <- t) {
            print("\nObjectId" + e.bloggPostObjectId.toString)
            print("\nTitle : " + e.title)
            print("\ntext : " + e.text)
            antal = antal + 1
            //val link: String = controllers.routes.RecipePageController.viewRecipeByNameAndProfile(profileName, e.linkToRecipe).url

          }
        }
        case None => {}
      }

      // ...

    } catch {
      case ex: Exception =>
        Logger.error("Could not create JSON of list of Recipe boxes: " + ex.getMessage)
    }

    print("3 ok")



    Ok(profileName + ", antal=" + antal)
  }


  def viewBloggPostByNameAndProfilePageJSON(profileName: String, page: Int) = UserAwareAction { implicit request =>

    var list: ListBuffer[BlogPostBoxJSON] = new ListBuffer[BlogPostBoxJSON]
    var t: Option[List[BloggPostBox]] = None


    println("profileLinkName(IN) : " + profileName)


    userProfileService.findByprofileLinkName(profileName) match {
      case Some(profile) => {
        try {

          t = bloggPostsService.getBlogPostsBoxesPage(profile.getOwner, page)
        } catch {
          case  ex: Exception =>
            Logger.error("Could not get list of Recipe boxes: " + ex.getMessage)
        }

      }
      case None => {}

    }


    try {
      // loop ....
      t match {
        case Some(t) => {
          for (e: BloggPostBox <- t) {
            //val link: String = controllers.routes.RecipePageController.viewRecipeByNameAndProfile(profileName, e.linkToRecipe).url
            list += BlogPostBoxJSON(e.bloggPostObjectId.toString, e.title, e.text, e.mainImage.getOrElse(""), e.hasNext, e.hasPrevious, e.totalPages) // ? antal sidor
          }
        }
        case None => {}
      }

      // ...

    } catch {
      case ex: Exception =>
        Logger.error("Could not create JSON of list of Recipe boxes: " + ex.getMessage)
    }

    Ok(convertBlogPostToJson(list))
  }

  def convertBlogPostToJson(jsonCase: Seq[BlogPostBoxJSON]): JsValue = Json.toJson(jsonCase)


  // edit

  def edit(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request =>
    val editingRecipe = bloggPostsService.findById(objectId)

    editingRecipe match {
      case None =>
        val errorMsg = "Wrong ID, cannot edit, Page cannot be found."
        Logger.debug(errorMsg)
        print("error ..... ")
        NotFound(errorMsg)
      case Some(item) =>
        item.isEditableBy(Helpers.getUserFromRequest.get.objectId)
        item.setTitle(item.getTitle)
        print("svar objectId : " + item.objectId)
        val form = BlogPostsForm.apply(
        id = Some(item.objectId.toString),
        title = Some(item.getTitle),
        maintext = Some(item.getText),
        mainImage = Some(""))


        // Get any images and sort them
        //val sortedImages = recipeService.getSortedRecipeImages(item)

        Ok(views.html.blogg.addOrEdit(bloggPostForm = recForm.fill(form), editingBloggPosts = editingRecipe, extraValues = setExtraValues(editingRecipe)))
    }
  }


  def view(objectId: UUID) = UserAwareAction { implicit request =>
    val editingRecipe = bloggPostsService.findById(objectId)

    editingRecipe match {
      case None =>
        val errorMsg = "Wrong ID, cannot edit, Page cannot be found."
        Logger.debug(errorMsg)
        print("error ..... ")
        NotFound(errorMsg)
      case Some(item) =>

        item.setTitle(item.getTitle)
        print("svar objectId : " + item.objectId)
        val form = BlogPostsForm.apply(
          id = Some(item.objectId.toString),
          title = Some(item.getTitle),
          maintext = Some(item.getText),
          mainImage = Some(""))


        // Get any images and sort them
        //val sortedImages = recipeService.getSortedRecipeImages(item)

        Ok(views.html.blogg.view(bloggPostForm = recForm.fill(form), editingBloggPosts = editingRecipe, extraValues = setExtraValues(editingRecipe)))
    }
  }

}

