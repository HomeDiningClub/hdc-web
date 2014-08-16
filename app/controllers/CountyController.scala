package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.{AnyContent, Request, RequestHeader, Controller}
import securesocial.core.SecureSocial
import play.api.data.Form
import play.api.data.Forms._
import models.viewmodels.CountyForm
import play.api.i18n.Messages
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import services.CountyService

import java.util.UUID
import models.location.County
import utils.authorization.WithRole
import enums.RoleEnums

@SpringController
class CountyController extends Controller with SecureSocial {

  @Autowired
  private var countyService: CountyService = _


  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val list: Option[List[County]] = countyService.getListOfAll
    Ok(views.html.edit.county.list(list))
  }

  // Edit - Add
  val countyForm = Form(
    mapping(
      "id" -> optional(text),
      "name" -> nonEmptyText(minLength = 1, maxLength = 255),
      "order" -> optional(number)
    )(CountyForm.apply)(CountyForm.unapply)
  )

  def editIndex() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.edit.county.index())
  }

  def add() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.edit.county.add(countyForm))
  }

  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>

    countyForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
        BadRequest(views.html.edit.county.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val newRec: Option[County] = contentData.id match {
          case Some(id) =>
            val item: Option[County] = countyService.findById(UUID.fromString(id)) match {
              case None => None
              case Some(cnt) =>
                cnt.name = contentData.name
                cnt.order = contentData.order.getOrElse(0)
                Some(cnt)
            }
            item
          case None =>
            contentData.order match {
              case None =>
                Some(new County(contentData.name))
              case Some(ordering) =>
                Some(new County(contentData.name, ordering))
            }
        }

        val result = newRec match {
          case None =>
            val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error.wrongid")
            BadRequest(views.html.edit.county.add(countyForm)).flashing(FlashMsgConstants.Error -> errorMessage)
          case Some(county) =>
            val saved = countyService.add(county)
            val successMessage = Messages("edit.success") + " - " + Messages("edit.add.success", saved.name, saved.objectId.toString)
            Redirect(controllers.routes.CountyController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
        }
        result
      }
    )

  }



  // Edit - Edit content
  def edit(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    countyService.findById(objectId) match {
      case None =>
        Ok(views.html.edit.county.index())
      case Some(county) =>
        val form = CountyForm.apply(
          Some(county.objectId.toString),
          county.name,
          Some(county.order)
        )

        Ok(views.html.edit.county.add(countyForm.fill(form)))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val result: Boolean = countyService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("edit.success") + " - " + Messages("edit.delete.success", objectId.toString)
        Redirect(controllers.routes.CountyController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.delete.error")
        Redirect(controllers.routes.CountyController.editIndex()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}