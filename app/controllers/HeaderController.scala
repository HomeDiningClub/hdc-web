package controllers

import play.api.mvc._

object HeaderController extends Controller{

  val menuItemsList = Seq[(String,String,Call,String)](
    ("Start page", "Start page title", routes.StartPageController.index, ""),
    ("Files", "Files title", routes.FileController.index, ""),
    ("About us", "About us", routes.ContentController.aboutUs, ""),
    ("Profile", "Profle", routes.HostController.index, ""),
    ("Login", "Login", securesocial.controllers.routes.LoginPage.login, ""),
    ("Campaign page", "Campaign title", routes.CampaignController.index, "")
  )

  def index = {
    views.html.header.header(menuItemsList)
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
