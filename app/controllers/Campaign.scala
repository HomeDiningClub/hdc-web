package controllers

import play.api._
import play.api.mvc._

object Campaign extends Controller {

  def index = Action {
    Ok(views.html.campaign.index())
  }

}