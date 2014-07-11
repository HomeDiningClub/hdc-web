package services

import java.util.Date
import play.api.Play.current
import com.typesafe.plugin._
import play.api.Logger

class MailService {
  private final val INSTANCE: MailService = new MailService

  private def MailService() {

  }

  def getInstance: MailService = {
    INSTANCE
  }

  def createMail(subject: String, message: String, recipients: List[EmailAndName], bbc: List[EmailAndName], from: EmailAndName, replyTo: EmailAndName): MailerAPI = {
    val mail = use[MailerPlugin].email
    val recipientsList: List[String] = null
    val bbcList: List[String] = null

    mail.setSubject(subject)

    if (!recipients.isEmpty) {
      recipients.foreach{r =>
        recipientsList + buildNameAndEmailString(r).toString
      }
      mail.setRecipient(recipientsList:_*)
    }
    if (!bbc.isEmpty) {
      bbc.foreach{bbc =>
        bbcList + buildNameAndEmailString(bbc).toString
      }
      mail.setBcc(bbcList:_*)
    }

    mail.setFrom(buildNameAndEmailString(from).toString)
    mail.setReplyTo(buildNameAndEmailString(replyTo).toString)
    mail.sendHtml("<html>" + message + "</html>" )

    mail
  }

  def sendMail(mailToSend: MailerAPI) {
    val nowDate: Date = new Date
      mailToSend.send("text")
      Logger.debug("Mail sent at:" + nowDate)

  }

  def buildNameAndEmailString(emailObject: EmailAndName): String = {
    val returnStr: String = emailObject.name + "<" + emailObject.email + ">"
    returnStr
  }
  case class EmailAndName(
    name: String,
    email: String
  )
}
