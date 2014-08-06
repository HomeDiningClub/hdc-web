package controllers

import play.api._
import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import utils.authorization.WithRole
import enums.RoleEnums

@SpringController
class EditController extends Controller with securesocial.core.SecureSocial {

  def index = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.edit.index())
  }

}