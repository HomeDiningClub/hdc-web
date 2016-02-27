package controllers

import javax.inject.{Named, Inject}

import constants.FlashMsgConstants
import enums.RoleEnums
import models.viewmodels.EmailAndName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import play.api.mvc.{AnyContent, Flash, RequestHeader, Controller}
import play.twirl.api.Html
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.{ContentService, MailService}
import customUtils.authorization.WithRole
import scala.collection.JavaConverters._
import models.UserCredential
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.AboutUsForm

class SuggestController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                   val messagesApi: MessagesApi,
                                   val mailService: MailService) extends Controller with SecureSocial with I18nSupport {

  /*
  @Autowired
  private var mailService: MailService = _
*/

  val mailForm = Form(
    mapping(
      "id" -> optional(text),
      "to" -> text,
      "name" -> text,
      "subject" -> nonEmptyText,
      "message" -> nonEmptyText
    )(AboutUsForm.apply)(AboutUsForm.unapply)
  )

  def suggestForm = UserAwareAction { implicit request =>
    request.user match {
      case None =>
        Ok(views.html.about.aboutNotLoggedIn())
      case Some(user) => {
        val mailViewForm = AboutUsForm.apply(
          Some(""),
          Messages("footer.link.mail.text"),
          user.firstName + " " + user.lastName,
          "",
          ""
        )
        Ok(views.html.about.about.render(mailForm.fill(mailViewForm), Flash.emptyCookie, request2Messages))
      }
    }

  }

  def suggestFeatures = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val currentUser = request.user

    var subject: String = ""
    var msg: String = ""

    mailForm.bindFromRequest.fold(
      errors => {

        request.headers.get("Referer") match {
          case Some(referrerUrl) =>

            val subject_error = errors.error("subject")
            val message_error = errors.error("message")

            var errorMsg: String = ""

            if (subject_error.nonEmpty && message_error.nonEmpty) {
              errorMsg = Messages("mails.features.incomplete")
            } else if(subject_error.nonEmpty) {
              errorMsg = Messages("mails.features.incomplete.subject")
            } else {
              errorMsg = Messages("mails.features.incomplete.message")
            }


            Redirect(referrerUrl, 302).flashing(FlashMsgConstants.Error -> errorMsg)
          case None =>
            Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName)).flashing(FlashMsgConstants.Error -> Messages("rating.add.error"))
        }

      },
      contentData => {

        if (!Option(contentData.subject).getOrElse("").isEmpty)
          subject = contentData.subject

        if (!Option(contentData.message).getOrElse("").isEmpty)
          msg = contentData.message

        val from = EmailAndName(
          name = currentUser.firstName + " " + currentUser.lastName,
          email = currentUser.email.getOrElse("")
        )

        val recipient = EmailAndName(
          name = Messages("main.title"),
          email = Messages("footer.link.mail.text")
        )

        mailService.createAndSendMailNoReply(
          subject = subject,
          message = msg,
          recipient = recipient,
          from = from)

        Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName))
      }
    )
  }
}
