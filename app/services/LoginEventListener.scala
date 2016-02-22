package services

import play.api.mvc.{ Session, RequestHeader }
import play.api.Logger
import securesocial.core._

class LoginEventListener extends EventListener {

  def onEvent[U](event: Event[U], request: RequestHeader, session: Session): Option[Session] = {
    val eventName = event match {
      case LoginEvent(u) => "login"
      case LogoutEvent(u) => "logout"
      case SignUpEvent(u) => "signup"
      case PasswordResetEvent(u) => "password reset"
      case PasswordChangeEvent(u) => "password change"
    }

    event.user match {
      case u: models.UserCredential => Logger.info("Traced %s event for usercredential %s".format(eventName, u.emailAddress))
      case u: BasicProfile => Logger.info("Traced %s event for basicprofile %s".format(eventName, u.email.getOrElse("No email found")))
      case any => Logger.info("Traced %s event, but unknown user %s".format(eventName, any.getClass))
    }

    // retrieving the current language
    Logger.info("Current language is %s".format(request2lang(request)))

    // Not changing the session so just return None
    // if you wanted to change the session then you'd do something like
    // Some(session + ("your_key" -> "your_value"))
    None
  }

}
