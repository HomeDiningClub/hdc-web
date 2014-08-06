package controllers

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
import securesocial.core.SecureSocial._
import utils.authorization.WithRole

@SpringController
class ContentController extends Controller with securesocial.core.SecureSocial {

  @Autowired
  private var contentService: ContentService = _


  // About us, Press and UserTerms
  // Menu
  lazy val aboutUsAsideMenu = List[MenuItem](
    new MenuItem(Messages("aboutus.header"),Messages("aboutus.header"),Messages("aboutus.header"), routes.ContentController.aboutUs().url, ""),
    new MenuItem(Messages("press.header"),Messages("press.header"),Messages("press.header"), routes.ContentController.press().url, ""),
    new MenuItem(Messages("usertermsandconditions.header"),Messages("usertermsandconditions.header"),Messages("usertermsandconditions.header"), routes.ContentController.userTermsAndConditions().url, "")
  )
  // Actions
  def aboutUs = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("aboutus.title"),
      column1Header = Messages("aboutus.header"),
      column1Body = Messages("aboutus.body"),
      menuList = aboutUsAsideMenu,
      menuHeader = Messages("aboutus.header"))
    )
  }
  def press = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("press.title"),
      column1Header = Messages("press.header"),
      column1Body = Messages("press.body"),
      menuList = aboutUsAsideMenu,
      menuHeader = Messages("press.header"))
    )
  }
  def userTermsAndConditions = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("usertermsandconditions.title"),
      column1Header = Messages("usertermsandconditions.header"),
      column1Body = Messages("usertermsandconditions.body"),
      menuList = aboutUsAsideMenu,
      menuHeader = Messages("usertermsandconditions.header"))
    )
  }




  // How does it work
  def howDoesItWork = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("howdoesitwork.title"),
      column1Header = Messages("howdoesitwork.header"),
      column1Body = Messages("howdoesitwork.body"))
    )
  }



  // Become a member & Perfect guest
  // Menu
  lazy val becomeAMemberAsideMenu = List[MenuItem](
    new MenuItem(Messages("becomeamember.header"),Messages("becomeamember.header"),Messages("becomeamember.header"), routes.ContentController.becomeAMember().url, ""),
    new MenuItem(Messages("theperfectguest.header"),Messages("theperfectguest.header"),Messages("theperfectguest.header"), routes.ContentController.thePerfectGuest().url, "")
  )
  // Actions
  def becomeAMember = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("becomeamember.title"),
      column1Header = Messages("becomeamember.header"),
      column1Body = Messages("becomeamember.body"),
      menuList = becomeAMemberAsideMenu,
      menuHeader = Messages("becomeamember.header"))
    )
  }
  def thePerfectGuest = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("theperfectguest.title"),
      column1Header = Messages("theperfectguest.header"),
      column1Body = Messages("theperfectguest.body"),
      menuList = becomeAMemberAsideMenu,
      menuHeader = Messages("theperfectguest.header"))
    )
  }



  // Become a host & practical info & profiletext
  // Menu
  lazy val becomeAHostAsideMenu = List[MenuItem](
    new MenuItem(Messages("becomeahost.header"),Messages("becomeahost.header"),Messages("becomeahost.header"), routes.ContentController.becomeAHost().url, ""),
    new MenuItem(Messages("practicalinfo.header"),Messages("practicalinfo.header"),Messages("practicalinfo.header"), routes.ContentController.practicalInfo().url, ""),
    new MenuItem(Messages("examplehosttext.header"),Messages("examplehosttext.header"),Messages("examplehosttext.header"), routes.ContentController.exampleHostText().url, "")
  )
  // Actions
  def becomeAHost = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("becomeahost.title"),
      column1Header = Messages("becomeahost.header"),
      column1Body = Messages("becomeahost.body"),
      menuList = becomeAHostAsideMenu,
      menuHeader = Messages("becomeahost.header"))
    )
  }
  def practicalInfo = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("practicalinfo.title"),
      column1Header = Messages("practicalinfo.header"),
      column1Body = Messages("practicalinfo.body"),
      menuList = becomeAHostAsideMenu,
      menuHeader = Messages("practicalinfo.header"))
    )
  }
  def exampleHostText = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("examplehosttext.title"),
      column1Header = Messages("examplehosttext.header"),
      column1Body = Messages("examplehosttext.body"),
      menuList = becomeAHostAsideMenu,
      menuHeader = Messages("examplehosttext.header"))
    )
  }




  // References
  def references = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("references.title"),
      column1Header = Messages("references.header"),
      column1Body = Messages("references.body"))
    )
  }


  // FAQ
  def faq = Action { implicit request =>
    Ok(views.html.contentcolumns.onecolumn(
      urlTitle = Messages("faq.title"),
      column1Header = Messages("faq.header"),
      column1Body = Messages("faq.body"))
    )
  }


  // Contact us
  def contact = Action { implicit request =>
    Ok(views.html.contentcolumns.twocolumn(
      urlTitle = Messages("contact.title"),
      column1Header = Messages("contact.header1"),
      column1Body = Messages("contact.body1"),
      column2Header = Messages("contact.header2"),
      column2Body = Messages("contact.body2"))
    )
  }






  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val listOfPage: Option[List[ContentPage]] = contentService.getListOfAll(fetchAll = true)
    Ok(views.html.edit.content.list(listOfPage))
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
    )(AddContentForm.apply _)(AddContentForm.unapply _)
  )

  def index() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.edit.content.index())
  }

  def add() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    // Default values for new item
    val defaultContent = AddContentForm(None,None,"","",None,None,None,ContentStateEnums.UNPUBLISHED.toString,None,visibleInMenus = false)
    Ok(views.html.edit.content.add(contentForm.fill(defaultContent), getPagesAsDropDown, getContentStatesAsDropDown, getCategoriesAsDropDown))
  }

  def addSubmit() = SecuredAction(WithRole(RoleEnums.ADMIN)) { implicit request =>

    contentForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
        BadRequest(views.html.edit.content.add(errors, getPagesAsDropDown, getContentStatesAsDropDown, getCategoriesAsDropDown)).flashing(FlashMsgConstants.Error -> errorMessage)
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
          val errorMessage = Messages("edit.error") + " - " + Messages("edit.add.error")
          BadRequest(views.html.edit.content.add(contentForm, getPagesAsDropDown, getContentStatesAsDropDown, getCategoriesAsDropDown)).flashing(FlashMsgConstants.Error -> errorMessage)

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
        val successMessage = Messages("edit.success") + " - " + Messages("edit.add.success", savedContentPage.name, savedContentPage.objectId.toString)
        Redirect(controllers.routes.ContentController.index()).flashing(FlashMsgConstants.Success -> successMessage)
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
        Ok(views.html.edit.content.index())
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

      Ok(views.html.edit.content.add(contentForm.fill(form), getPagesAsDropDown, getContentStatesAsDropDown, getCategoriesAsDropDown))
    }
  }

  // Edit - Delete content
  def delete(objectId: java.util.UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val result: Boolean = contentService.deleteContentPageById(objectId)

    result match {
      case true =>
        val successMessage = Messages("edit.success") + " - " + Messages("edit.delete.success", objectId.toString)
        Redirect(controllers.routes.ContentController.index()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("edit.error") + " - " + Messages("edit.delete.error")
        Redirect(controllers.routes.ContentController.index()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }


}