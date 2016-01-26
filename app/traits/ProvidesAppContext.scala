package traits

import com.google.inject.ImplementedBy
import customUtils.Helpers
import customUtils.security.SecureSocialRuntimeEnvironment
import models.UserCredential
import models.content.ContentPage
import play.api.mvc._
import play.api.cache.CacheApi
import models.viewmodels.{MenuItem, AppContext}
import play.api.i18n.{Messages, I18nSupport, MessagesApi}
import play.api.mvc.RequestHeader
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.RequestWithUser
import services.ContentService
import javax.inject.Inject

trait ProvidesAppContext extends Controller with SecureSocial with I18nSupport {

  @Inject val env: SecureSocialRuntimeEnvironment = null
  @Inject var contentService: ContentService = null
  @Inject var cache: CacheApi = null
  @Inject var messagesApi: MessagesApi = null

  implicit def appContext(implicit request: RequestHeader): AppContext = {

    val user = getUserFromRequest

    AppContext(
      headBodyBg = bodyBg,
      headQuickLinkTitle = quickLinkTitle(user),
      headMenuItems = buildMainMenu,
      headQuickLinks = quickLinkList(user)
    )
  }


  def getUserFromRequest(implicit request: RequestHeader): Option[UserCredential] = {
    request.asInstanceOf[RequestWithUser[AnyContent, UserCredential]].user match {
      case Some(user:UserCredential) => Some(user)
      case None => None
      case _ => None
    }
  }

  def buildMainMenu: Option[List[MenuItem]] = {

    val retMenuItemsList: List[MenuItem] = cache.getOrElse[List[MenuItem]]("main.menu") {

      // Define default items
      val defMenuItem01 = MenuItem(
        name = Messages("header.main-menu.link.startpage.text"),
        title = Messages("header.main-menu.link.startpage.title"),
        url = controllers.routes.StartPageController.index().url,
        icon = "glyphicon glyphicon-home")

      val defMenuItem03 = MenuItem(
        name = Messages("header.main-menu.link.fb.text"),
        title = Messages("header.main-menu.link.fb.title"),
        url = Messages("header.main-menu.link.fb.href"),
        icon = "genericon genericon-facebook",
        target = "_blank",
        wrapperCssClass = "pull-right",
        textCssClass = "hidden-sm"
      )

      val defMenuItem04 = MenuItem(
        name = Messages("header.main-menu.link.instagram.text"),
        title = Messages("header.main-menu.link.instagram.title"),
        url = Messages("header.main-menu.link.instagram.href"),
        icon = "genericon genericon-instagram",
        target = "_blank",
        wrapperCssClass = "pull-right",
        textCssClass = "hidden-sm"
      )

      val defMenuItem05 = MenuItem(
        name = Messages("header.main-menu.link.members-browse.text"),
        title = Messages("header.main-menu.link.members-browse.title"),
        url = controllers.routes.BrowsePageController.index().url
      )

      // Define main items
      val menuItemsList: Option[List[MenuItem]] = contentService.getMainMenuItems match {
        case Some(items) => Some(items.map {
          page: ContentPage =>
            MenuItem(
              name = page.name,
              title = page.name,
              url = controllers.routes.ContentPageController.viewContentByName(page.route).url
            )
        })
        case None => None
      }

      val retMenuItemsListBuffer: collection.mutable.ListBuffer[MenuItem] = collection.mutable.ListBuffer[MenuItem]()

      if(menuItemsList.nonEmpty)
        retMenuItemsListBuffer.appendAll(menuItemsList.get)

      retMenuItemsListBuffer.prepend(defMenuItem05)
      retMenuItemsListBuffer.prepend(defMenuItem01)
      retMenuItemsListBuffer.append(defMenuItem03)
      retMenuItemsListBuffer.append(defMenuItem04)

      retMenuItemsListBuffer.result()
    }

    Some(retMenuItemsList)
  }



  def bodyBg = {
    val r = scala.util.Random
    val number = r.nextInt(10) + 1
    "background-image:url('/assets/images/general/body-bg-faded/2048x1360-" + number.toString + ".jpg')"
  }

  def quickLinkTitle(currentUser: Option[UserCredential]): String = {
    currentUser match {
      case Some(user) => Messages("header.link.host-profile-header", "<span class=\"hidden-xs\">" + user.fullName + "</span>")
      case None => ""
    }
  }

  def quickLinkList(currentUser: Option[UserCredential])(implicit request: RequestHeader): Seq[(String, String, String, String, String)] = {

    currentUser match {
      case Some(user) => {
        val menu = collection.mutable.Buffer[(String,String,String,String,String)](
            (Messages("header.link.host-profile"), Messages("header.link.host-profile"), controllers.routes.UserProfileController.viewProfileByLoggedInUser().url, "", ""),
            (Messages("header.link.host-profile-edit"), Messages("header.link.host-profile-edit"), controllers.routes.UserProfileController.edit().url, "", ""),
            (Messages("header.link.inbox"), Messages("header.link.inbox"), controllers.routes.UserProfileController.viewProfileByLoggedInUser().url + "#inbox-tab", "", "") //Removed until we fetch nr of messages: "<span class=\"badge\">0</span>")
          )

        if (Helpers.isUserAdmin(currentUser))
          menu.append((Messages("header.link.admin"), Messages("header.link.admin"), controllers.admin.routes.AdminController.index().url, "", ""))

        menu.append((Messages("header.link.logout"), Messages("header.link.logout"), env.routes.loginPageUrl, "", ""))
        menu.toSeq
      }
      case None => {
        Seq[(String,String,String,String,String)](
            (Messages("header.link.become-member"), Messages("header.link.become-member"), env.routes.startSignUpUrl, "hidden-xs", ""),
            (Messages("header.link.login"), Messages("header.link.login"), env.routes.loginPageUrl, "", "")
          )
      }
    }
  }

}
