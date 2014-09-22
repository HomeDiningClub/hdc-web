package controllers

import play.api._
import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller

@SpringController
class CampaignController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.campaign.index())
  }

}

@SpringController
object CampaignController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.campaign.index())
  }

}