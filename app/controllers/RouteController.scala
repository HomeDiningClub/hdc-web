package controllers

import play.api.mvc.{Action, Controller}

class RouteController extends Controller {

  def removeTrailingSlash(path: String) = Action {
    MovedPermanently("/" + path)
  }
}

