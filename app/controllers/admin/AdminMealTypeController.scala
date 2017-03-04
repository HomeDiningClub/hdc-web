package controllers.admin

import javax.inject.{Named, Inject}

import org.springframework.stereotype.{Controller => SpringController}

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import org.springframework.beans.factory.annotation.Autowired
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.MealTypeService
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import constants.FlashMsgConstants
import java.util.UUID
import customUtils.authorization.WithRole
import enums.RoleEnums
import models.UserCredential
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.EventPropertyForm

class AdminMealTypeController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                         val mealTypeService: MealTypeService,
                                         val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  // Edit - Listing
  def listAll: Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.event.mealtype.list(mealTypeService.listAll()))
  }

  // Edit - Add
  val mealForm = Form(
    mapping(
      "id" -> optional(text),
      "name" -> nonEmptyText(minLength = 1, maxLength = 255),
      "order" -> number(min = 0, max = 999, strict = false)
    )(EventPropertyForm.apply)(EventPropertyForm.unapply)
  )

  def editIndex(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.event.mealtype.index())
  }

  def add(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.event.mealtype.add(mealForm.fill(EventPropertyForm.apply(None,"",0))))
  }

  def addSubmit(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    mealForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.event.mealtype.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val saved = contentData.id match {
          case Some(id) =>
            val item = mealTypeService.findById(UUID.fromString(id)) match {
              case None => mealTypeService.create(contentData.name,contentData.order)
              case Some(existingItem) =>
                existingItem.name = contentData.name
                existingItem.order = contentData.order
                existingItem
            }
            mealTypeService.save(item)
          case None =>
            mealTypeService.create(contentData.name,contentData.order)
        }

        val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", saved.name, saved.objectId.toString)
        Redirect(controllers.admin.routes.AdminMealTypeController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }


  // Edit - Edit content
  def edit(objectId: UUID): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    mealTypeService.findById(objectId) match {
      case None =>
        Ok(views.html.admin.event.mealtype.index())
      case Some(item) =>
        val form = EventPropertyForm.apply(
          Some(item.objectId.toString),
          item.name,
          item.order
        )
        Ok(views.html.admin.event.mealtype.add(mealForm.fill(form)))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val result: Boolean = mealTypeService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("admin.success") + " - " + Messages("admin.delete.success", objectId.toString)
        Redirect(controllers.admin.routes.AdminMealTypeController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.delete.error")
        Redirect(controllers.admin.routes.AdminMealTypeController.listAll()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}