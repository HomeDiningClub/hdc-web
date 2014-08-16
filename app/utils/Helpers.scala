package utils

import play.api.mvc.RequestHeader
import models.UserCredential
import securesocial.core.SecureSocial
import play.api.Logger


object Helpers {

  implicit def getUserFromRequest(implicit request: RequestHeader): Option[UserCredential] = {
    val user = SecureSocial.currentUser.map {
      u =>
        Logger.debug("Found user:" + u.fullName)
        u.asInstanceOf[UserCredential]
    }

    if(user.isDefined)
      user
    else
      None
  }

}
