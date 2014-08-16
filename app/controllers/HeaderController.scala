package controllers

import play.api.mvc._
import play.api.i18n.Messages
import securesocial.core.SecureSocial
import play.api.Logger
import models.UserCredential
import securesocial.core.java.SecureSocial.UserAwareAction
import play.api.templates.Html

object HeaderController extends Controller with SecureSocial {

  // Name, Title, Href, Class
  val menuItemsList = Seq[(String,String,Call,String)](
    ("Start page", "Start page title", routes.StartPageController.index(), ""),
    ("Admin mode", "Admin", controllers.admin.routes.AdminController.index(), ""),
    ("My Profile", "My profile", routes.UserProfileController.viewProfileByLoggedInUser(), ""),
    ("Recipe", "Recipe", routes.RecipePageController.index(), ""),
    ("Login", "Login", securesocial.controllers.routes.LoginPage.login(), ""),
    ("About us", "About us", routes.ContentPageController.aboutUs(), ""),
    ("Campaign page", "Campaign title", routes.CampaignController.index(), "")
  )

  implicit def getUserFromRequest(implicit request: RequestHeader): Option[UserCredential] = {
    val user = SecureSocial.currentUser.map {
      u =>
        Logger.debug("Debug:" + u.fullName)
        u.asInstanceOf[UserCredential]
    }

    if(user.isDefined)
      user
    else
      None
  }

  def index(request: RequestHeader) = {

    var quickLinkTitle: String = ""
    // Name, Title, Href, Class, Extra HTML
    val quickLinkList: Seq[(String,String,Call,String,String)] = getUserFromRequest(request) match {
      case Some(user) =>
        quickLinkTitle = Messages("header.link.host-profile-header", "<span class=\"hidden-xs\">" + user.fullName() + "</span>")
        Seq[(String,String,Call,String,String)](
          (Messages("header.link.host-profile"), Messages("header.link.host-profile"), routes.UserProfileController.viewProfileByLoggedInUser(), "", ""),
          (Messages("header.link.host-profile-edit"), Messages("header.link.host-profile-edit"), routes.UserProfileController.edit(), "", ""),
          (Messages("header.link.inbox"), Messages("header.link.inbox"), routes.UserProfileController.viewProfileByLoggedInUser(), "", "<span class=\"badge\">0</span>"),
          (Messages("header.link.logout"), Messages("header.link.logout"), securesocial.controllers.routes.LoginPage.logout(), "", "")
        )
      case None =>
        Seq[(String,String,Call,String,String)](
          (Messages("header.link.become-member"), Messages("header.link.become-member"), securesocial.controllers.routes.LoginPage.login, "hidden-xs", ""),
          (Messages("header.link.login"), Messages("header.link.login"), securesocial.controllers.routes.LoginPage.login, "", "")
        )
    }

    views.html.header.header.render(menuItemsList, quickLinkTitle, quickLinkList)
  }

}
