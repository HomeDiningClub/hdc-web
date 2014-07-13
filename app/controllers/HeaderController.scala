package controllers

import play.api.mvc._
import play.api.i18n.Messages
import securesocial.core.SecureSocial
import play.api.Logger
import models.UserCredential

object HeaderController extends Controller with SecureSocial {

  // Name, Title, Href, Class
  val menuItemsList = Seq[(String,String,Call,String)](
    ("Start page", "Start page title", routes.StartPageController.index, ""),
    ("Files", "Files title", routes.FileController.index, ""),
    ("About us", "About us", routes.ContentController.aboutUs, ""),
    ("Profile", "Profle", routes.HostController.index, ""),
    ("Login", "Login", securesocial.controllers.routes.LoginPage.login, ""),
    ("Campaign page", "Campaign title", routes.CampaignController.index, "")
  )

  // Name, Title, Href, Class, Extra HTML
  var quickLinkList = Seq[(String,String,Call,String,String)](
    (Messages("header.link.host-profile"), Messages("header.link.host-profile"), routes.HostController.index, "", ""),
    (Messages("header.link.inbox"), Messages("header.link.inbox"), routes.HostController.index, "", "<span class=\"badge\">0</span>"),
    (Messages("header.link.login"), Messages("header.link.login"), securesocial.controllers.routes.LoginPage.login, "", "")
  )

  implicit def getUserFromRequest(implicit request: play.api.mvc.RequestHeader): Option[UserCredential] = {
    SecureSocial.currentUser.map {
      u =>
        Logger.debug("Debug:" + u.fullName)
        Some(u.asInstanceOf[UserCredential])
    }
    None
  }

  def index = {
    views.html.header.header(menuItemsList, quickLinkList)
  }

  /*
    private def selectedOrNot(reqUri: String, menuUri: String): String = {
      if(reqUri == menuUri)
        "active"
      else
        ""
    }
  */

}
