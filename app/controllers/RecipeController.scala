package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.{MultipartFormData, Action, Controller}
import securesocial.core.SecureSocial
import models.Recipe
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.viewmodels.RecipeForm
import play.api.i18n.Messages
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import services.{UserCredentialService, ContentFileService, RecipeService}
import play.api.libs.Files.TemporaryFile
import enums.FileTypeEnums
import java.util.UUID

@SpringController
class RecipeController extends Controller with SecureSocial {


  @Autowired
  private var recipeService: RecipeService = _

  @Autowired
  private var fileService: ContentFileService = _

  def index = Action {
    Ok(views.html.recipe.recipe())
  }

  // Edit - Listing
  def listRecipes = SecuredAction { implicit request =>
    val listOfPage: List[Recipe] = recipeService.getListOfAll
    Ok(views.html.edit.listRecipes(listOfPage))
  }

  // Edit - Add Content
  val contentForm = Form(
    mapping(
      "receipeid" -> optional(number),
      "recipename" -> nonEmptyText(minLength = 1, maxLength = 255),
      "recipebody" -> optional(text)
    )(RecipeForm.apply _)(RecipeForm.unapply _)
  )

  def indexRecipe() = SecuredAction { implicit request =>
    Ok(views.html.edit.indexRecipes())
  }

  def addRecipe() = SecuredAction { implicit request =>
    Ok(views.html.edit.addRecipe(contentForm))
  }

  def addRecipeSubmit() = SecuredAction(parse.multipartFormData) { implicit request =>

    contentForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.content.add.error")
        BadRequest(views.html.edit.addRecipe(contentForm)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {
        var newRec = new Recipe(contentData.name)

        request.body.file("recipemainimage").map {
          file =>
            val filePerm: MultipartFormData.FilePart[TemporaryFile] = file
            val imageFile = fileService.uploadFile(filePerm, UserCredentialService.socialUser2UserCredential(request.user), FileTypeEnums.IMAGE)
              imageFile match {
                case Some(imageFile) =>
                  newRec.mainImage = imageFile
                case None => None
            }
        }

        contentData.mainBody match {
          case Some(content) => newRec.mainBody = content
          case None =>
        }
        val savedContentPage = recipeService.add(newRec)
        val successMessage = Messages("edit.success") + " - " + Messages("edit.recipe.add.success", savedContentPage.name, savedContentPage.objectId.toString)
        Redirect(controllers.routes.RecipeController.indexRecipe()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }


  // Edit - Edit content
  def edit(objectId: UUID) = SecuredAction { implicit request =>
    Ok(views.html.edit.indexContentPages())
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction { implicit request =>
    val result: Boolean = recipeService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("edit.success") + " - " + Messages("edit.recipe.delete.success", objectId.toString)
        Redirect(controllers.routes.RecipeController.indexRecipe()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.recipe.delete.error")
        Redirect(controllers.routes.RecipeController.indexRecipe()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}