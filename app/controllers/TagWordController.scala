package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.{Request, AnyContent, RequestHeader, Controller}
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
import utils.authorization.WithRole
import enums.RoleEnums

@SpringController
class TagWordController extends Controller with SecureSocial {

  @Autowired
  private var tagwordService: TagWordService = _


  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val list: Option[List[TagWord]] = tagwordService.listAll()
    Ok(views.html.edit.tagword.list(list))
  }

  // Edit - Add
  val tagwordForm = Form(
    mapping(
      "id" -> optional(text),
      "tagwordname" -> nonEmptyText(minLength = 1, maxLength = 255),
      "tagwordgroupname" -> nonEmptyText(minLength = 1, maxLength = 255)
    )(TagWordForm.apply _)(TagWordForm.unapply _)
  )

  def index() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.edit.tagword.index())
  }

  def add() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.edit.tagword.add(tagwordForm))
  }

  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>

    tagwordForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
        BadRequest(views.html.edit.tagword.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val saved = contentData.id match {
          case Some(id) =>
            val item = tagwordService.findById(UUID.fromString(id))
            item.tagName = contentData.tagwordName
            item.tagGroupName = contentData.tagwordGroupName.toLowerCase
            tagwordService.save(item)
          case None =>
            tagwordService.createTag(contentData.tagwordName,"","",contentData.tagwordGroupName.toLowerCase)
        }

        val successMessage = Messages("edit.success") + " - " + Messages("edit.add.success", saved.tagName, saved.objectId.toString)
        Redirect(controllers.routes.TagWordController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }



  // Edit - Edit content
  def edit(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val item = tagwordService.findById(objectId)

    item match {
      case null =>
        Ok(views.html.edit.tagword.index())
      case _ =>
        val form = TagWordForm.apply(
          Some(item.objectId.toString),
          item.tagName,
          item.tagGroupName
        )

        Ok(views.html.edit.tagword.add(tagwordForm.fill(form)))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
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