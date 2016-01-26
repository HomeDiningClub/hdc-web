package controllers.test

import javax.inject.{Named, Inject}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import customUtils.security.SecureSocialRuntimeEnvironment

//@Named
class PerformanceTestController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment, val messagesApi: MessagesApi) extends Controller with securesocial.core.SecureSocial with I18nSupport{

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