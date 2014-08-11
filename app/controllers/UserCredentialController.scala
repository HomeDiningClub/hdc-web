package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import securesocial.core.SecureSocial
import play.api.data.Form
import play.api.data.Forms._
import models.viewmodels.{UserCredentialForm, CountyForm}
import play.api.i18n.Messages
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import services.{UserCredentialService, CountyService}

import java.util.UUID
import models.location.County
import utils.authorization.WithRole
import enums.RoleEnums
import models.UserCredential

@SpringController
class UserCredentialController extends Controller with SecureSocial {

  @Autowired
  private var userCredentialService: UserCredentialService = _


  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val list: Option[List[UserCredential]] = userCredentialService.getListOfAll
    Ok(views.html.edit.usercredential.list(list))
  }

  // Edit - Add
  val userCredForm = Form(
    mapping(
      "id" -> text,
      "firstname" -> nonEmptyText(minLength = 1, maxLength = 255),
      "lastname" -> nonEmptyText(minLength = 1, maxLength = 255),
      "emailaddress" -> email
    )(UserCredentialForm.apply)(UserCredentialForm.unapply)
  )

  def editIndex() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.edit.usercredential.index())
  }

//  def add() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
//    Ok(views.html.edit.usercredential.add(userCredForm))
//  }

  def addUserToRole() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Redirect(routes.UserRoleController.addUserToRole())
  }

  def editSubmit() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>

    userCredForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
        BadRequest(views.html.edit.usercredential.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val newRec = userCredentialService.findById(UUID.fromString(contentData.id))
        newRec.firstName = contentData.firstName
        newRec.lastName = contentData.lastName
        newRec.fullName = contentData.firstName + " " + contentData.lastName
        newRec.emailAddress = contentData.emailAddress

        val saved = userCredentialService.add(newRec)
        val successMessage = Messages("edit.success") + " - " + Messages("edit.add.success", saved.emailAddress, saved.objectId.toString)
        Redirect(controllers.routes.UserCredentialController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }



  // Edit
  def edit(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val item = userCredentialService.findById(objectId)

    item match {
      case null =>
        Ok(views.html.edit.usercredential.index())
      case _ =>
        val form = UserCredentialForm.apply(
          item.objectId.toString,
          item.firstName,
          item.lastName,
          item.emailAddress
        )

        Ok(views.html.edit.usercredential.add(userCredForm.fill(form)))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val result: Boolean = userCredentialService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("edit.success") + " - " + Messages("edit.delete.success", objectId.toString)
        Redirect(controllers.routes.UserCredentialController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.delete.error")
        Redirect(controllers.routes.UserCredentialController.editIndex()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}