package utils

import play.api.mvc.{Controller, RequestHeader}
import models.UserCredential
import securesocial.core.{Identity, SecureSocial}
import play.api.{DefaultGlobal, Logger}
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

  def startPerfLog(): Long = {
    System.currentTimeMillis
  }

  def endPerfLog(name: String, startTime: Long): Unit ={
    val endTime = System.currentTimeMillis
    val requestTime = endTime - startTime
    Logger.info(name + ":" + requestTime.toString)
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


  def limitLength(input: String, limitLength: Int): String = {
    if(input == null)
      null

    if(limitLength == null)
      input

    if(input.length >= limitLength)
      input.substring(0,limitLength)
    else
      input
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


  def titleCase(s: String) = {
    if (s != null && s.length > 0) {
      s.head.toUpper + s.tail.toLowerCase
    }else{
      s
    }
  }
}
