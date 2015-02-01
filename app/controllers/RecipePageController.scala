package controllers

import models.files.ContentFile
import models.jsonmodels.RecipeBoxJSON
import org.springframework.stereotype.{Controller => SpringController}
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._
import securesocial.core.SecureSocial
import models.{UserCredential, Recipe}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import services.{UserProfileService, ContentFileService, RecipeService}
import play.api.libs.Files.TemporaryFile
import enums.{ContentStateEnums, RoleEnums, FileTypeEnums}
import java.util.UUID
import utils.authorization.{WithRoleAndOwnerOfObject, WithRole}
import scala.Some
import models.viewmodels.{EditRecipeExtraValues, RecipeBox, RecipeForm}
import utils.Helpers
import play.api.Logger
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

@SpringController
class RecipePageController extends Controller with SecureSocial {

  @Autowired
  private var recipeService: RecipeService = _

  @Autowired
  private var userProfileService: UserProfileService = _

  @Autowired
  private var fileService: ContentFileService = _


  def viewRecipeByNameAndProfile(profileName: String, recipeName: String) = UserAwareAction { implicit request =>

    println("###############################")
    println("profileName = " + profileName)
    println("recipeName = " + recipeName)
    println("###############################")

    // Try getting the recipe from name, if failure show 404
    recipeService.findByownerProfileProfileLinkNameAndRecipeLinkName(profileName,recipeName) match {
      case Some(recipe) =>
             Ok(views.html.recipe.recipe(recipe, recipeBoxes = recipeService.getRecipeBoxes(recipe.getOwnerProfile.getOwner), isThisMyRecipe = isThisMyRecipe(recipe)))
          case None =>
            val errMess = "Cannot find recipe using name:" + recipeName + " and profileName:" + profileName
            Logger.debug(errMess)
            BadRequest(errMess)
        }
  }


  def viewRecipeByNameAndProfilePageJSON(profileName: String, page: Int) = UserAwareAction { implicit request =>

    var list: ListBuffer[RecipeBoxJSON] = new ListBuffer[RecipeBoxJSON]
    var t: Option[List[RecipeBox]] = None


    println("profileLinkName(IN) : " + profileName)


    userProfileService.findByprofileLinkName(profileName) match {
      case Some(profile) => {
        try {
          t = recipeService.getRecipeBoxesPage(profile.getOwner, page)
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
          for (e: RecipeBox <- t) {
            //val link: String = controllers.routes.RecipePageController.viewRecipeByNameAndProfile(profileName, e.linkToRecipe).url
            list += RecipeBoxJSON(e.objectId.toString, e.linkToRecipe, e.name, e.preAmble.getOrElse(""), e.mainImage.getOrElse(""), e.recipeRating.toString, e.recipeBoxCount, e.hasNext, e.hasPrevious, e.totalPages)
          }
        }
        case None => {}
      }

     // ...

    } catch {
      case ex: Exception =>
        Logger.error("Could not create JSON of list of Recipe boxes: " + ex.getMessage)
    }

    Ok(convertRecipesToJson(list))
  }


  def convertRecipesToJson(jsonCase: Seq[RecipeBoxJSON]): JsValue = Json.toJson(jsonCase)

  def viewRecipeByNameAndProfilePage(profileName: String, recipeName: String, page: Int) = UserAwareAction { implicit request =>

    // Try getting the recipe from name, if failure show 404
    recipeService.findByownerProfileProfileLinkNameAndRecipeLinkName(profileName,recipeName) match {
      case Some(recipe) =>
        Ok(views.html.recipe.recipe(recipe, recipeBoxes = recipeService.getRecipeBoxes(recipe.getOwnerProfile.getOwner), isThisMyRecipe = isThisMyRecipe(recipe)))
      case None =>
        val errMess = "Cannot find recipe using name:" + recipeName + " and profileName:" + profileName
        Logger.debug(errMess)
        BadRequest(errMess)
    }
  }


  def viewRecipeByName(recipeName: String) = UserAwareAction { implicit request =>

    // Try getting the recipe from name, if failure show 404
    recipeService.findByrecipeLinkName(recipeName) match {
      case Some(recipe) =>
        Redirect(controllers.routes.RecipePageController.viewRecipeByNameAndProfile(recipe.getOwnerProfile.profileLinkName,recipe.getLink))
      case None =>
        val errMess = "Cannot find recipe using name:" + recipeName
        Logger.debug(errMess)
        BadRequest(errMess)
    }
  }

  private def isThisMyRecipe(recipe: Recipe)(implicit request: RequestHeader): Boolean = {
    utils.Helpers.getUserFromRequest match {
      case None =>
        false
      case Some(user) =>
        if(recipe.getOwnerProfile.getOwner.objectId == user.objectId)
          true
        else
          false
    }
  }


  // Edit - Add Content
  val recForm = Form(
    mapping(
      "receipeid" -> optional(text),
      "recipename" -> nonEmptyText(minLength = 1, maxLength = 255),
      "recipepreamble" -> optional(text(maxLength = 255)),
      "recipebody" -> optional(text),
      "recipemainimage" -> optional(text),
      "recipeimages" -> optional(text)
    )(RecipeForm.apply)(RecipeForm.unapply)
  )

  private def setExtraValues(recipe: Option[Recipe] = None): EditRecipeExtraValues = {

    if(recipe.isDefined){
      // Other values not fit to be in form-classes
      val mainImage = recipe.get.getMainImage match {
        case null => None
        case image => Some(image.getStoreId)
      }
      val recipeImages = recipe.get.getRecipeImages.asScala.toList match {
        case null | Nil => Nil
        case images => images.map { image =>
          image.getStoreId
        }
      }

      EditRecipeExtraValues(
        mainImage match {
          case Some(item) => Some(List(routes.ImageController.imgChooserThumb(item).url))
          case None => None
        },
        recipeImages match {
          case Nil => None
          case items => Some(items.map{ item => routes.ImageController.imgChooserThumb(item).url})
        },
        recipe.get.getMaxNrOfMainImages,
        recipe.get.getMaxNrOfRecipeImages
      )
    }else{

      // Not brilliant, consider moving config
      var tempRec = new Recipe("temporary")
      val maxMainImage = tempRec.getMaxNrOfMainImages
      val maxImages = tempRec.getMaxNrOfRecipeImages
      tempRec = null

      EditRecipeExtraValues(None,None,maxMainImage,maxImages)
    }
  }


  def add() = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>
    Ok(views.html.recipe.addOrEdit(recipeForm = recForm, extraValues = setExtraValues(None)))
  }

  def edit(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request =>
    val editingRecipe = recipeService.findById(objectId)

    editingRecipe match {
      case None =>
        val errorMsg = "Wrong ID, cannot edit, Page cannot be found."
        Logger.debug(errorMsg)
        NotFound(errorMsg)
      case Some(item) =>
        item.isEditableBy(Helpers.getUserFromRequest.get.objectId)
        val form = RecipeForm.apply(
          id = Some(item.objectId.toString),
          name = item.getName,
          preAmble = item.getPreAmble match{case null|"" => None case _ => Some(item.getPreAmble)},
          mainBody = Some(item.getMainBody),
          mainImage = item.getMainImage match {
            case null => None
            case item => Some(item.objectId.toString)
          },
          images = recipeService.convertToCommaSepStringOfObjectIds(recipeService.getSortedRecipeImages(item))
        )

        // Get any images and sort them
        //val sortedImages = recipeService.getSortedRecipeImages(item)

        Ok(views.html.recipe.addOrEdit(recipeForm = recForm.fill(form), editingRecipe = editingRecipe, extraValues = setExtraValues(editingRecipe)))
    }
  }


  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    val currentUser: Option[UserCredential] = Helpers.getUserFromRequest

    if(currentUser.nonEmpty)
      Unauthorized("Not authorized to perform this function")

    recForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("recipe.add.error")
        BadRequest(views.html.recipe.addOrEdit(errors,extraValues = setExtraValues(None))).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val newRec: Option[Recipe] = contentData.id match {
          case Some(id) =>
            recipeService.findById(UUID.fromString(id)) match {
              case None => None
              case Some(item) =>
                item.isEditableBy(currentUser.get.objectId).asInstanceOf[Boolean] match {
                  case true =>
                    item.setName(contentData.name)
                    Some(item)
                  case false =>
                    None
                }
            }
          case None =>
            Some(new Recipe(contentData.name))
        }

        if (newRec.isEmpty) {
          Logger.debug("Error saving Recipe: User used a non-existing, or someone elses Recipe")
          val errorMessage = Messages("recipe.add.error")
          BadRequest(views.html.recipe.addOrEdit(recForm.fill(contentData), extraValues = setExtraValues(None))).flashing(FlashMsgConstants.Error -> errorMessage)
        }

        // Recipe main image
        contentData.mainImage match {
          case Some(imageId) => UUID.fromString(imageId) match {
            case imageUUID: UUID =>
              fileService.getFileByObjectIdAndOwnerId(imageUUID, currentUser.get.objectId) match {
                case Some(item) => newRec.get.setAndRemoveMainImage(item)
                case _  => None
              }
          }
          case None =>
            newRec.get.deleteMainImage()
            None
        }

        // Recipe images
        var hasDeletedImages = false
        contentData.images match {
          case None =>
            newRec.get.deleteRecipeImages()
            hasDeletedImages = true
          case Some(imageStr) =>
            // This is just a comma sep list of object id's, split, validate UUID, and verify each entry
            imageStr.split(",").take(newRec.get.getMaxNrOfRecipeImages).foreach { imageId =>
              UUID.fromString(imageId) match {
                case imageUUID: UUID =>
                  fileService.getFileByObjectIdAndOwnerId(imageUUID, currentUser.get.objectId) match {
                    case Some(item) =>
                      // Found at least one valid image, clean the current list, but only one time
                      if (!hasDeletedImages) {
                        newRec.get.deleteRecipeImages()
                        hasDeletedImages = true
                      }
                      // Add it
                      newRec.get.addRecipeImage(item)
                    case _ =>
                      None
                  }
              }
            }
        }

        /*var i = 1
        while(i <= 6) {
          request.body.file("recipeimage" + i).map {
            file =>
              fileService.uploadFile(file, currentUser.get.objectId, FileTypeEnums.IMAGE, ImagePreSets.recipeImages) match {
                case Some(item) =>
                  // This code is ugly as hell, but replaces an earlier image
                  // Remodel to JSON-delete etc in the future
                  if(sortedImages.isDefined && sortedImages.get.isDefinedAt(i)){
                    newRec.get.deleteRecipeImage(sortedImages.get(i))
                  }
                  newRec.get.addRecipeImage(item)
                case None =>
                  None
              }
          }
          i = i + 1
        }*/

        /*
        request.body.file("recipemainimage").map {
          file =>
            fileService.uploadFile(file, currentUser.get.objectId, FileTypeEnums.IMAGE, ImagePreSets.recipeImages) match {
              case Some(item) => newRec.get.setAndRemoveMainImage(item)
              case None => None
            }
        }

        // Get a sorted list to compare with replacing images
        val sortedImages = recipeService.getSortedRecipeImages(newRec.get)
        var i = 1
        while(i < 6) {
          request.body.file("recipeimage" + i).map {
            file =>
                fileService.uploadFile(file, currentUser.get.objectId, FileTypeEnums.IMAGE, ImagePreSets.recipeImages) match {
                case Some(item) =>
                  // This code is ugly as hell, but replaces an earlier image
                  // Remodel to JSON-delete etc in the future
                  if(sortedImages.isDefined && sortedImages.get.isDefinedAt(i)){
                    newRec.get.deleteRecipeImage(sortedImages.get(i))
                  }
                  newRec.get.addRecipeImage(item)
                case None =>
                  None
              }
          }
          i = i + 1
        }
      */


        newRec.get.setMainBody(contentData.mainBody.getOrElse(""))
        newRec.get.setPreAmble(contentData.preAmble.getOrElse(""))
        newRec.get.contentState = ContentStateEnums.PUBLISHED.toString

        val savedRecipe = recipeService.add(newRec.get)
        val savedProfile = userProfileService.addRecipeToProfile(currentUser.get, savedRecipe)
        val successMessage = Messages("recipe.add.success", savedRecipe.getName)
        Redirect(controllers.routes.RecipePageController.viewRecipeByNameAndProfile(currentUser.get.profiles.iterator.next.profileLinkName,savedRecipe.getLink)).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }

  // Delete
  def delete(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request =>
    val recipe: Option[Recipe] = recipeService.findById(objectId)

    if(recipe.isEmpty){
        val errorMessage = Messages("recipe.delete.error")
        Redirect(controllers.routes.UserProfileController.viewProfileByLoggedInUser()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

    val recipeLinkName = recipe.get.getLink
    val recipeOwnerProfileName =  recipe.get.getOwnerProfile.profileLinkName
    val result: Boolean = recipeService.deleteById(recipe.get.objectId)

    result match {
      case true =>
        val successMessage = Messages("recipe.delete.success")
        Redirect(controllers.routes.UserProfileController.viewProfileByLoggedInUser()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("recipe.delete.error")
        Redirect(controllers.routes.RecipePageController.viewRecipeByNameAndProfile(recipeOwnerProfileName,recipeLinkName)).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}