package controllers

import javax.inject.Inject

import customUtils.security.SecureSocialRuntimeEnvironment
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.mvc.Controller
import securesocial.core.SecureSocial

class CampaignController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                    val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  def index = Action { implicit request =>
    Ok(views.html.campaign.index())
  }

}
