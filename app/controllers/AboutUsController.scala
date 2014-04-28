package controllers

import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller

@SpringController
class AboutUsController extends Controller {

  def index = Action {
    Ok(views.html.aboutus.index())
  }

}