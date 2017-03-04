package services

import javax.inject.Inject

import models.viewmodels.EmailAndName
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.mailer._


class MailService @Inject() (mailer: MailerClient,
                             val messagesApi: MessagesApi) extends I18nSupport {

  def createMail(subject: String, message: String, recipients: List[EmailAndName], bcc: Option[List[EmailAndName]], from: EmailAndName, replyTo: EmailAndName): Email = {
    Email(
      subject = subject,
      from = buildNameAndEmailString(from).toString,
      to = buildRecipientsList(recipients),
      replyTo = Some(buildNameAndEmailString(replyTo).toString),
      bodyHtml = createMailBody(message),
      //charset = Some("ISO-8859-1"), // Could be used: "ISO-8859-1" or "UTF-8"
      //attachments = if(sendWithWrappingLayout) Seq(AttachmentFile("logo.jpg", new File("public\\images\\mail\\logo.jpg"), contentId = Some("logocid"))) else Seq.empty,
      bcc = bcc match {
        case Some(items) =>
          buildRecipientsList(items)
        case None => Seq.empty
      }
    )
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

  private def createMailBody(message: String): Some[String] = {
    Some(
        "<html>" + message + "</html>"
    )
  }

}
