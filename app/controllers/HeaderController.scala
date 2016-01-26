package controllers
/*
import javax.inject.{Named, Inject}

import play.api.mvc._
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import models.UserCredential
import customUtils.Helpers
import scala.collection
import org.springframework.beans.factory.annotation.Autowired
import services.ContentService
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import models.viewmodels.MenuItem
import models.content.ContentPage
import customUtils.security.SecureSocialRuntimeEnvironment
import securesocial.core.RuntimeEnvironment
import scala.concurrent.ExecutionContext

//@Named
@deprecated("Use the ProvidesAppContext instead","Since play 2.4 upgrade")
class HeaderController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment, val messagesApi: MessagesApi, val contentService: ContentService) extends Controller with securesocial.core.SecureSocial with I18nSupport {

  /*
  @Autowired
  private var contentService: ContentService = _
*/

  def bodyBg = {
    val r = scala.util.Random
    val number = r.nextInt(10) + 1
    "background-image:url('/assets/images/general/body-bg-faded/2048x1360-" + number.toString + ".jpg')"
  }

  def index(implicit request: RequestHeader) { //implicit request: SecuredRequest[AnyContent,UserCredential] =>

    //val currentUser = request.user

    val currentUser: Option[UserCredential] = customUtils.Helpers.getUserFromRequest(request)

    // Quick links
    var quickLinkTitle: String = ""
    // Name, Title, Href, Class, Extra HTML
    val quickLinkList: Seq[(String,String,String,String,String)] = currentUser match {
      case Some(user) =>
        quickLinkTitle = Messages("header.link.host-profile-header", "<span class=\"hidden-xs\">" + user.fullName + "</span>")
        val menu = collection.mutable.Buffer[(String,String,String,String,String)](
          (Messages("header.link.host-profile"), Messages("header.link.host-profile"), routes.UserProfileController.viewProfileByLoggedInUser().url, "", ""),
          (Messages("header.link.host-profile-edit"), Messages("header.link.host-profile-edit"), routes.UserProfileController.edit().url, "", ""),
          (Messages("header.link.inbox"), Messages("header.link.inbox"), routes.UserProfileController.viewProfileByLoggedInUser().url + "#inbox-tab", "", "") //Removed until we fetch nr of messages: "<span class=\"badge\">0</span>")
        )

        if(Helpers.isUserAdmin(user))
          menu.append((Messages("header.link.admin"), Messages("header.link.admin"), admin.routes.AdminController.index().url, "", ""))

        menu.append((Messages("header.link.logout"), Messages("header.link.logout"), securesocial.controllers.routes.LoginPage.logout().url, "", ""))
        menu.toSeq
      case None =>
        Seq[(String,String,String,String,String)](
          (Messages("header.link.become-member"), Messages("header.link.become-member"), securesocial.controllers.routes.LoginPage.login.url, "hidden-xs", ""),
          (Messages("header.link.login"), Messages("header.link.login"), securesocial.controllers.routes.LoginPage.login.url, "", "")
        )
    }

    // Main menu

    // Define default items
    val defMenuItem01 = MenuItem(
      name = Messages("header.main-menu.link.startpage.text"),
      title = Messages("header.main-menu.link.startpage.title"),
      url = routes.StartPageController.index().url,
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
      url = routes.BrowsePageController.index().url
      )



    // Define main items
    val menuItemsList: Option[List[MenuItem]] = contentService.getMainMenuItems match {
      case Some(items) => Some(items.map {
        page: ContentPage =>
          MenuItem(
            name = page.name,
            title = page.name,
            url = routes.ContentPageController.viewContentByName(page.route).url
          )
      })
      case None => None
    }

    val retMenuItemsList: collection.mutable.ListBuffer[MenuItem] = collection.mutable.ListBuffer[MenuItem]()
    if(menuItemsList.nonEmpty)
      retMenuItemsList.appendAll(menuItemsList.get)

    retMenuItemsList.prepend(defMenuItem05)
    retMenuItemsList.prepend(defMenuItem01)
    retMenuItemsList.append(defMenuItem03)
    retMenuItemsList.append(defMenuItem04)



    views.html.header.header.render(menuItems = Some(retMenuItemsList.result()), quickLinkTitle = quickLinkTitle, quicklinkItems = quickLinkList, messages = request2Messages)
  }


}
*/