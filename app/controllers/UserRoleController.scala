package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import securesocial.core.SecureSocial
import play.api.data.Form
import play.api.data.Forms._
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import models.{UserCredential, UserRole}
import services.{UserCredentialService, UserRoleService}
import models.viewmodels.{AddUserToRoleForm, UserRoleForm}
import play.api.i18n.Messages
import constants.FlashMsgConstants
import scala.collection.mutable

@SpringController
class UserRoleController extends Controller with SecureSocial {

  @Autowired
  private var userRoleService: UserRoleService = _
  @Autowired
  private var userCredentialService: UserCredentialService = _


  // Edit - Listing
  def listAll = SecuredAction { implicit request =>
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
        BadRequest(views.html.edit.roles.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
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
      "roleObjectId" -> nonEmptyText,
      "addOrRemove" -> boolean
    )(AddUserToRoleForm.apply _)(AddUserToRoleForm.unapply _)
  )

  def addUserToRole() = SecuredAction { implicit request =>
    Ok(views.html.edit.roles.addUserToRole(userAddToRoleForm,getUsersAsDropDown,getRolesAsDropDown))
  }

  def addUserToRoleSubmit() = SecuredAction(parse.multipartFormData) { implicit request =>

    userAddToRoleForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
        BadRequest(views.html.edit.roles.addUserToRole(errors,getUsersAsDropDown,getRolesAsDropDown)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {
        val saved = contentData.addOrRemoveRole match {
          case true => userRoleService.addRoleToUser(UUID.fromString(contentData.roleObjectId), UUID.fromString(contentData.userObjectId))
          case false => userRoleService.removeRoleFromUser(UUID.fromString(contentData.roleObjectId), UUID.fromString(contentData.userObjectId))
        }
        val successMessage = Messages("edit.success") + " - " + Messages("edit.add.success", saved.fullName(), saved.objectId.toString)
        Redirect(controllers.routes.UserRoleController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }


  private def getRolesAsDropDown: Option[Seq[(String,String)]] = {
    val returnItems: Option[Seq[(String,String)]] = userRoleService.getListOfAll match {
      case Some(items) =>
        var bufferList : mutable.Buffer[(String,String)] = mutable.Buffer[(String,String)]()

        // Map and add the rest
        items.sortBy(tw => tw.name).toBuffer.map {
          item: UserRole =>
            bufferList += ((item.objectId.toString, item.name))
        }
        Some(bufferList.toSeq)
      case None =>
        None
    }
    returnItems
  }

  private def getUsersAsDropDown: Option[Seq[(String,String)]] = {
    val returnItems: Option[Seq[(String,String)]] = userCredentialService.getListOfAll match {
      case Some(items) =>
        var bufferList : mutable.Buffer[(String,String)] = mutable.Buffer[(String,String)]()

        // Map and add the rest
        items.sortBy(tw => tw.fullName()).toBuffer.map {
          item: UserCredential =>
            bufferList += ((item.objectId.toString, item.fullName() + " - (" + item.email.getOrElse("") + " , " + item.providerId + ")"))
        }
        Some(bufferList.toSeq)
      case None =>
        None
    }
    returnItems
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