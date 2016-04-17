package customUtils

import java.text.SimpleDateFormat
import java.util.{UUID, Date}
import javax.inject.{Named, Inject}

import models.base.AuditEntity
import play.api.mvc.RequestHeader
import models.UserCredential
import play.api.Logger
import customUtils.authorization.{IsAuthorizedChecks, WithRole}
import enums.RoleEnums
import scala.util.Random
import org.joda.time.DateTime
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._
import org.springframework.beans.factory.annotation.Autowired
import customUtils.security.SecureSocialRuntimeEnvironment
import org.springframework.stereotype.{Controller => SpringController}
import scala.language.postfixOps
import scala.util.matching.Regex

//@Named
object Helpers {

  /*
  @Inject implicit var env: SecureSocialRuntimeEnvironment = null
*/
  /*
  def getUserFromRequest(req: RequestHeader): Option[UserCredential] = {
    implicit val request: RequestHeader = req
    implicit def executionContext: ExecutionContext = env.executionContext

    val futureUser = securesocial.core.SecureSocial.currentUser

    Await.result(futureUser, 500 millis) match {
      case Some(user:UserCredential) => Some(user)
      case None => None
    }
  }
*/

/*  def currentUser = Action.async { implicit request =>
    SecureSocial.currentUser[DemoUser].map { maybeUser =>
      val userId = maybeUser.map(_.main.userId).getOrElse("unknown")
      Ok(s"Your id is $userId")
    }
  }*/

  def startPerfLog(): Long = {
    System.currentTimeMillis
  }

  def endPerfLog(name: String, startTime: Long): Unit ={
    val endTime = System.currentTimeMillis
    val requestTime = endTime - startTime
    Logger.info(name + ":" + requestTime.toString)
  }

  def isUserAdmin(checkUser: UserCredential, req: RequestHeader): Boolean = {
      new WithRole(RoleEnums.ADMIN).isAuthorized(checkUser, req) match {
        case true => true
        case false => false
      }
  }

  def isUserAdmin(checkUser: Option[UserCredential]): Boolean = {
    checkUser match {
      case Some(user) => isUserAdmin(user)
      case None => false
    }
  }

  def isUserAdmin(checkUser: UserCredential): Boolean = {
    IsAuthorizedChecks.ValidateWithRole(checkUser, RoleEnums.ADMIN)
  }

  /*
  def isUserAdmin(request: RequestHeader): Boolean = {
    Helpers.getUserFromRequest(request) match {
      case Some(user) => isUserAdmin(user)
      case None => false
    }
  }
  */

  def isValidUuid(uuid: String): Boolean = {
    if(uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")){
      true
    }else{
      false
    }
  }

  def isValidTime(time: String): Boolean = {
    if(time.matches("[0-9]{2}:[0-9]{2}")){
      true
    }else{
      false
    }
  }


  def getDateForSharing(auditEntry: AuditEntity): String = {
    auditEntry.getLastModifiedDate match {
      case modDate: Date => formatDateForSharing(modDate)
      case _ => formatDateForSharing(auditEntry.getCreatedDate)
    }
  }

  def formatDateForSharing(date: java.util.Date): String = {
    new SimpleDateFormat("yyyyMMddHHmmss").format(date)
  }

  def formatDateForDisplay(date: java.util.Date): String = {
    new SimpleDateFormat("yyyy-MM-dd - HH:mm").format(date)
  }

  def formatDateForDisplay(date: DateTime): String = {
    date.toString("YYYY-MM-dd - HH:mm")
  }

  def formatDate(date: java.util.Date, format: String) = {
    new SimpleDateFormat(format).format(date)
  }


  def limitLength(input: String, limitLength: Int): String = {
    if(input == null)
      null

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

  def getCurrentDateTime: java.util.Date = {
    DateTime.now().toDate
  }

  def titleCase(s: String) = {
    if (s != null && s.length > 0) {
      s.head.toUpper + s.tail.toLowerCase
    }else{
      s
    }
  }
}
