package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc._
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
import utils.authorization.WithRole
import enums.RoleEnums
import utils.authorization.WithRole
import scala.Some
import models.viewmodels.AddUserToRoleForm
import models.viewmodels.UserRoleForm

@SpringController
class UserRoleController extends Controller with SecureSocial {

  @Autowired
  private var userRoleService: UserRoleService = _
  @Autowired
  private var userCredentialService: UserCredentialService = _


  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val list: Option[List[UserRole]] = userRoleService.getListOfAll
    Ok(views.html.edit.roles.list(list))
  }

  // Edit - Add
  val userRoleForm = Form(
    mapping(
      "id" -> optional(text),
      "name" -> nonEmptyText(minLength = 1, maxLength = 255)
    )(UserRoleForm.apply _)(UserRoleForm.unapply _)
  )

  def index() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.edit.roles.index())
  }

  def add() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.edit.roles.add(userRoleForm))
  }

  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>

    userRoleForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
        BadRequest(views.html.edit.roles.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val saved = contentData.id match {
          case Some(id) =>
            val item = userRoleService.findById(UUID.fromString(id))
            item.name = contentData.name
            userRoleService.save(item)
          case None =>
            userRoleService.createRole(contentData.name)
        }

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

  def addUserToRole() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.edit.roles.addUserToRole(userAddToRoleForm,getUsersAsDropDown,getRolesAsDropDown))
  }

  def addUserToRoleSubmit() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>

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
  def edit(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val item = userRoleService.findById(objectId)

    item match {
      case null =>
        Ok(views.html.edit.roles.index())
      case _ =>
        val form = UserRoleForm.apply(
          Some(item.objectId.toString),
          item.name
        )

        Ok(views.html.edit.roles.add(userRoleForm.fill(form)))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
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