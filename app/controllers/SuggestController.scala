package controllers

import constants.FlashMsgConstants
import enums.RoleEnums
import models.content.ContentPage
import models.viewmodels.{EmailAndName, AboutUsForm}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.{RequestHeader, Controller}
import securesocial.core.SecureSocial
import services.{ContentService, MailService}
import utils.authorization.WithRole
import scala.collection.JavaConverters._

/**
 * Created by Tommy on 17/10/2014.
 */
@SpringController
class SuggestController extends Controller with SecureSocial { }

@SpringController
object SuggestController extends Controller with SecureSocial {

  @Autowired
  private var contentService: ContentService = _

  @Autowired
  private var mailService: MailService = _

  val mailForm = Form(
    mapping(
      "id" -> optional(text),
      "to" -> text,
      "name" -> text,
      "subject" -> nonEmptyText,
      "message" -> nonEmptyText
    )(AboutUsForm.apply)(AboutUsForm.unapply)
  )

  def aboutUs = { implicit request: RequestHeader =>
    val currentUser = utils.Helpers.getUserFromRequest
    currentUser match {
      case None =>
        views.html.about.aboutNotLoggedIn() // No user, return nothing or something.
      case Some(user) => {
        val mailViewForm = AboutUsForm.apply(
          Some(""),
          Messages("footer.link.mail.text"),
          (user.firstName() + " " + user.lastName()),
          "",
          ""
        )

        views.html.about.about.render(mailForm.fill(mailViewForm),request, flash)
      }
    }

  }

  def suggestFeatures = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request =>
    val currentUser = utils.Helpers.getUserFromRequest.get

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
              errorMsg = ": Ämne och Meddelande"
            } else if(subject_error.nonEmpty) {
              errorMsg = ": Ämne"
            } else {
              errorMsg = ": Meddelande"
            }


            Redirect(referrerUrl, 302).flashing(FlashMsgConstants.Error -> (Messages("required field") + errorMsg))
          case None =>
            Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName)).flashing(FlashMsgConstants.Error -> Messages("rating.add.error"))
        }



//        val errorMessage = Messages("Subject") + " - " + Messages("No message")
//        Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName))
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

//        mailService.createMailNoReply(subject, msg, recipient, from)

        Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName))
      }
    )
  }
}
