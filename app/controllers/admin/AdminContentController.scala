package controllers.admin

import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import play.api.i18n.Messages
import org.springframework.beans.factory.annotation.Autowired
import services.ContentService
import models.content._
import play.api.data._
import play.api.data.Forms._
import constants.FlashMsgConstants
import models.viewmodels.AddContentForm
import java.util.UUID
import scala.collection.mutable
import enums.{RoleEnums, ContentCategoryEnums, ContentStateEnums}
import utils.authorization.WithRole

@SpringController
class AdminContentController extends Controller with securesocial.core.SecureSocial {

  @Autowired
  private var contentService: ContentService = _



  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val listOfPage: Option[List[ContentPage]] = contentService.getListOfAll(fetchAll = true)
    Ok(views.html.admin.content.list(listOfPage))
  }

  // Edit - Add Content
  val contentForm = Form(
    mapping(
      "pageid" -> optional(text),
      "pageparentid" -> optional(text),
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

  def editIndex() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.admin.content.index())
  }

  def add() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    // Default values for new item
    val defaultContent = AddContentForm(None,None,"","",None,None,None,ContentStateEnums.UNPUBLISHED.toString,None,visibleInMenus = false)
    Ok(views.html.admin.content.add(contentForm.fill(defaultContent), getPagesAsDropDown, getContentStatesAsDropDown, getCategoriesAsDropDown))
  }

  def addSubmit() = SecuredAction(WithRole(RoleEnums.ADMIN)) { implicit request =>

    contentForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.content.add(errors, getPagesAsDropDown, getContentStatesAsDropDown, getCategoriesAsDropDown)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val newContent = contentData.id match {
          case Some(id) =>
            val page = contentService.findContentById(UUID.fromString(id))
            page.name = contentData.name
            page.route = contentData.route
            page.visibleInMenus = contentData.visibleInMenus
            page
          case None =>
            new ContentPage(contentData.name,contentData.route, contentData.visibleInMenus)
        }

        // If this occurs, user has sent an invalid UUID to edit and save
        if(newContent == null){
          val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
          BadRequest(views.html.admin.content.add(contentForm, getPagesAsDropDown, getContentStatesAsDropDown, getCategoriesAsDropDown)).flashing(FlashMsgConstants.Error -> errorMessage)

        }

        contentData.title match {
          case Some(title) => newContent.title = title
          case None => newContent.title = ""
        }
        contentData.preamble match {
          case Some(preamble) => newContent.preamble = preamble
          case None => newContent.preamble = ""
        }
        contentData.mainBody match {
          case Some(content) => newContent.mainBody = content
          case None => newContent.mainBody = ""
        }
        contentData.parentId match {
          case Some(id) =>
            val settingParentPage = contentService.findContentById(UUID.fromString(id))

            // Cannot set itself as a parent
            if(settingParentPage.objectId == newContent.objectId)
              newContent.parentPage = null
            else
              newContent.parentPage = contentService.findContentById(UUID.fromString(id))

          case None =>
            if(newContent.parentPage != null)
              newContent.parentPage = null
        }

        // get request value from submitted form
//        val map: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded match {
//          case Some(content) => content.map.get("contentcategories")
//        }
//        val checkedVal: Array[String] = map.get("contentcategories") // get selected categories
//
//        // Assign checked value to model
//        newContent.contentCategories = checkedVal


        contentData.contentCategories match {
          case Some(listOfCategories) =>
            newContent.contentCategories = listOfCategories.toArray
          case None =>
            newContent.contentCategories = null
        }

        if(contentData.contentState == ContentStateEnums.PUBLISHED.toString)
          newContent.publish()
        else if(contentData.contentState == ContentStateEnums.UNPUBLISHED.toString){
          newContent.unPublish()
        }

        val savedContentPage = contentService.addContentPage(newContent)
        val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", savedContentPage.name, savedContentPage.objectId.toString)
        Redirect(controllers.admin.routes.AdminContentController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }

  private def getPagesAsDropDown: Option[Seq[(String,String)]] = {
    val returnItems: Option[Seq[(String,String)]] = contentService.getListOfAll() match {
      case Some(items) =>
        var bufferList : mutable.Buffer[(String,String)] = mutable.Buffer[(String,String)]()

        // Map and add the rest
        items.sortBy(tw => tw.name).toBuffer.map {
          item: ContentPage =>
            bufferList += ((item.objectId.toString, item.name + " - (" + item.route + ")"))
        }
        Some(bufferList.toSeq)
      case None =>
        None
    }
    returnItems
  }


  private def getContentStatesAsDropDown: Seq[(String,String)] = {
    val returnItems: Seq[(String,String)] = Seq(
      (ContentStateEnums.PUBLISHED.toString,ContentStateEnums.PUBLISHED.toString),
      (ContentStateEnums.UNPUBLISHED.toString,ContentStateEnums.UNPUBLISHED.toString)
    )
    returnItems
  }

  private def getCategoriesAsDropDown: Option[Seq[(String,String)]] = {
    val returnItems: Seq[(String,String)] = List(
      (ContentCategoryEnums.MAINMENU.toString,ContentCategoryEnums.MAINMENU.toString),
      (ContentCategoryEnums.NEWS.toString,ContentCategoryEnums.NEWS.toString),
      (ContentCategoryEnums.QUICKLINKS.toString,ContentCategoryEnums.QUICKLINKS.toString)
    )
    Some(returnItems)
  }



  // Edit - Edit content
  def edit(objectId: java.util.UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val item = contentService.findContentById(objectId, fetchAll = true)
    item match {
      case null =>
        Ok(views.html.admin.content.index())
      case _ =>
        val form = AddContentForm.apply(
        Some(item.objectId.toString),
        item.parentPage match {
          case null => None
          case page => Some(page.objectId.toString)
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

      Ok(views.html.admin.content.add(contentForm.fill(form), getPagesAsDropDown, getContentStatesAsDropDown, getCategoriesAsDropDown))
    }
  }

  // Edit - Delete content
  def delete(objectId: java.util.UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val result: Boolean = contentService.deleteContentPageById(objectId)

    result match {
      case true =>
        val successMessage = Messages("admin.success") + " - " + Messages("admin.delete.success", objectId.toString)
        Redirect(controllers.admin.routes.AdminContentController.editIndex()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.delete.error")
        Redirect(controllers.admin.routes.AdminContentController.editIndex()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }


}