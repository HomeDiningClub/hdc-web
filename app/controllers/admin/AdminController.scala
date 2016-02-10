package controllers.admin

import javax.inject.{Named, Inject}

import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, RequestHeader, Controller}
import customUtils.authorization.WithRole
import enums.RoleEnums
import models.UserCredential
import org.springframework.beans.factory.annotation.Autowired
import customUtils.security.SecureSocialRuntimeEnvironment
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest

class AdminController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                 val messagesApi: MessagesApi,
                                 val adminStatisticsController: AdminStatisticsController) extends Controller with SecureSocial with I18nSupport {

  def index = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.index(Some(adminStatisticsController.getStatBox)))
  }

}