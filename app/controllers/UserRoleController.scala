package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import securesocial.core.SecureSocial
import play.api.data.Form
import play.api.data.Forms._
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import models.UserRole
import services.UserRoleService
import models.viewmodels.{AddUserToRoleForm, UserRoleForm}
import play.api.i18n.Messages
import constants.FlashMsgConstants

@SpringController
class UserRoleController extends Controller with SecureSocial {

  @Autowired
  private var userRoleService: UserRoleService = _


  // Edit - Listing
  def list = SecuredAction { implicit request =>
    val list: Option[List[UserRole]] = userRoleService.getListOfAll
    Ok(views.html.edit.roles.list(list))
  }

  // Edit - Add
  val userRoleForm = Form(
    mapping(
      "name" -> nonEmptyText(minLength = 1, maxLength = 255)
    )(UserRoleForm.apply _)(UserRoleForm.unapply _)
  )

  def index() = SecuredAction { implicit request =>
    Ok(views.html.edit.roles.index())
  }

  def add() = SecuredAction { implicit request =>
    Ok(views.html.edit.roles.add(userRoleForm))
  }

  def addSubmit() = SecuredAction(parse.multipartFormData) { implicit request =>

    userRoleForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
        BadRequest(views.html.edit.roles.add(userRoleForm)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {
        val newRec = userRoleService.createRole(contentData.name)
        val saved = userRoleService.add(newRec)
        val successMessage = Messages("edit.success") + " - " + Messages("edit.add.success", saved.name, saved.objectId.toString)
        Redirect(controllers.routes.UserRoleController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }

  val userAddToRoleForm = Form(
    mapping(
      "userObjectId" -> nonEmptyText,
      "roleObjectId" -> nonEmptyText
    )(AddUserToRoleForm.apply _)(AddUserToRoleForm.unapply _)
  )

  def addUserToRole() = SecuredAction { implicit request =>
    Ok(views.html.edit.roles.addUserToRole(userAddToRoleForm))
  }

  def addUserToRoleSubmit() = SecuredAction(parse.multipartFormData) { implicit request =>

    userAddToRoleForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
        BadRequest(views.html.edit.roles.addUserToRole(userAddToRoleForm)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {
        val saved = userRoleService.addRoleToUser(UUID.fromString(contentData.roleObjectId), UUID.fromString(contentData.userObjectId))
        val successMessage = Messages("edit.success") + " - " + Messages("edit.add.success", saved.fullName(), saved.objectId.toString)
        Redirect(controllers.routes.UserRoleController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }



  // Edit - Edit content
  def edit(objectId: UUID) = SecuredAction { implicit request =>
    Ok(views.html.edit.roles.index())
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction { implicit request =>
    val result: Boolean = userRoleService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("edit.success") + " - " + Messages("edit.delete.success", objectId.toString)
        Redirect(controllers.routes.UserRoleController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.delete.error")
        Redirect(controllers.routes.UserRoleController.index()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}