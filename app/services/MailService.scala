package services

import javax.inject.{Named, Inject}
import models.viewmodels.{EmailAndName}
import org.springframework.stereotype.Service
import play.api.Logger
import play.api.i18n.{MessagesApi, I18nSupport, Messages}
import play.api.libs.mailer.MailerClient
import play.api.libs.mailer._

//@Named
//@Service
class MailService @Inject() (mailer: MailerClient,
                             val messagesApi: MessagesApi) extends I18nSupport {

  def createMail(subject: String, message: String, recipients: List[EmailAndName], bcc: Option[List[EmailAndName]], from: EmailAndName, replyTo: EmailAndName): Email = {

    val email = Email(
      subject = subject,
      from = buildNameAndEmailString(from).toString,
      to = buildRecipientsList(recipients),
      replyTo = Some(buildNameAndEmailString(replyTo).toString),
      bodyHtml = Some("<html>" + message + "</html>"),
      bcc = bcc match {
        case Some(items) =>
          buildRecipientsList(items)
        case None => Seq.empty
      }
    )
    email
  }

  def createAndSendMailNoReply(subject: String, message: String, recipient: EmailAndName, from: EmailAndName): Email = {



    val email = createMail(
      subject = subject,
      recipients = List(recipient),
      message = message,
      bcc = None,
      from = from,
      replyTo = getDefaultAnonSender
    )
    sendMail(email)
    email
  }

  def sendMail(email:Email): String = {
    mailer.send(email)
  }

  def getDefaultAnonSender: EmailAndName = {
    EmailAndName(
      name = Messages("main.title"),
      email = Messages("mail.text.no.reply")
    )
  }

  private def buildRecipientsList(recipients: List[EmailAndName]): Seq[String] = {
    if (recipients.nonEmpty){
      recipients.map {
        r: EmailAndName =>
          buildNameAndEmailString(r)
      }
    }else{
      Seq.empty
    }
  }

  private def buildNameAndEmailString(emailObject: EmailAndName): String = {
    val returnStr: String = emailObject.name + "<" + emailObject.email + ">"
    returnStr
  }

}
