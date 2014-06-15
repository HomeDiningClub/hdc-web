package controllers

import play.api._
import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller

@SpringController
class EditController extends Controller with securesocial.core.SecureSocial {

  def index = SecuredAction {
    Ok(views.html.edit.index())
  }

}