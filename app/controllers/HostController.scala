package controllers

import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller

@SpringController
class HostController extends Controller {

  // Constants
  val FOODANDBEVERAGE = "foodandbeverage-tab"
  val BLOG = "blog-tab"
  val REVIEWS = "reviews-tab"
  val INBOX = "inbox-tabs"

  // Link-name, title, link-href, class-name, active
  val menuItemsList = Seq[(String,String,String,String)](
    ("Mat & Dryck", "Mat & Dryck", FOODANDBEVERAGE, "active"),
    ("Blogg", "Blogg", BLOG, ""),
    ("Omdömen", "Omdömen", REVIEWS, ""),
    ("Inbox", "Inbox", INBOX, "")
  )

  def index = Action { implicit request =>
    Ok(views.html.host.index(menuItemsList,FOODANDBEVERAGE,BLOG,REVIEWS,INBOX))
  }

}