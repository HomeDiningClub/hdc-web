package controllers.test

import javax.inject.{Inject, Named}

import play.api.{Environment, Logger}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import customUtils.security.SecureSocialRuntimeEnvironment
import play.api.libs.ws.WS
import securesocial.core.SecureSocial

import scala.concurrent.Future
import play.api.Play.current

import scala.collection.mutable.ListBuffer

class PerformanceTestController @Inject() (implicit val env: SecureSocialRuntimeEnvironment,
                                           val messagesApi: MessagesApi,
                                           val environment: Environment) extends Controller with SecureSocial with I18nSupport{
  def asyncAction = Action.async { request =>
    val futureJsUserArray = WS.url("http://www.json-generator.com/api/json/get/cfLxEnRoAy?indent=2").get()
    futureJsUserArray.map{jsResponse => Ok(jsResponse.body).as("application/json")}
  }

  def index2 = UserAwareAction { implicit request =>

    var a = 0
    var testList: ListBuffer[String] = ListBuffer[String]()
    var startTime = System.currentTimeMillis
    var endTime = System.currentTimeMillis
    var requestTime = endTime - startTime
    var result: String = ""

    //testList += controllers.routes.UserProfileController.viewProfileByName("magnus-profil").absoluteURL()
    //testList += controllers.routes.UserProfileController.viewProfileByName("Erikatettglasvin").absoluteURL()
    //testList += controllers.routes.UserProfileController.viewProfileByName("LCHFInspiration").absoluteURL()
    //testList += controllers.routes.UserProfileController.viewProfileByName("mazo").absoluteURL()
    testList += controllers.routes.UserProfileController.viewProfileByName("EvelinneSundberg").absoluteURL()
    //testList += controllers.routes.UserProfileController.viewProfileByName("kimsilvasti").absoluteURL()

    testList.toList

    for(a <- 1 to 2){

      startTime = System.currentTimeMillis

      for(url: String <- testList){
        val futureJsUserArray = WS.url(url).get()
        futureJsUserArray.map{jsResponse => result += jsResponse.body}
      }

      endTime = System.currentTimeMillis
      requestTime = endTime - startTime
      Logger.info(requestTime.toString)
    }


    Ok(result)
  }

  def index = UserAwareAction { implicit request =>

    var a = 0
    var result: String = ""
    var startTime = System.currentTimeMillis
    var endTime = System.currentTimeMillis
    var requestTime = endTime - startTime

    for(a <- 1 to 10){
      startTime = System.currentTimeMillis
      result += controllers.routes.UserProfileController.viewProfileByName("magnus-profil").url
      result += controllers.routes.UserProfileController.viewProfileByName("Erikatettglasvin").url
      result += controllers.routes.UserProfileController.viewProfileByName("LCHFInspiration").url
      result += controllers.routes.UserProfileController.viewProfileByName("LCHFInspiration").url
      result += controllers.routes.UserProfileController.viewProfileByName("mazo").url
      result += controllers.routes.UserProfileController.viewProfileByName("EvelinneSundberg").url
      result += controllers.routes.UserProfileController.viewProfileByName("EvelinneSundberg").url
      result += controllers.routes.UserProfileController.viewProfileByName("kimsilvasti").url
      result += controllers.routes.UserProfileController.viewProfileByName("magnus-profil").url
      result += controllers.routes.UserProfileController.viewProfileByName("magnus-profil_1").url
      result += controllers.routes.UserProfileController.viewProfileByName("magnus-profil_2").url
      result += controllers.routes.UserProfileController.viewProfileByName("magnus-profil_3").url
      result += controllers.routes.UserProfileController.viewProfileByName("magnus-profil_4").url
      endTime = System.currentTimeMillis
      requestTime = endTime - startTime
      Logger.info(requestTime.toString)
    }


    Ok(result)
  }
}