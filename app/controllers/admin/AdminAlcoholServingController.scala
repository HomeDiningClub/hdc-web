package controllers.admin

import java.util.UUID
import javax.inject.Inject

import constants.FlashMsgConstants
import customUtils.authorization.WithRole
import customUtils.security.SecureSocialRuntimeEnvironment
import enums.RoleEnums
import models.UserCredential
import models.formdata.EventPropertyForm
import org.springframework.stereotype.{Controller => SpringController}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.{AlcoholServingService, MealTypeService}

class AdminAlcoholServingController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                         val alcoholServingService: AlcoholServingService,
                                         val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  // Edit - Listing
  def listAll: Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.event.alcoholServing.list(alcoholServingService.listAll()))
  }

  // Edit - Add
  val alcoForm = Form(
    mapping(
      "id" -> optional(text),
      "name" -> nonEmptyText(minLength = 1, maxLength = 255),
      "order" -> number(min = 0, max = 999, strict = false)
    )(EventPropertyForm.apply)(EventPropertyForm.unapply)
  )

  def editIndex(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.event.alcoholServing.index())
  }

  def add(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.event.alcoholServing.add(alcoForm.fill(EventPropertyForm.apply(None,"",0))))
  }

  def addSubmit(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    alcoForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.event.alcoholServing.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val saved = contentData.id match {
          case Some(id) =>
            val item = alcoholServingService.findById(UUID.fromString(id)) match {
              case None => alcoholServingService.create(contentData.name,contentData.order)
              case Some(existingItem) =>
                existingItem.name = contentData.name
                existingItem.order = contentData.order
                existingItem
            }
            alcoholServingService.save(item)
          case None =>
            alcoholServingService.create(contentData.name,contentData.order)
        }

        val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", saved.name, saved.objectId.toString)
        Redirect(controllers.admin.routes.AdminAlcoholServingController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }


  // Edit - Edit content
  def edit(objectId: UUID): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    alcoholServingService.findById(objectId) match {
      case None =>
        Ok(views.html.admin.event.alcoholServing.index())
      case Some(item) =>
        val form = EventPropertyForm.apply(
          Some(item.objectId.toString),
          item.name,
          item.order
        )
        Ok(views.html.admin.event.alcoholServing.add(alcoForm.fill(form)))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val result: Boolean = alcoholServingService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("admin.success") + " - " + Messages("admin.delete.success", objectId.toString)
        Redirect(controllers.admin.routes.AdminAlcoholServingController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.delete.error")
        Redirect(controllers.admin.routes.AdminAlcoholServingController.listAll()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}