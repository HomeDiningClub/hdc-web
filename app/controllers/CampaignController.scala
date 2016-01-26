package controllers

import javax.inject.{Named, Inject}

import customUtils.security.SecureSocialRuntimeEnvironment
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import securesocial.core.SecureSocial
import traits.ProvidesAppContext

//@Named
class CampaignController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment) extends Controller with SecureSocial with ProvidesAppContext {

  def index = Action { implicit request =>
    Ok(views.html.campaign.index())
  }

}
