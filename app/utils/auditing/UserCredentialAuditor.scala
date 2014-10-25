package utils.auditing

import java.util.{Calendar, UUID}
import org.springframework.data.auditing.{CurrentDateTimeProvider, DateTimeProvider}
import org.springframework.data.domain.AuditorAware

class UserCredentialAuditor extends AuditorAware[UUID]{ //with DateTimeProvider {

  // TODO: Get the requests user.objectId, this cannot be done now without extending the handler to bring the implicit request or just overriding it and sending it with parameters
  def getCurrentAuditor: UUID = {
//    Context.current().session().get("uuid")
    //UUID.randomUUID
    null
  }
//
//  def getNow: Calendar = {
//    CurrentDateTimeProvider.INSTANCE.getNow
//  }
}
