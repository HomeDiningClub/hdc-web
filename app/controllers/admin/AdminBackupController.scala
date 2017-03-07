package controllers.admin

import javax.inject.{Inject, Named}

import org.springframework.stereotype.{Controller => SpringController}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller, RequestHeader}
import customUtils.authorization.WithRole
import enums.RoleEnums
import models.UserCredential
import org.springframework.beans.factory.annotation.Autowired
import customUtils.security.SecureSocialRuntimeEnvironment
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest

//@Named
class AdminBackupController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                        val messagesApi: MessagesApi,
                                        val configuration: Configuration) extends Controller with SecureSocial with I18nSupport {

  def listAllBackupJobs: Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.backup.index())
  }

  def doBackup(): Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    customUtils.backup.BackupData.makeFullBackup(configuration)
    Ok("Done!")
  }
}
