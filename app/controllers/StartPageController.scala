package controllers

import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.viewmodels.SearchStartPageForm


@SpringController
class StartPageController extends Controller {

  // Search startpage form
//  val searchStartPageForm = Form(
//    mapping(
//      "freeText" -> optional(text),
//      "area" -> Seq[(String, String)],
//      "foodArea" -> Seq[(String, String)]
//    )(SearchStartPageForm.apply _)(SearchStartPageForm.unapply _)
//  )

  def filterProfiles = Action { implicit request =>
    Ok(views.html.startpage.index())
    //Ok(views.html.startpage.index(searchStartPageForm))
  }

  def index = Action {
    Ok(views.html.startpage.index())
  }

}