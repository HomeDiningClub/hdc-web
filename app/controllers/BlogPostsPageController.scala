package controllers

import models.files.ContentFile
import models.jsonmodels.{BlogPostBoxJSON}
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._
import models.{BlogPost, UserCredential}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import constants.FlashMsgConstants
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.{RequestWithUser, SecuredRequest}
import services.{NodeEntityService, UserProfileService, ContentFileService, BlogPostsService}
import enums.{ContentStateEnums, RoleEnums, FileTypeEnums}
import java.util.UUID
import customUtils.authorization.{WithRoleAndOwnerOfObject, WithRole}
import models.viewmodels._
import customUtils.Helpers
import play.api.Logger
import scala.collection.JavaConverters._
import javax.inject.{Inject}
import scala.collection.mutable.ListBuffer
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.BlogPostsForm

class BlogPostsPageController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                         val blogPostsService: BlogPostsService,
                                         val userProfileService: UserProfileService,
                                         val fileService: ContentFileService,
                                         implicit val nodeEntityService: NodeEntityService,
                                         val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  /*
  @Autowired
  private var blogPostsService: BlogPostsService = _

  @Autowired
  private var userProfileService: UserProfileService = _

  @Autowired
  private var fileService: ContentFileService = _
*/

  val recForm = Form(
    mapping(
      "blogpostid" -> optional(text),
      "title" -> optional(text),
      "maintext" -> optional(text),
      "mainimage" -> optional(text)
    )(BlogPostsForm.apply)(BlogPostsForm.unapply)
  )


  // Change to parameter
  def viewBlogPosts() = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>
    Ok(views.html.blog.blogBox("freddy"))
  }


  def add() = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>
    Ok(views.html.blog.addOrEdit(blogPostForm = recForm, extraValues = setExtraValues(None)))
  }

  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    val currentUser: UserCredential = request.user

    // Only HOSTS are allowed to create blog items
    if(!currentUser.profiles.iterator().next().isUserHost)
      Unauthorized("Not authorized to perform this function")


    recForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("blog.add.error")
        BadRequest(views.html.blog.addOrEdit(errors, extraValues = setExtraValues(None))).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        print("addSubmit ... 4... ")

        val newRec: Option[BlogPost] = contentData.id match {
          case Some(id) =>
            blogPostsService.findById(UUID.fromString(id)) match {
              case None => None
              case Some(item) =>


                item.isEditableBy(currentUser.objectId).asInstanceOf[Boolean] match {
                  case true =>
                    item.setTitle(contentData.title.get)
                    Some(item)
                  case false =>
                    None
                }
            }
          case None =>
            print("addSubmit ... 5 ... No in data ....")
            Some(new BlogPost())
        }

        print("addSubmit ... 5 ... ")

        if (newRec.isEmpty) {

          newRec.get.setTitle("test title")
          newRec.get.setText(contentData.maintext.get)

          print("addSubmit ... 6 ... ")

          Logger.debug("Error saving Recipe: User used a non-existing, or someone elses Recipe")
          val errorMessage = Messages("recipe.add.error")
          BadRequest(views.html.blog.addOrEdit(recForm.fill(contentData), extraValues = setExtraValues(None))).flashing(FlashMsgConstants.Error -> errorMessage)
        }

        print("addSubmit ... 7 ... ")

        // Recipe main image
        contentData.mainImage match {
          case Some(imageId) => UUID.fromString(imageId) match {
            case imageUUID: UUID =>
              fileService.getFileByObjectIdAndOwnerId(imageUUID, currentUser.objectId) match {
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


        val savedBlogPost = blogPostsService.add(newRec.get)
        val savedProfile = userProfileService.addBlogPostsToProfile(currentUser, savedBlogPost)
        val blogPostObjectId = savedBlogPost.objectId
        val successMessage = Messages("blog.add.success", savedBlogPost.getTitle)
        Redirect(controllers.routes.BlogPostsPageController.view(blogPostObjectId)).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }

  private def setExtraValues(blogPosts: Option[BlogPost] = None): EditBlogPostsExtraValues = {

    if(blogPosts.isDefined){
      // Other values not fit to be in form-classes
      val mainImage = blogPosts.get.getMainImage match {
        case null => None
        case image => Some(image.getStoreId)
      }



      EditBlogPostsExtraValues(
        mainImage match {
          case Some(item) => Some(List(routes.ImageController.imgChooserThumb(item).url))
          case None => None
        }, 1)
    }else{

      // Not brilliant, consider moving config
      //@todo new constructor new BlogPosts("temporary")
      var tempRec = new BlogPost()
      val maxMainImage = tempRec.getMaxNrOfMainImages

      tempRec = null

      EditBlogPostsExtraValues(None, 1)
    }
  }


  def viewBlogPostByNameAndProfile(profileName: String, blogPostId: String) = UserAwareAction { implicit request =>

    println("###############################")
    println("profileName = " + profileName)
    println("blogPostId = " + blogPostId)
    println("###############################")



    // Try getting the recipe from name, if failure show 404

    blogPostsService.findById(UUID.fromString(blogPostId)) match {
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


  def viewListOfBlogPosts(profileName: String, page: Int) = UserAwareAction { implicit request =>

    var t: Option[List[BlogPostItem]] = None
    var antal : Int = 0
    print("1 ok")

    userProfileService.findByprofileLinkName(profileName) match {
      case Some(profile) => {
        try {
          t = blogPostsService.getBlogPostsBoxesPage(profile.getOwner, page)

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
          for (e: BlogPostItem <- t) {
            print("\nObjectId" + e.blogPostObjectId.toString)
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


  def viewBlogPostByNameAndProfilePageJSON(profileName: String, page: Int) = UserAwareAction { implicit request =>

    var list: ListBuffer[BlogPostBoxJSON] = new ListBuffer[BlogPostBoxJSON]
    var t: Option[List[BlogPostItem]] = None


    println("profileLinkName(IN) : " + profileName)


    userProfileService.findByprofileLinkName(profileName) match {
      case Some(profile) => {
        try {

          t = blogPostsService.getBlogPostsBoxesPage(profile.getOwner, page)
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
          for (e: BlogPostItem <- t) {
            //val link: String = controllers.routes.RecipePageController.viewRecipeByNameAndProfile(profileName, e.linkToRecipe).url

            var cd : String = Helpers.formatDateForDisplay(e.dateCreated)
            var md : String = Helpers.formatDateForDisplay(e.dateChanged)

            println("Mod date : " + md)

            list += BlogPostBoxJSON(e.blogPostObjectId.toString, e.title, e.text, cd, md, e.mainImage.getOrElse(""), e.hasNext, e.hasPrevious, e.totalPages) // ? antal sidor
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

  private def buildMetaData(blogPost: BlogPost, request: RequestHeader): Option[MetaData] = {
    val domain = "//" + request.domain

    Some(MetaData(
      fbUrl = domain + request.path,
      fbTitle = blogPost.getTitle,
      fbDesc = blogPost.getText match {
        case null | "" => ""
        case item: String => customUtils.Helpers.limitLength(Helpers.removeHtmlTags(item), 125)
      },
      fbImage = blogPost.getMainImage match {
        case image: ContentFile => { domain + routes.ImageController.profileNormal(image.getStoreId).url }
        case _ => { "" }
      }
    ))
  }

  def edit(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val editingRecipe = blogPostsService.findById(objectId)

    editingRecipe match {
      case None =>
        val errorMsg = "Wrong ID, cannot edit, Page cannot be found."
        Logger.debug(errorMsg)
        NotFound(errorMsg)
      case Some(item) =>
        item.isEditableBy(request.user.objectId).asInstanceOf[Boolean] match {
          case true => {
            item.setTitle(item.getTitle)

            val form = BlogPostsForm.apply(
              id = Some(item.objectId.toString),
              title = Some(item.getTitle),
              maintext = Some(item.getText),
              mainImage = Some(""))

            Ok(views.html.blog.addOrEdit(blogPostForm = recForm.fill(form), editingBlogPosts = editingRecipe, extraValues = setExtraValues(editingRecipe)))
          }
          case false => {
            val errorMsg = "Cannot edit someone elses blog post"
            Logger.debug(errorMsg)
            NotFound(errorMsg)
          }

        }


        // Get any images and sort them
        //val sortedImages = recipeService.getSortedRecipeImages(item)


    }
  }

  private def isThisMyBlogPost(blogPost: BlogPost)(implicit request: RequestWithUser[AnyContent,UserCredential]): Boolean = {
    request.user match {
      case None =>
        false
      case Some(user) =>
        if(blogPost.getOwnerProfile.getOwner.objectId == user.objectId)
          true
        else
          false
    }
  }

  def view(objectId: UUID) = UserAwareAction { implicit request =>
    val blogPosting = blogPostsService.findById(objectId)

    blogPosting match {
      case None =>
        val errorMsg = "Wrong ID, cannot edit, Page cannot be found."
        Logger.debug(errorMsg)
        print("error ..... ")
        NotFound(errorMsg)
      case Some(item) =>
        Ok(views.html.blog.view(blogPost = item, metaData = buildMetaData(item,request), isThisMyBlogPost = isThisMyBlogPost(item)))
    }
  }


  // Delete
  def delete(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request: RequestHeader =>
    val blogPost: Option[BlogPost] = blogPostsService.findById(objectId)

    if(blogPost.isEmpty){
      val errorMessage = Messages("blog.delete.error")
      Redirect(controllers.routes.UserProfileController.viewProfileByLoggedInUser()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

    val result: Boolean = blogPostsService.deleteById(blogPost.get.objectId)

    result match {
      case true =>
        val successMessage = Messages("blog.delete.success")
        Redirect(controllers.routes.UserProfileController.viewProfileByLoggedInUser()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("blog.delete.error")
        Redirect(controllers.routes.BlogPostsPageController.view(blogPost.get.objectId)).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }


}

