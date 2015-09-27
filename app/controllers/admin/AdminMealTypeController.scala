package controllers.admin

import org.springframework.stereotype.{Controller => SpringController}
import securesocial.core.SecureSocial
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.viewmodels.{MealTypeForm}
import org.springframework.beans.factory.annotation.Autowired
import services.{MealTypeService}
import play.api.i18n.Messages
import constants.FlashMsgConstants
import java.util.UUID
import utils.authorization.WithRole
import enums.RoleEnums

@SpringController
class AdminMealTypeController extends Controller with SecureSocial {

  @Autowired
  private var mealTypeService: MealTypeService = _

  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.admin.event.mealtype.list(mealTypeService.listAll()))
  }

  // Edit - Add
  val mealForm = Form(
    mapping(
      "id" -> optional(text),
      "name" -> nonEmptyText(minLength = 1, maxLength = 255),
      "order" -> number(min = 0, max = 999, strict = false)
    )(MealTypeForm.apply)(MealTypeForm.unapply)
  )

  def editIndex() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.admin.event.mealtype.index())
  }

  def add() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.admin.event.mealtype.add(mealForm.fill(MealTypeForm.apply(None,"",0))))
  }

  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>

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
  def edit(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    mealTypeService.findById(objectId) match {
      case None =>
        Ok(views.html.admin.event.mealtype.index())
      case Some(item) =>
        val form = MealTypeForm.apply(
          Some(item.objectId.toString),
          item.name,
          item.order
        )
        Ok(views.html.admin.event.mealtype.add(mealForm.fill(form)))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
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