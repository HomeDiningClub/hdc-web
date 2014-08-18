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
import services.{ContentFileService, RecipeService}
import play.api.libs.Files.TemporaryFile
import enums.{RoleEnums, FileTypeEnums}
import java.util.UUID
import presets.ImagePreSets
import utils.authorization.WithRole
import scala.Some
import models.viewmodels.RecipeForm

@SpringController
class AdminRecipeController extends Controller with SecureSocial {

  @Autowired
  private var recipeService: RecipeService = _

  @Autowired
  private var fileService: ContentFileService = _


  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val listOfPage: List[Recipe] = recipeService.getListOfAll
    Ok(views.html.admin.recipe.list(listOfPage))
  }

  // Edit - Add Content
  val contentForm = Form(
    mapping(
      "receipeid" -> optional(text),
      "recipename" -> nonEmptyText(minLength = 1, maxLength = 255),
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

    contentForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.recipe.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
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

        val saved = recipeService.add(newRec)
        val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", saved.name, saved.objectId.toString)
        Redirect(controllers.admin.routes.AdminRecipeController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }


  // Edit - Edit content
  def edit(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val item = recipeService.findById(objectId)

    item match {
      case null =>
        Ok(views.html.admin.recipe.index())
      case _ =>
        val form = RecipeForm.apply(
          Some(item.objectId.toString),
          item.name,
          Some(item.mainBody)
        )

        Ok(views.html.admin.recipe.add(contentForm.fill(form)))
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

}