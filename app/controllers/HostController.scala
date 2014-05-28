package controllers

import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller

@SpringController
class HostController extends Controller {

  val menuItemsList = Seq[(String,String,Call,String)](
    ("Mat & Dryck", "Mat & Dryck", routes.StartPageController.index, ""),
    ("Blogg", "Blogg", routes.StartPageController.index, ""),
    ("Omdömen", "Omdömen", routes.StartPageController.index, ""),
    ("Inbox", "Inbox", routes.StartPageController.index, "")
  )

  def index = Action {
    Ok(views.html.host.index(menuItemsList))
  }

}