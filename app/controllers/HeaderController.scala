package controllers

import play.api.mvc._
import play.api.i18n.Messages
import securesocial.core.SecureSocial
import play.api.Logger
import models.UserCredential
import securesocial.core.java.SecureSocial.UserAwareAction
import play.api.templates.Html
import utils.Helpers
import utils.authorization.WithRole
import enums.RoleEnums
import scala.collection
import org.springframework.beans.factory.annotation.Autowired
import services.ContentService
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import models.viewmodels.MenuItem
import models.content.ContentPage

class HeaderController extends Controller with SecureSocial {
}

@SpringController
object HeaderController extends Controller with SecureSocial {

  @Autowired
  private var contentService: ContentService = _

  // Name, Title, Href, Class
//  val menuItemsList = Seq[(String,String,Call,String)](
//    ("Start page", "Start page title", routes.StartPageController.index(), ""),
//    ("My Profile", "My profile", routes.UserProfileController.viewProfileByLoggedInUser(), ""),
//    ("Login", "Login", securesocial.controllers.routes.LoginPage.login(), ""),
//    ("About us", "About us", routes.ContentPageController.aboutUs(), ""),
//    ("Campaign page", "Campaign title", routes.CampaignController.index(), "")
//  )


  def index(request: RequestHeader) = {

    // Quick links
    var quickLinkTitle: String = ""
    // Name, Title, Href, Class, Extra HTML
    val quickLinkList: Seq[(String,String,Call,String,String)] = Helpers.getUserFromRequest(request) match {
      case Some(user) =>
        quickLinkTitle = Messages("header.link.host-profile-header", "<span class=\"hidden-xs\">" + user.fullName() + "</span>")
        val menu = collection.mutable.Buffer[(String,String,Call,String,String)](
          (Messages("header.link.host-profile"), Messages("header.link.host-profile"), routes.UserProfileController.viewProfileByLoggedInUser(), "", ""),
          (Messages("header.link.host-profile-edit"), Messages("header.link.host-profile-edit"), routes.UserProfileController.edit(), "", ""),
          (Messages("header.link.inbox"), Messages("header.link.inbox"), routes.UserProfileController.viewProfileByLoggedInUser(), "", "<span class=\"badge\">0</span>")
        )

        if(Helpers.isUserAdmin(user))
          menu.append((Messages("header.link.admin"), Messages("header.link.admin"), admin.routes.AdminController.index(), "", ""))

        menu.append((Messages("header.link.logout"), Messages("header.link.logout"), securesocial.controllers.routes.LoginPage.logout(), "", ""))
        menu.toSeq
      case None =>
        Seq[(String,String,Call,String,String)](
          (Messages("header.link.become-member"), Messages("header.link.become-member"), securesocial.controllers.routes.LoginPage.login, "hidden-xs", ""),
          (Messages("header.link.login"), Messages("header.link.login"), securesocial.controllers.routes.LoginPage.login, "", "")
        )
    }

    // Main menu

    // Define default items
    val defMenuItem01 = MenuItem(
      name = Messages("header.main-menu.link.startpage"),
      title = Messages("header.main-menu.link.startpage"),
      url = routes.StartPageController.index().url,
      icon = "glyphicon glyphicon-home")

    val defMenuItem02 = MenuItem(
      name = Messages("header.main-menu.link.newsletter"),
      title = Messages("header.main-menu.link.newsletter"),
      url = routes.CampaignController.index().url,
      icon = "glyphicon glyphicon-envelope")


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

    retMenuItemsList.prepend(defMenuItem01)
    retMenuItemsList.append(defMenuItem02)



    views.html.header.header.render(Some(retMenuItemsList.result()), quickLinkTitle, quickLinkList)
  }

}
