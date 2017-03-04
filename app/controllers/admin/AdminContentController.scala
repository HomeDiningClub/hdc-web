package controllers.admin

import javax.inject.{Named, Inject}
import play.api.cache._
import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import org.springframework.beans.factory.annotation.Autowired
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.ContentService
import models.content._
import play.api.data._
import play.api.data.Forms._
import constants.FlashMsgConstants
import java.util.UUID
import scala.collection.mutable
import enums.{RoleEnums, ContentCategoryEnums, ContentStateEnums}
import customUtils.authorization.WithRole
import java.util
import models.UserCredential
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.AddContentForm

class AdminContentController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                        val contentService: ContentService,
                                        val messagesApi: MessagesApi, val cache: CacheApi) extends Controller with SecureSocial with I18nSupport {
/*
  @Autowired
  private var contentService: ContentService = _
*/

  // Edit - Listing
  def listAll: Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val listOfPage: Option[List[ContentPage]] = contentService.getListOfAll
    Ok(views.html.admin.content.list(listOfPage))
  }

  // Edit - Add Content
  val contentForm = Form(
    mapping(
      "pageid" -> optional(text),
      "relatedpages" -> optional(list(text)),
      "pagename" -> nonEmptyText(minLength = 1, maxLength = 255),
      "pageroute" -> nonEmptyText(minLength = 1, maxLength = 255),
      "pagetitle" -> optional(text),
      "pagepreamble" -> optional(text),
      "pagebody" -> optional(text),
      "contentstate" -> nonEmptyText,
      "contentcategories" -> optional(list(text)),
      "pagevisibleinmenus" -> boolean
    )(AddContentForm.apply)(AddContentForm.unapply)
  )

  def editIndex(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: RequestHeader =>
    Ok(views.html.admin.content.index())
  }

  def add(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: RequestHeader =>
    // Default values for new item
    val defaultContent = AddContentForm(None,None,"","",None,None,None,ContentStateEnums.UNPUBLISHED.toString,None,visibleInMenus = false)
    Ok(views.html.admin.content.add(contentForm.fill(defaultContent), contentService.getPagesAsDropDown(), contentService.getContentStatesAsDropDown, contentService.getCategoriesAsDropDown))
  }

  def addSubmit(): Action[AnyContent] = SecuredAction(WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    contentForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.content.add(errors, contentService.getPagesAsDropDown(), contentService.getContentStatesAsDropDown, contentService.getCategoriesAsDropDown)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val newContent: Option[ContentPage] = contentData.id match {
          case Some(id) =>
            contentService.findContentById(UUID.fromString(id)) match {
              case None => None
              case Some(page) =>
                page.name = contentData.name
                page.route = contentData.route
                page.visibleInMenus = contentData.visibleInMenus
                Some(page)
            }
          case None =>
            Some(new ContentPage(contentData.name,contentData.route, contentData.visibleInMenus))
        }

        // If this occurs, user has sent an invalid UUID to edit and save
        if(newContent == None){
          val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
          BadRequest(views.html.admin.content.add(contentForm, contentService.getPagesAsDropDown(), contentService.getContentStatesAsDropDown, contentService.getCategoriesAsDropDown)).flashing(FlashMsgConstants.Error -> errorMessage)

        }

        contentData.title match {
          case Some(title) => newContent.get.title = title
          case None => newContent.get.title = ""
        }
        contentData.preamble match {
          case Some(preamble) => newContent.get.preamble = preamble
          case None => newContent.get.preamble = ""
        }
        contentData.mainBody match {
          case Some(content) => newContent.get.mainBody = content
          case None => newContent.get.mainBody = ""
        }
//        contentData.parentId match {
//          case Some(id) =>
//            val settingParentPage = contentService.findContentById(UUID.fromString(id))
//
//            // Cannot set itself as a parent
//            if(settingParentPage.objectId == newContent.objectId)
//              newContent.parentPage = null
//            else
//              newContent.parentPage = contentService.findContentById(UUID.fromString(id))
//
//          case None =>
//            if(newContent.parentPage != null)
//              newContent.parentPage = null
//        }

        // get request value from submitted form
//        val map: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded match {
//          case Some(content) => content.map.get("contentcategories")
//        }
//        val checkedVal: Array[String] = map.get("contentcategories") // get selected categories
//
//        // Assign checked value to model
//        newContent.contentCategories = checkedVal

        contentData.relatedPages match {
          case Some(listOfPages) =>
            // Clear earlier pages
            newContent.get.removeAllRelatedPages()
            listOfPages.foreach {
              objIdAsStr: String =>
                contentService.findContentById(UUID.fromString(objIdAsStr)) match {
                  case None =>
                  case Some(item) =>
                    newContent.get.addRelatedPage(item)
                }
            }
          case None =>
            contentService.removeAllRelatedPages(newContent.get)
        }

        contentData.contentCategories match {
          case Some(listOfCategories) =>
            newContent.get.contentCategories = listOfCategories.toArray
          case None =>
            newContent.get.contentCategories = null
        }

        if(contentData.contentState == ContentStateEnums.PUBLISHED.toString)
          newContent.get.publish()
        else if(contentData.contentState == ContentStateEnums.UNPUBLISHED.toString){
          newContent.get.unPublish()
        }

        clearCache()
        val savedContentPage = contentService.addContentPage(newContent.get)
        val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", savedContentPage.name, savedContentPage.objectId.toString)
        Redirect(controllers.admin.routes.AdminContentController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }

  def clearCache(): Unit ={
    cache.remove("main.menu")
  }


  // Edit - Edit content
  def edit(objectId: java.util.UUID): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: RequestHeader =>
    contentService.findContentById(objectId) match {
      case None =>
        Ok(views.html.admin.content.index())
      case Some(item) =>
        val form = AddContentForm.apply(
        Some(item.objectId.toString),
        item.getRelatedPages match {
          case null => None
          case relPages => Some(contentService.mapRelatedPagesToStringOfObjectIds(item))
        },
        item.name,
        item.route,
        Some(item.title),
        Some(item.preamble),
        Some(item.mainBody),
        item.contentState match {
          case null => ContentStateEnums.UNPUBLISHED.toString
          case state => state
        },
        item.contentCategories match {
          case null => None
          case categories => Some(item.contentCategories.toList)
        },
        item.visibleInMenus)

      Ok(views.html.admin.content.add(contentForm.fill(form), contentService.getPagesAsDropDown(Some(item)), contentService.getContentStatesAsDropDown, contentService.getCategoriesAsDropDown))
    }
  }

  // Edit - Delete content
  def delete(objectId: java.util.UUID): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: RequestHeader =>
    val result: Boolean = contentService.deleteContentPageById(objectId)

    result match {
      case true =>
        clearCache()
        val successMessage = Messages("admin.success") + " - " + Messages("admin.delete.success", objectId.toString)
        Redirect(controllers.admin.routes.AdminContentController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.delete.error")
        Redirect(controllers.admin.routes.AdminContentController.listAll()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }


}