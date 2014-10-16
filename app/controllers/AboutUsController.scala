package controllers

import com.typesafe.plugin.MailerAPI
import enums.RoleEnums
import models.viewmodels.{EmailAndName, AboutUsForm}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc._
import services.MailService
import utils.authorization.WithRole
import scala.collection.JavaConverters._


/**
 * Created by Tommy on 29/09/2014.
 */
@SpringController
class AboutUsController extends Controller with  securesocial.core.SecureSocial {

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
    //      verifying ("subject får inte vara tomt", f => Option(f.subject).getOrElse("").isEmpty )
    //      verifying ("message får inte vara tomt", f => Option(f.message).getOrElse("").isEmpty )
  )

  def aboutUs = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>
    val currentUser = utils.Helpers.getUserFromRequest.get


    val mailViewForm = AboutUsForm.apply(
      Some(""),
      Messages("footer.link.mail.text"),
      (currentUser.firstName() + " " + currentUser.lastName()),
      "",
      ""
    )
// views.html.host.replyGuest.render(messageFormMapping.fill(hostReply), message.owner, message.objectId.toString, message, request)
    Ok(views.html.about.about.apply(mailForm.fill(mailViewForm)))
  }

  def suggestFeatures = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>
    val currentUser = utils.Helpers.getUserFromRequest.get

    var subject: String = ""
    var msg: String = ""

    mailForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("Subject") + " - " + Messages("No message")
        BadRequest(views.html.about.about(errors))
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

        mailService.createMailNoReply(subject, msg, recipient, from)

        Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName))
      }
    )
  }
}
