package controllers

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, UUID, Date}

import com.typesafe.plugin.MailerAPI
import constants.FlashMsgConstants
import controllers.SuggestController._
import enums.RoleEnums
import models.message.{Message}
import models.{UserProfile, UserCredential}
import models.viewmodels.{MessageForm, EmailAndName}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.Play
import play.api.Logger
import play.api.data.{Mapping, Form}
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.{RequestHeader, Controller}
import securesocial.core.SecureSocial
import services.{MailService, MessageService, UserCredentialService}
import utils.authorization.WithRole
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class MessagesController extends  Controller { }

@SpringController
object MessagesController extends Controller with SecureSocial {

  @Autowired
  private var userCredentialService : UserCredentialService = _

  @Autowired
  private var messageService: MessageService = _

  @Autowired
  private var mailService: MailService = _

  val messageFormMapping = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "phone" -> optional(text),
      "memberId" -> optional(text),
      "hostId" -> optional(text),
      "date" -> date("yyyy-MM-dd"),
      "time" -> date("HH:mm"),
      "numberOfGuests" -> number,
      "request" -> optional(text),
      "response" -> optional(text),
      "messageType" -> optional(text),
      "createdDate" -> optional(date("yyyy-MM-dd")),
      "messageId" -> optional(text),
      "profileLinkName" -> nonEmptyText
    )(MessageForm.apply)(MessageForm.unapply)
  )

  def markMessageAsRead(messageId : String) = SecuredAction(ajaxCall=true) { implicit request =>
    val currentUser = utils.Helpers.getUserFromRequest

    if(currentUser.nonEmpty){
      messageService.findById(UUID.fromString(messageId)) match {
        case None =>
          Ok("No Message found")
        case Some(msg) =>
          if(msg.getRecipient.objectId == currentUser.get.objectId){
            msg.read = true
            messageService.saveMessage(msg)
            Ok("Ok")
          }else{
            Ok("User tried to read someone else's messages")
          }
      }
    }else{
      Ok("No logged in user")
    }
  }

  def renderMessage(message: Message) = { implicit request: RequestHeader =>

    utils.Helpers.getUserFromRequest match {
      case None =>

      case Some(currentUser) =>

        if (message.getRecipient != null && message.getRecipient.equals(currentUser)) {
          val hostReply = MessageForm.apply(message.getOwner().firstName, message.getOwner().lastName, Option(message.phone), Option(message.getOwner().objectId.toString),
            Option(currentUser.objectId.toString), message.date, message.time, message.numberOfGuests, Option(message.request), Option(""), Option(message.`type`),
            Option(message.getCreatedDate), Option(message.objectId.toString),message.getOwner().profiles.iterator().next().profileLinkName)

          views.html.host.replyGuest.render(messageFormMapping.fill(hostReply), message.getOwner(), message.objectId.toString, message, request)
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

        if(content.response.isEmpty) {
            Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName) + "#inbox-tab#" + content.messageId).flashing(FlashMsgConstants.Error -> Messages("mails.error.no.reply"))
        } else {

          userCredentialService.findById(UUID.fromString(content.memberId.getOrElse(""))) match {
            case None =>

            case Some(receiver) =>

              val msgItr = messageService.findIncomingMessagesForUser(currentUser)

              if(msgItr.nonEmpty){
                for(msg: Message <- msgItr.get){
                //while (msgItr.hasNext) {
                  //var msg: Message = msgItr.next()

                  if(msg.request.equals(content.request.getOrElse(""))) {

                    // save here
                    messageService.createResponse(currentUser, receiver, msg, content.response.getOrElse(""), msg.phone)

                    val guest = EmailAndName(
                      name = receiver.firstName() + " " + receiver.lastName(),
                      email = receiver.emailAddress
                    )

                    val hdc = EmailAndName(
                      name = Messages("main.title"),
                      email = Messages("footer.link.mail.text")
                    )


                    val baseUrl: String = routes.StartPageController.index().absoluteURL(false).dropRight(1)
                    val userUrl: String = routes.UserProfileController.viewProfileByName(receiver.profiles.asScala.head.profileLinkName).url + "#inbox-tab"

                    val appUrl: String = " " + currentUser.getFullName + " <a href='" + (baseUrl + userUrl) + "'>" + Messages("mails.hosting.mail.link-text") + "</a>"

                    mailService.createMailNoReply(Messages("main.title") + " " + Messages("mails.hosting.mail.title"), Messages("mail.hdc.text") + appUrl, guest, hdc)

                  } else {
                    Logger.debug("Message not equal to request")
                  }
                }
              }
          }

          Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName))

        }

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

          val host = MessageForm.apply(currentUser.firstName(), currentUser.lastName(), Option(currentUser.getPhone), Option(currentUser.objectId.toString), Option(hostingUser.objectId.toString), new Date(), currentTime, 1, Option(""), Option(""), Option(""), Option(new Date()), Option(""), currentUser.profiles.asScala.head.profileLinkName)

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

        if (content.request.isEmpty) {

          userCredentialService.findById(UUID.fromString(content.hostId.getOrElse(""))) match {
            case Some(hostingUser) =>
              Redirect(routes.UserProfileController.viewProfileByName(hostingUser.profiles.asScala.head.profileLinkName)).flashing(FlashMsgConstants.Error -> Messages("mails.error.no.message"))
            case None =>
              BadRequest(views.html.host.hostErrorMsg.render(Messages("mails.error.no.message"), "error"))
          }

        } else {

          userCredentialService.findById(UUID.fromString(content.hostId.getOrElse(""))) match {
            case None =>
              BadRequest(views.html.host.hostErrorMsg.render(Messages("rating.add.error"), "error"))
            case Some(hostingUser) => {

              messageService.createRequest(currentUser, hostingUser, content.date, content.time, content.numberOfGuests, content.request.getOrElse(""), content.phone)

              val guest = EmailAndName(
                name = currentUser.profiles.asScala.head.profileLinkName,
                email = currentUser.emailAddress
              )

              val hdc = EmailAndName(
                name = Messages("main.title"),
                email = Messages("footer.link.mail.text")
              )

              val host = EmailAndName(
                name = hostingUser.profiles.asScala.head.profileLinkName,
                email = hostingUser.emailAddress
              )

              val baseUrl: String = routes.StartPageController.index().absoluteURL(false).dropRight(1)
              val userUrl: String = routes.UserProfileController.viewProfileByName(hostingUser.profiles.asScala.head.profileLinkName).url + "#inbox-tab"

              val appUrl: String = " " + currentUser.profiles.asScala.head.profileLinkName + " <a href='" + (baseUrl + userUrl) + "'>" + Messages("mails.hosting.mail.link-text") + "</a>"

              mailService.createMailNoReply(Messages("main.title") + " " + Messages("mails.hosting.mail.title"), Messages("mail.hdc.text") + appUrl, host, hdc)

              Redirect(routes.UserProfileController.viewProfileByName(hostingUser.profiles.asScala.head.profileLinkName))
            }
          }

        }



      }

    )


  }

}
