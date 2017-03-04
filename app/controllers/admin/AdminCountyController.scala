package controllers.admin

import javax.inject.{Inject, Named}

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.{Action, AnyContent, Controller, RequestHeader}
import play.api.data.Form
import play.api.data.Forms._
import org.springframework.beans.factory.annotation.Autowired
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.CountyService
import models.location.County
import customUtils.authorization.WithRole
import enums.RoleEnums
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import constants.FlashMsgConstants
import java.util.UUID

import models.UserCredential
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.CountyForm

class AdminCountyController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                       val countyService: CountyService,
                                       val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {
/*
  @Autowired
  private var countyService: CountyService = _
*/

  // Edit - Listing
  def listAll: Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val list: Option[List[County]] = countyService.getListOfAll
    Ok(views.html.admin.county.list(list))
  }

  // Edit - Add
  val countyForm = Form(
    mapping(
      "id" -> optional(text),
      "name" -> nonEmptyText(minLength = 1, maxLength = 255),
      "order" -> optional(number)
    )(CountyForm.apply)(CountyForm.unapply)
  )

  def editIndex(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.county.index())
  }

  def add(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.county.add(countyForm))
  }

  def addSubmit(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    countyForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.county.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
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
            val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error.wrongid")
            BadRequest(views.html.admin.county.add(countyForm)).flashing(FlashMsgConstants.Error -> errorMessage)
          case Some(county) =>
            val saved = countyService.add(county)
            val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", saved.name, saved.objectId.toString)
            Redirect(controllers.admin.routes.AdminCountyController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
        }
        result
      }
    )

  }



  // Edit - Edit content
  def edit(objectId: UUID): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    countyService.findById(objectId) match {
      case None =>
        Ok(views.html.admin.county.index())
      case Some(county) =>
        val form = CountyForm.apply(
          Some(county.objectId.toString),
          county.name,
          Some(county.order)
        )

        Ok(views.html.admin.county.add(countyForm.fill(form)))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val result: Boolean = countyService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("admin.success") + " - " + Messages("admin.delete.success", objectId.toString)
        Redirect(controllers.admin.routes.AdminCountyController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.delete.error")
        Redirect(controllers.admin.routes.AdminCountyController.listAll()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}