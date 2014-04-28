package controllers

import play.api._
import play.api.mvc._

object CampaignController extends Controller {

  def index = Action {
    Ok(views.html.campaign.index())
  }

}