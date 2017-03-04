package controllers.test

import javax.inject.Inject

import customUtils.security.SecureSocialRuntimeEnvironment
import models.UserCredential
import org.joda.time.DateTime
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import securesocial.core.SecureSocial

import scala.collection.JavaConverters._

class SecureTestController @Inject() (implicit val env: SecureSocialRuntimeEnvironment,
                                      val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  def testAction = Action { implicit request =>

    var host = request.remoteAddress
    var keys = request.headers.keys.iterator
    var str: String = ""

    while (keys.hasNext) {
      var obj = keys.next()
      str = str + "\n key = " + obj + " value= " + request.headers.apply(obj)
    }

    str = str + "\n Remote host : " + host

    Ok("Datum: " + DateTime.now() + str)
  }



  // SecuredAction
  // UserAwareAction
  // Action
  def testAction2(callingString: String): Action[AnyContent] = UserAwareAction() { implicit request =>

    val user: Option[UserCredential] = request.user
    var response = ""

    val hasAccess = user match {
      case Some(u) => true
      case None => false
    }

    val loggedIdUserProfile: Option[models.UserProfile] = user match {
      case Some(u) => Some(u.profiles.asScala.head)
      case None => None
    }

    if (hasAccess) {
      response = loggedIdUserProfile.get.profileLinkName
    } else {
      response = "NO_USER_LOGGED_IN"
    }

    Ok(response)
  }

}
