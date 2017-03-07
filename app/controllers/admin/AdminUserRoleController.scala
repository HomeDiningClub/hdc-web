package controllers.admin

import javax.inject.{Named, Inject}

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc._

import play.api.data.Form
import play.api.data.Forms._
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import models.{UserCredential, UserRole}
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.{UserCredentialService, UserRoleService}
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import constants.FlashMsgConstants
import scala.collection.mutable
import customUtils.authorization.WithRole
import enums.RoleEnums
import customUtils.authorization.WithRole
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.{UserRoleForm, AddUserToRoleForm}
import models.formdata.UserRoleForm

class AdminUserRoleController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                         val userRoleService: UserRoleService,
                                         val userCredentialService: UserCredentialService,
                                         val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  /*
  @Autowired
  private var userRoleService: UserRoleService = _

  @Autowired
  private var userCredentialService: UserCredentialService = _
*/

  // Edit - Listing
  def listAll: Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val list: Option[List[UserRole]] = userRoleService.getListOfAll
    Ok(views.html.admin.roles.list(list))
  }

  // Edit - Add
  val userRoleForm = Form(
    mapping(
      "id" -> optional(text),
      "name" -> nonEmptyText(minLength = 1, maxLength = 255)
    )(UserRoleForm.apply)(UserRoleForm.unapply)
  )

  def editIndex(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.roles.index())
  }

  def add(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.roles.add(userRoleForm))
  }

  def addSubmit(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    userRoleForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.roles.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
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

        val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", saved.name, saved.objectId.toString)
        Redirect(controllers.admin.routes.AdminUserRoleController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }

  val userAddToRoleForm = Form(
    mapping(
      "userObjectId" -> nonEmptyText,
      "roleObjectId" -> nonEmptyText,
      "addOrRemoveRole" -> boolean
    )(AddUserToRoleForm.apply)(AddUserToRoleForm.unapply)
  )

  def addUserToRole(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.roles.addUserToRole(userAddToRoleForm,getUsersAsDropDown,getRolesAsDropDown))
  }

  def addUserToRoleSubmit(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    userAddToRoleForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.roles.addUserToRole(errors,getUsersAsDropDown,getRolesAsDropDown)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {
        val saved = contentData.addOrRemoveRole match {
          case true => userRoleService.addRoleToUser(UUID.fromString(contentData.roleObjectId), UUID.fromString(contentData.userObjectId))
          case false => userRoleService.removeRoleFromUser(UUID.fromString(contentData.roleObjectId), UUID.fromString(contentData.userObjectId))
        }
        val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", saved.fullName, saved.objectId.toString)
        Redirect(controllers.admin.routes.AdminUserRoleController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
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
        Some(bufferList)
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
        items.sortBy(tw => tw.fullName).toBuffer.map {
          item: UserCredential =>
            bufferList += ((item.objectId.toString, item.fullName + " - (" + item.emailAddress + " , " + item.providerId + ")"))
        }
        Some(bufferList)
      case None =>
        None
    }
    returnItems
  }



  // Edit - Edit content
  def edit(objectId: UUID): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val item = userRoleService.findById(objectId)

    item match {
      case null =>
        Ok(views.html.admin.roles.index())
      case _ =>
        val form = UserRoleForm.apply(
          Some(item.objectId.toString),
          item.name
        )

        Ok(views.html.admin.roles.add(userRoleForm.fill(form)))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val result: Boolean = userRoleService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("admin.success") + " - " + Messages("admin.delete.success", objectId.toString)
        Redirect(controllers.admin.routes.AdminUserRoleController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.delete.error")
        Redirect(controllers.admin.routes.AdminUserRoleController.editIndex()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}