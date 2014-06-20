package controllers

import play.api.mvc.{Controller, Action}


object TestBild extends Controller {


  def testsida = Action {
    Ok("OK")
  }

}
