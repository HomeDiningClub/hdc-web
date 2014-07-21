package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import securesocial.core.SecureSocial
import play.api.data.Form
import play.api.data.Forms._
import models.viewmodels.TagWordForm
import org.springframework.beans.factory.annotation.Autowired
import services.TagWordService
import play.api.i18n.Messages
import constants.FlashMsgConstants
import models.profile.TagWord
import java.util.UUID

@SpringController
class TagWordController extends Controller with SecureSocial {

  @Autowired
  private var tagwordService: TagWordService = _


  // Edit - Listing
  def list = SecuredAction { implicit request =>
    val list: Option[List[TagWord]] = tagwordService.listAll()
    Ok(views.html.edit.tagword.list(list))
  }

  // Edit - Add
  val tagwordForm = Form(
    mapping(
      "tagwordname" -> nonEmptyText(minLength = 1, maxLength = 255),
      "tagwordgroupname" -> nonEmptyText(minLength = 1, maxLength = 255)
    )(TagWordForm.apply _)(TagWordForm.unapply _)
  )

  def index() = SecuredAction { implicit request =>
    Ok(views.html.edit.tagword.index())
  }

  def add() = SecuredAction { implicit request =>
    Ok(views.html.edit.tagword.add(tagwordForm))
  }

  def addSubmit() = SecuredAction(parse.multipartFormData) { implicit request =>

    tagwordForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
        BadRequest(views.html.edit.tagword.add(tagwordForm)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {
        val saved = tagwordService.createTag(contentData.tagwordName,"","",contentData.tagwordGroupName.toLowerCase)
        val successMessage = Messages("edit.success") + " - " + Messages("edit.add.success", saved.tagName, saved.objectId.toString)
        Redirect(controllers.routes.TagWordController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }



  // Edit - Edit content
  def edit(objectId: UUID) = SecuredAction { implicit request =>
    Ok(views.html.edit.tagword.index())
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction { implicit request =>
    val result: Boolean = tagwordService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("edit.success") + " - " + Messages("edit.delete.success", objectId.toString)
        Redirect(controllers.routes.TagWordController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.delete.error")
        Redirect(controllers.routes.TagWordController.index()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}