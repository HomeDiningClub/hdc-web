package controllers.admin

import javax.inject.{Inject, Named}

import models.formdata.{TagCheckboxForm, TagListForm, UserProfileDataForm, UserProfileOptionsForm}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.{I18nSupport, MessagesApi}
import securesocial.core.SecureSocial
import services.{CountyService, TagWordService, UserProfileService}
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
      "emailAddress2" -> text,
      "options" -> mapping(
        "payCash" -> boolean,
        "paySwish" -> boolean,
        "payBankCard" -> boolean,
        "payIZettle" -> boolean,
        "wantsToBeHost" -> boolean
      )(UserProfileOptionsForm.apply)(UserProfileOptionsForm.unapply)
    )
    (UserProfileDataForm.apply) (UserProfileDataForm.unapply)
  )

  val TagsForm = Form(
    mapping(
      "tagList" -> optional(list[TagCheckboxForm]{
        mapping(
          "value" -> text
        )(TagCheckboxForm.apply)(TagCheckboxForm.unapply)
      })
    )(TagListForm.apply) (TagListForm.unapply)
  )

}
