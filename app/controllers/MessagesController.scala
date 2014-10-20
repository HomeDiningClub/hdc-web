package controllers

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, UUID, Date}

import com.typesafe.plugin.MailerAPI
import enums.RoleEnums
import models.message.{Message}
import models.{UserProfile, UserCredential}
import models.viewmodels.{MessageForm, EmailAndName}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.Play
import play.api.data.{Mapping, Form}
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.{RequestHeader, Controller}
import securesocial.core.SecureSocial
import services.{MailService, MessageService, UserCredentialService}
import utils.authorization.WithRole
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
 * Created by Tommy on 30/09/2014.
 */
class MessagesController extends  Controller { }

@SpringController
object MessagesController extends  Controller with SecureSocial {

  @Autowired
  private var userCredentialService : UserCredentialService = _

  @Autowired
  private var messageService: MessageService = _

  @Autowired
  private var mailService: MailService = _

  // TEST
  val messageFormMapping = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "phone" -> nonEmptyText,
      "memberId" -> optional(text),
      "hostId" -> optional(text),
      "date" -> date("yyyy-MM-dd"),
      "time" -> date("HH:mm"),
      "numberOfGuests" -> number,
      "request" -> optional(text),
      "response" -> optional(text),
      "messageType" -> optional(text),
      "createdDate" -> optional(date("yyyy-MM-dd"))
    )(MessageForm.apply)(MessageForm.unapply)
  )

  def markMessageAsRead(messageId : String) = SecuredAction(ajaxCall=true) { implicit request =>
    var currentUser = request.user.asInstanceOf[UserCredential].profiles.asScala.head

    var msg = messageService.findById(UUID.fromString(messageId))

    msg.read = "true"
    messageService.saveMessage(msg)

    Ok("Ok")
  }

  def renderHostReplyForm(message: Message) = { implicit request: RequestHeader =>

    utils.Helpers.getUserFromRequest match {
      case None =>

      case Some(currentUser) =>

        if (message.recipient.equals(currentUser)) {

          val hostReply = MessageForm.apply(message.getOwner().firstName, message.getOwner().lastName, message.phone, Option(message.getOwner().objectId.toString),
            Option(currentUser.objectId.toString), message.date, message.time, message.numberOfGuests, Option(message.request), Option(""), Option(message.`type`),
            Option(message.getCreatedDate))

          views.html.host.replyGuest.render(messageFormMapping.fill(hostReply), message.owner, message.objectId.toString, message, request)
        }

    }
  }

  def replyToGuest = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request =>

    val currentUser = utils.Helpers.getUserFromRequest.get

    messageFormMapping.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.host.hostErrorMsg.render(Messages("rating.add.error"), "error"))
      },
      content => {

        userCredentialService.findById(UUID.fromString(content.memberId.getOrElse(""))) match {
          case None =>

          case Some(receiver) =>

            val msgItr:util.Iterator[Message] = currentUser.getMessages.iterator()

            while (msgItr.hasNext) {
              var msg: Message = msgItr.next()

              if(msg.request.equals(content.request.getOrElse(""))) {

                // save here
                messageService.createResponse(currentUser, receiver, msg, content.response.getOrElse(""), msg.phone)

                val guest = EmailAndName(
                  name = currentUser.firstName() + " " + currentUser.lastName(),
                  email = currentUser.emailAddress
                )

                val hdc = EmailAndName(
                  name = Messages("main.title"),
                  email = Messages("footer.link.mail.text")
                )


                val baseUrl: String = routes.StartPageController.index().absoluteURL(false).dropRight(1)
                val userUrl: String = routes.UserProfileController.viewProfileByName(receiver.firstName).url

                val appUrl: String = " " + currentUser.getFullName + " <a href='" + (baseUrl + userUrl) + "'>länk</a>"

                mailService.createMailNoReply(Messages("main.title") + " Förfrågan", Messages("mail.hdc.text") + appUrl, guest, hdc)

              } else {
                println("###### NOT EQUAL ######")
              }
            }
        }

        Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName))
      }

    )


  }


  def renderHostForm(hostingUser: UserCredential) = { implicit request: RequestHeader =>

    utils.Helpers.getUserFromRequest match {
      case None =>
        views.html.host.hostNotLoggedIn()
      case Some(currentUser) =>

        // Disallow user to apply to themselves
        if(hostingUser.objectId.toString.equalsIgnoreCase(currentUser.objectId.toString)){
          views.html.host.hostErrorMsg.render(Messages("You can´t apply to yourself"), "info")
        }else{

          val today = Calendar.getInstance().getTime()
          val format = new SimpleDateFormat("HH:mm")
          val currentTime = format.parse(format.format(new Date()))

          val host = MessageForm.apply(currentUser.firstName(), currentUser.lastName(), currentUser.getPhone, Option(currentUser.objectId.toString), Option(hostingUser.objectId.toString), new Date(), currentTime, 1, Option(""), Option(""), Option(""), Option(new Date()))

          views.html.host.applyHost.render(messageFormMapping.fill(host), Some(hostingUser), request)
        }
    }
  }


  def applyToHost = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request =>
    val currentUser = utils.Helpers.getUserFromRequest.get

    messageFormMapping.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.host.applyHost(errors, Option(currentUser)))
      },
      content => {

        userCredentialService.findById(UUID.fromString(content.hostId.getOrElse(""))) match {
          case None =>
            BadRequest(views.html.host.hostErrorMsg.render(Messages("rating.add.error"), "error"))
          case Some(hostingUser) => {

            messageService.createRequest(currentUser, hostingUser, content.date, content.time, content.numberOfGuests, content.request.getOrElse(""), content.phone)

            val guest = EmailAndName(
              name = currentUser.firstName() + " " + currentUser.lastName(),
              email = currentUser.emailAddress
            )

            val hdc = EmailAndName(
              name = Messages("main.title"),
              email = Messages("footer.link.mail.text")
            )

            val host = EmailAndName(
              name = hostingUser.firstName() + " " + hostingUser.lastName(),
              email = hostingUser.emailAddress
            )

            val baseUrl: String = routes.StartPageController.index().absoluteURL(false).dropRight(1)
            val userUrl: String = routes.UserProfileController.viewProfileByName(hostingUser.firstName).url

            val appUrl: String = " " + currentUser.getFullName + " <a href='" + (baseUrl + userUrl) + "'>länk</a>"

            mailService.createMailNoReply(Messages("main.title") + " Förfrågan", Messages("mail.hdc.text") + appUrl, host, hdc)

            Redirect(routes.UserProfileController.viewProfileByName(hostingUser.profiles.asScala.head.profileLinkName))
          }
        }

      }

    )


  }

}
