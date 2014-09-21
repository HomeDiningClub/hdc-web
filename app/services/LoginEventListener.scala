package services

import models.UserCredential
import securesocial.core._
import play.api.mvc.{Session, RequestHeader}
import play.api.{Application, Logger}

class LoginEventListener(app: Application) extends EventListener {
  override def id: String = "LoginEventListener"

  def onEvent(event: Event, request: RequestHeader, session: Session): Option[Session] = {
    val eventName = event match {
      case e: LoginEvent => {
//        val loggedInUser = event.user.asInstanceOf[UserCredential]
//        Some(session.+("id",loggedInUser.objectId.toString))
//
//        session("uuid",loggedInUser.objectId.toString)
        "login"
      }
      case e: LogoutEvent => {
//        Option(Session.deserialize(Map[String,String]()))
        "logout"
      }
      case e: SignUpEvent => "signup"
      case e: PasswordResetEvent => "password reset"
      case e: PasswordChangeEvent => "password change"
    }

    Logger.info("traced %s event for user %s".format(eventName, event.user.fullName))
 
    // retrieving the current language
   // Logger.info("current language is %s".format(lang(request)))

    // Not changing the session so just return None
    // if you wanted to change the session then you'd do something like
    // Some(session + ("your_key" -> "your_value"))
    None
  }
}
