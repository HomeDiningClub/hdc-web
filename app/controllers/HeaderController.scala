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
    ("Start page", "Start page title", routes.StartPageController.index, ""),
    ("Edit mode", "Edit", routes.EditController.index(), ""),
    ("Profile", "Profle", routes.HostController.index, ""),
    ("Recipe", "Recipe", routes.RecipeController.index(), ""),
    ("Login", "Login", securesocial.controllers.routes.LoginPage.login, ""),
    ("About us", "About us", routes.ContentController.aboutUs, ""),
    ("Campaign page", "Campaign title", routes.CampaignController.index, "")
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

    // Name, Title, Href, Class, Extra HTML
    val quickLinkList: Seq[(String,String,Call,String,String)] = getUserFromRequest(request) match {
      case Some(user) =>
        Seq[(String,String,Call,String,String)](
          (user.fullName(), Messages("header.link.host-profile", user.fullName()), routes.HostController.index, "", ""),
          (Messages("header.link.inbox"), Messages("header.link.inbox"), routes.HostController.index, "", "<span class=\"badge\">0</span>"),
          (Messages("header.link.logout"), Messages("header.link.logout"), securesocial.controllers.routes.LoginPage.logout(), "", "")
        )
      case None =>
        Seq[(String,String,Call,String,String)](
          (Messages("header.link.become-member"), Messages("header.link.become-member"), securesocial.controllers.routes.LoginPage.login, "", ""),
          (Messages("header.link.login"), Messages("header.link.login"), securesocial.controllers.routes.LoginPage.login, "", "")
        )
    }

    views.html.header.header.render(menuItemsList, quickLinkList)
  }

}
