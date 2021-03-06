package controllers.admin

import javax.inject.{Named, Inject}

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.{AnyContent, Controller}

import play.api.data.Form
import play.api.data.Forms._
import org.springframework.beans.factory.annotation.Autowired
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.TagWordService
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import constants.FlashMsgConstants
import models.profile.TagWord
import java.util.UUID
import customUtils.authorization.WithRole
import enums.RoleEnums
import models.UserCredential
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.TagWordForm

class AdminTagWordController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                        val tagWordService: TagWordService,
                                        val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  /*
  @Autowired
  private var tagwordService: TagWordService = _
*/

  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val list: Option[List[TagWord]] = tagWordService.listAll()
    Ok(views.html.admin.tagword.list(list))
  }

  // Edit - Add
  val tagwordForm = Form(
    mapping(
      "id" -> optional(text),
      "tagwordname" -> nonEmptyText(minLength = 1, maxLength = 255),
      "tagwordgroupname" -> nonEmptyText(minLength = 1, maxLength = 255)
    )(TagWordForm.apply)(TagWordForm.unapply)
  )

  def editIndex() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.tagword.index())
  }

  def add() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.tagword.add(tagwordForm))
  }

  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    tagwordForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.tagword.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val saved = contentData.id match {
          case Some(id) =>
            val item = tagWordService.findById(UUID.fromString(id)) match {
              case None => tagWordService.createTag(contentData.tagwordName,"","",contentData.tagwordGroupName.toLowerCase)
              case Some(foundTag) => foundTag
            }
            item.tagName = contentData.tagwordName
            item.tagGroupName = contentData.tagwordGroupName.toLowerCase
            tagWordService.save(item)
          case None =>
            tagWordService.createTag(contentData.tagwordName,"","",contentData.tagwordGroupName.toLowerCase)
        }

        val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", saved.tagName, saved.objectId.toString)
        Redirect(controllers.admin.routes.AdminTagWordController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }



  // Edit - Edit content
  def edit(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val optionItem = tagWordService.findById(objectId)

    optionItem match {
      case None =>
        Ok(views.html.admin.tagword.index())
      case Some(item) =>
        val form = TagWordForm.apply(
          Some(item.objectId.toString),
          item.tagName,
          item.tagGroupName
        )

        Ok(views.html.admin.tagword.add(tagwordForm.fill(form)))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val result: Boolean = tagWordService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("admin.success") + " - " + Messages("admin.delete.success", objectId.toString)
        Redirect(controllers.admin.routes.AdminTagWordController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.delete.error")
        Redirect(controllers.admin.routes.AdminTagWordController.listAll()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}