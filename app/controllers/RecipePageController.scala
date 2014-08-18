package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc._
import securesocial.core.{SecuredRequest, SecureSocial}
import models.{UserCredential, Recipe}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.viewmodels.RecipeForm
import play.api.i18n.Messages
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import services.{UserProfileService, UserCredentialService, ContentFileService, RecipeService}
import play.api.libs.Files.TemporaryFile
import enums.{ContentStateEnums, RoleEnums, FileTypeEnums}
import java.util.UUID
import presets.ImagePreSets
import utils.authorization.{WithRoleAndOwnerOfObject, WithRole}
import scala.Some
import models.viewmodels.RecipeForm
import play.api.mvc.Security.AuthenticatedRequest
import play.api.libs.Files
import utils.Helpers

@SpringController
class RecipePageController extends Controller with SecureSocial {

  @Autowired
  private var recipeService: RecipeService = _

  @Autowired
  private var userProfileService: UserProfileService = _

  @Autowired
  private var fileService: ContentFileService = _


  def index() = UserAwareAction { implicit request =>
    Ok(views.html.recipe.recipe())
  }




  // Edit - Add Content
  val recForm = Form(
    mapping(
      "receipeid" -> optional(text),
      "recipename" -> nonEmptyText(minLength = 1, maxLength = 255),
      "recipebody" -> optional(text)
    )(RecipeForm.apply)(RecipeForm.unapply)
  )



  def add() = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>
    Ok(views.html.recipe.addOrEdit(recForm))
  }

  def edit(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request =>
    val item = recipeService.findById(objectId)

    item match {
      case null =>
        Ok(views.html.recipe.recipe())
      case _ =>
        val form = RecipeForm.apply(
          Some(item.objectId.toString),
          item.name,
          Some(item.mainBody)
        )

        Ok(views.html.recipe.addOrEdit(recForm.fill(form)))
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

        val newRec = contentData.id match {
          case Some(id) =>
            val item = recipeService.findById(UUID.fromString(id))
            item.name = contentData.name
            item
          case None =>
            new Recipe(contentData.name)
        }

        request.body.file("recipemainimage").map {
          file =>
            val filePerm: MultipartFormData.FilePart[TemporaryFile] = file
            val imageFile = fileService.uploadFile(filePerm, request.user.asInstanceOf[UserCredential].objectId, FileTypeEnums.IMAGE, ImagePreSets.recipeImages)
            imageFile match {
              case Some(item) =>
                newRec.mainImage = item
              case None => None
            }
        }

        newRec.mainBody = contentData.mainBody.getOrElse("")
        newRec.contentState = ContentStateEnums.PUBLISHED.toString

        val savedRecipe = recipeService.add(newRec)
        val savedProfile = userProfileService.addRecipeToProfile(currentUser.get, savedRecipe)
        val successMessage = Messages("recipe.add.success", savedRecipe.name)
        Redirect(controllers.routes.RecipePageController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }



  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request =>

    val result: Boolean = recipeService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("recipe.delete.success")
        Redirect(controllers.routes.RecipePageController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("recipe.delete.error")
        Redirect(controllers.routes.RecipePageController.index()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}