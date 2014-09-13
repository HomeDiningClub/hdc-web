package controllers

import models.files.ContentFile
import org.springframework.stereotype.{Controller => SpringController}
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
import presets.ImagePreSets
import utils.authorization.{WithRoleAndOwnerOfObject, WithRole}
import scala.Some
import models.viewmodels.{RecipeBox, RecipeForm}
import utils.Helpers
import play.api.Logger
import scala.collection.JavaConverters._

@SpringController
class RecipePageController extends Controller with SecureSocial {

  @Autowired
  private var recipeService: RecipeService = _

  @Autowired
  private var userProfileService: UserProfileService = _

  @Autowired
  private var fileService: ContentFileService = _


  def viewRecipeByNameAndProfile(profileName: String, recipeName: String) = UserAwareAction { implicit request =>

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
    recipeService.findByrecipeLinkName(recipeName, fetchAll = false) match {
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
      "recipebody" -> optional(text)
    )(RecipeForm.apply)(RecipeForm.unapply)
  )



  def add() = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>
    Ok(views.html.recipe.addOrEdit(recForm))
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
          Some(item.objectId.toString),
          item.getName,
          item.getPreAmble match{case null|"" => None case _ => Some(item.getPreAmble)},
          Some(item.getMainBody)
        )

        // Get any images and sort them
        val sortedImages = recipeService.getSortedRecipeImages(item)

        Ok(views.html.recipe.addOrEdit(recForm.fill(form),editingRecipe, sortedImages))
    }
  }


  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    val currentUser: Option[UserCredential] = Helpers.getUserFromRequest

    if(currentUser.nonEmpty)
      Unauthorized("Not authorized to perform this function")

    recForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("recipe.add.error")
        BadRequest(views.html.recipe.addOrEdit(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
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
          BadRequest(views.html.recipe.addOrEdit(recForm.fill(contentData))).flashing(FlashMsgConstants.Error -> errorMessage)
        }

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