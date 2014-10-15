package services

import java.util.Date
import models.viewmodels.EmailAndName
import org.springframework.stereotype.Service
import play.api.Play.current
import com.typesafe.plugin._
import play.api.Logger
import play.api.i18n.Messages

import scala.collection.mutable.ListBuffer

@Service
class MailService {
  //private final val INSTANCE: MailService = new MailService

//  private def MailService() {
//
//  }

//  def getInstance: MailService = {
//    INSTANCE
//  }

  def createMail(subject: String, message: String, recipients: List[EmailAndName], bbc: List[EmailAndName], from: EmailAndName, replyTo: EmailAndName): MailerAPI = {
    val mail = use[MailerPlugin].email
//    val recipientsList: List[String] = null
//    val bbcList: List[String] = null
    var repList : ListBuffer[String] = ListBuffer()

    mail.setSubject(subject)

    if (!recipients.isEmpty) {
      recipients.foreach{r =>
//        recipientsList + buildNameAndEmailString(r).toString
        repList += buildNameAndEmailString(r).toString
      }
//      mail.setRecipient(recipientsList:_*)
      val recipientsList = repList.result()
      mail.setRecipient(recipientsList:_*)
    }
//    if (!bbc.isEmpty) {
//      bbc.foreach{bbc =>
//        bbcList + buildNameAndEmailString(bbc).toString
//      }
//      mail.setBcc(bbcList:_*)
//    }

    mail.setFrom(buildNameAndEmailString(from).toString)
    mail.setReplyTo(buildNameAndEmailString(replyTo).toString)
    mail.sendHtml("<html>" + message + "</html>" )

    mail
  }

  def createMail(subject: String, message: String, recipient: EmailAndName, bbc: EmailAndName, from: EmailAndName, replyTo: EmailAndName) : MailerAPI = {
    val mail = use[MailerPlugin].email


    mail.setSubject(subject)
    mail.setRecipient(buildNameAndEmailString(recipient))
    mail.setFrom(buildNameAndEmailString(from))
    mail.setReplyTo(buildNameAndEmailString(replyTo))
    mail.sendHtml("<html>" + message + "</html>" )

    mail
  }

  def createMailNoReply(subject: String, message: String, recipient: EmailAndName, from: EmailAndName) : MailerAPI = {
    val mail = use[MailerPlugin].email

    val noReply = EmailAndName(
      name = Messages("main.title"),
      email = Messages("mail.text.no.reply")
    )

    mail.setSubject(subject)
    mail.setRecipient(buildNameAndEmailString(recipient))
    mail.setReplyTo(buildNameAndEmailString(noReply))
    mail.setFrom(buildNameAndEmailString(from))
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
//  case class EmailAndName(
//    name: String,
//    email: String
//  )
}
