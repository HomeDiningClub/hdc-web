package utils

import play.api.mvc.{Controller, RequestHeader}
import models.UserCredential
import securesocial.core.{Identity, SecureSocial}
import play.api.Logger
import play.api.i18n.Lang
import utils.authorization.WithRole
import enums.RoleEnums

import scala.util.Random


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

  def isUserAdmin(checkUser: Identity): Boolean = {
      new WithRole(RoleEnums.ADMIN).isAuthorized(checkUser) match {
        case true => true
        case false => false
      }
  }

  def isUserAdmin(request: RequestHeader): Boolean = {
    Helpers.getUserFromRequest(request) match {
      case Some(user) => isUserAdmin(user)
      case None => false
    }
  }


  def createRoute(input: String): String = {
    // TODO: Improve language when making routes
    input.toLowerCase.replaceAll("å", "a").replaceAll("ä", "a").replaceAll("ö", "o").replaceAll("\\s", "-").replaceAll("\\W_-", "")
  }

  def removeHtmlTags(input: String): String = {
    input.replaceAll("""<(?!\/?a(?=>|\s.*>))\/?.*?>""", "")
  }

  def randomBetween(lowerBound: Int, upperBound: Int): Int = {
    Random.nextInt(upperBound - lowerBound) + lowerBound
  }
}
