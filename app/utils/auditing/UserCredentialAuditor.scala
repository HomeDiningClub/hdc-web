package utils.auditing

import java.util.{Calendar, UUID}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.{CurrentDateTimeProvider, DateTimeProvider}
import org.springframework.data.domain.AuditorAware
import play.api.mvc.{Session, RequestHeader}
import play.api.Play.current
import play.mvc.Http
import securesocial.core.java.SecureSocial.UserAwareAction
import securesocial.core.{SecureSocial, Authenticator, DefaultAuthenticatorStore}
import org.springframework.data.auditing.AuditingHandler
import org.springframework.data.auditing.IsNewAwareAuditingHandler

class UserCredentialAuditor extends AuditorAware[UUID] with DateTimeProvider {

  override def getCurrentAuditor: UUID = {
  //    Authenticator.find(id = )
//    Http.Context.current().args.get(SecureSocial.USER_KEY, request.user.getOrNull)
//    val test = SecureSocial.authenticatorFromRequest
    UUID.randomUUID()
  }

//  def testUser = UserAwareAction { implicit request =>
//    Ok(views.html.campaign.index())
//  }
  def getNow: Calendar = {
    CurrentDateTimeProvider.INSTANCE.getNow
  }
}
