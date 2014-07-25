package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import securesocial.core.SecureSocial
import play.api.data.Form
import play.api.data.Forms._
import models.viewmodels.{CountyForm, RecipeForm}
import play.api.i18n.Messages
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import services.CountyService

import java.util.UUID
import models.location.County

@SpringController
class CountyController extends Controller with SecureSocial {

  @Autowired
  private var countyService: CountyService = _


  // Edit - Listing
  def list = SecuredAction { implicit request =>
    val list: Option[List[County]] = countyService.getListOfAll
    Ok(views.html.edit.county.list(list))
  }

  // Edit - Add
  val countyForm = Form(
    mapping(
      "name" -> nonEmptyText(minLength = 1, maxLength = 255),
      "order" -> optional(number)
    )(CountyForm.apply _)(CountyForm.unapply _)
  )

  def index() = SecuredAction { implicit request =>
    Ok(views.html.edit.county.index())
  }

  def add() = SecuredAction { implicit request =>
    Ok(views.html.edit.county.add(countyForm))
  }

  def addSubmit() = SecuredAction(parse.multipartFormData) { implicit request =>

    countyForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
        BadRequest(views.html.edit.county.add(countyForm)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {
        val newRec = contentData.order match {
          case None => new County(contentData.name)
          case Some(ordering) => new County(contentData.name, ordering)
        }
        val saved = countyService.add(newRec)
        val successMessage = Messages("edit.success") + " - " + Messages("edit.add.success", saved.name, saved.objectId.toString)
        Redirect(controllers.routes.CountyController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }



  // Edit - Edit content
  def edit(objectId: UUID) = SecuredAction { implicit request =>
    Ok(views.html.edit.county.index())
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction { implicit request =>
    val result: Boolean = countyService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("edit.success") + " - " + Messages("edit.delete.success", objectId.toString)
        Redirect(controllers.routes.CountyController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.delete.error")
        Redirect(controllers.routes.CountyController.index()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}