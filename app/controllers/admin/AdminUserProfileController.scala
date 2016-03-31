package controllers.admin

import javax.inject.{Named, Inject}

import models.formdata.UserProfileDataForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.{I18nSupport, MessagesApi}
import securesocial.core.SecureSocial
import services.{CountyService, UserProfileService, TagWordService}
import play.api.data._
import play.api.data.Forms._
import play.api._
import play.api.mvc._
import models.UserCredential
import customUtils.security.SecureSocialRuntimeEnvironment

class AdminUserProfileController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                            val userProfileService: UserProfileService,
                                            val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport{

  val AnvandareForm = Form(
    mapping(
      "name" -> text,
      "name2" -> text,
      "aboutmeheadline" -> text,
      "aboutme" -> text,
      "county" -> text,
      "streetAddress" -> text,
      "zipCode" -> text,
      "city" -> text,
      "phoneNumber" -> text,
      "personnummer" -> text,
      "acceptTerms"  -> boolean,
      "mainimage" -> optional(text),
      "avatarimage" -> optional(text),
      "firstName" -> text,
      "lastName" -> text,
      "emailAddress" -> text,
     "emailAddress2" -> text
    )
    (UserProfileDataForm.apply) (UserProfileDataForm.unapply)
  )

}
