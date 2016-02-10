package controllers

import javax.inject.{Named, Inject}

import play.api.cache.CacheApi
import play.api.mvc._
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import models.UserCredential
import customUtils.Helpers
import play.twirl.api.Html
import securesocial.core.SecureSocial.RequestWithUser
import services.ContentService
import play.api.mvc.Controller
import models.viewmodels.MenuItem
import models.content.ContentPage
import customUtils.security.SecureSocialRuntimeEnvironment
import securesocial.core.{SecureSocial}


class HeaderController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                  val messagesApi: MessagesApi,
                                  val contentService: ContentService,
                                  val cache: CacheApi) extends Controller with SecureSocial with I18nSupport {

  def mainMenu = UserAwareAction() { implicit request =>

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

    Ok(views.html.header.mainmenu.render(Some(retMenuItemsList), request2Messages))
  }

  def quickLinks = UserAwareAction() { implicit request =>
    Ok(views.html.header.quicklinks.render(getQuickLinkTitle(request.user), getQuickLinkList(request.user),request2Messages))
  }

  private def getQuickLinkTitle(currentUser: Option[UserCredential]): String = {
    currentUser match {
      case Some(user) => Messages("header.link.host-profile-header", "<span class=\"hidden-xs\">" + user.fullName + "</span>")
      case None => ""
    }
  }

  private def getQuickLinkList(currentUser: Option[UserCredential])(implicit request: RequestHeader): Seq[(String, String, String, String, String)] = {

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

  private def getUserFromRequest(implicit request: RequestHeader): Option[UserCredential] = {
    request.asInstanceOf[RequestWithUser[AnyContent, UserCredential]].user match {
      case Some(user:UserCredential) => Some(user)
      case None => None
      case _ => None
    }
  }

}