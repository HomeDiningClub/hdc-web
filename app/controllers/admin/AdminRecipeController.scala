package controllers.admin

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
import utils.authorization.WithRole
import scala.Some
import models.viewmodels.RecipeForm
import utils.Helpers
import play.api.Logger

@SpringController
class AdminRecipeController extends Controller with SecureSocial {

  @Autowired
  private var recipeService: RecipeService = _

  @Autowired
  private var userProfileService: UserProfileService = _

  @Autowired
  private var fileService: ContentFileService = _


  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val listOfPage: List[Recipe] = recipeService.getListOfAll(fetchAll = true)
    Ok(views.html.admin.recipe.list(listOfPage))
  }

  // Edit - Add Content
  val contentForm = Form(
    mapping(
      "receipeid" -> optional(text),
      "recipename" -> nonEmptyText(minLength = 1, maxLength = 255),
      "recipepreamble" -> optional(text(maxLength = 255)),
      "recipebody" -> optional(text)
    )(RecipeForm.apply)(RecipeForm.unapply)
  )



  def editIndex() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.admin.recipe.index())
  }

  def add() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.admin.recipe.add(contentForm))
  }

  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN))(parse.multipartFormData) { implicit request =>

    val currentUser: Option[UserCredential] = Helpers.getUserFromRequest

    contentForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.recipe.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {


        val newRec: Option[Recipe] = contentData.id match {
          case Some(id) =>
            recipeService.findById(UUID.fromString(id)) match {
              case None => None
              case Some(item) =>
                item.setName(contentData.name)
                Some(item)
            }
          case None =>
            Some(new Recipe(contentData.name))
        }

        if(newRec.isEmpty){
            Logger.debug("Error saving Recipe: User used a non-existing Recipe objectId")
            val errorMessage = Messages("recipe.add.error")
            BadRequest(views.html.recipe.addOrEdit(contentForm.fill(contentData))).flashing(FlashMsgConstants.Error -> errorMessage)
        }

        request.body.file("recipemainimage").map {
          file =>
            val filePerm: MultipartFormData.FilePart[TemporaryFile] = file
            val imageFile = fileService.uploadFile(filePerm, request.user.asInstanceOf[UserCredential].objectId, FileTypeEnums.IMAGE, ImagePreSets.recipeImages)
            imageFile match {
              case Some(item) =>
                newRec.get.setAndRemoveMainImage(item)
              case None => None
            }
        }

        newRec.get.setPreAmble(contentData.preAmble.getOrElse(""))
        newRec.get.setMainBody(contentData.mainBody.getOrElse(""))
        newRec.get.contentState = ContentStateEnums.PUBLISHED.toString

        val saved = recipeService.add(newRec.get)
        val savedProfile = userProfileService.addRecipeToProfile(currentUser.get, saved)
        val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", saved.getName, saved.objectId.toString)
        Redirect(controllers.admin.routes.AdminRecipeController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }


  // Edit - Edit content
  def edit(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val editingRecipe = recipeService.findById(objectId)
    editingRecipe match {
      case None =>
        Ok(views.html.admin.recipe.index())
      case Some(item) =>
        val form = RecipeForm.apply(
          Some(item.objectId.toString),
          item.getName,
          item.getPreAmble match{case null|"" => None case _ => Some(item.getPreAmble)},
          Some(item.getMainBody)
        )

        Ok(views.html.admin.recipe.add(contentForm.fill(form),editingRecipe))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val result: Boolean = recipeService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("admin.success") + " - " + Messages("admin.delete.success", objectId.toString)
        Redirect(controllers.admin.routes.AdminRecipeController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.delete.error")
        Redirect(controllers.admin.routes.AdminRecipeController.editIndex()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }
  // Edit - Delete content
  def deleteAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    recipeService.deleteAll
    val successMessage = Messages("admin.success") + " - " + Messages("admin.delete-all.success")
    Redirect(controllers.admin.routes.AdminRecipeController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
  }
}