package controllers.admin

import _root_.controllers.EnvData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import securesocial.core.{SecuredRequest, SecureSocial}
import services.{CountyService, UserProfileService, TagWordService}
import play.api.data._
import play.api.data.Forms._
import play.api._
import play.api.mvc._

@SpringController
class AdminUserProfileController  extends Controller with SecureSocial {

  // Services
  @Autowired
  var userProfileService: UserProfileService = _

  @Autowired
  var tagWordService : TagWordService = _

  @Autowired
  var countyService : CountyService = _

  // Form
  val userProfileForm : play.api.data.Form[models.formdata.UserProfileForm]  = play.api.data.Form(
    mapping(
      "userName" -> text,
      "emailAddress" -> email,
      "firstName" -> text,
      "lastName" -> text,
      "aboutme" -> text,
     // "quality" -> list(boolean),
      "county" -> text,
      "streetAddress" -> text,
      "zipCode" -> text,
      "city" -> text,
      "phoneNumber" -> text,
      "idno" -> longNumber
    )(models.formdata.UserProfileForm.apply)(models.formdata.UserProfileForm.unapply)
  )

  val AnvandareForm = Form(
    mapping(
      "name" -> text,
      "name2" -> text,
    //  "quality" -> list(text),
      "aboutmeheadline" -> text,
      "aboutme" -> text,
      "county" -> text,
      "streetAddress" -> text,
      "zipCode" -> text,
      "city" -> text,
      "phoneNumber" -> text,
      "personnummer" -> text,
      "acceptTerms"  -> boolean,
      // "childFfriendly" -> optional(text),
      // "handicapFriendly" -> optional(text),
      // "havePets" -> optional(text),
      // "smoke" -> optional(text),
      "allkoholServing" -> optional(text),
      "mainimage" -> optional(text),
      "avatarimage" -> optional(text)
    )
    (EnvData.apply) (EnvData.unapply)
  )

}
