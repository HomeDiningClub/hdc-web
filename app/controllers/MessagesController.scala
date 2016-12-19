package controllers

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, UUID}
import javax.inject.{Inject, Named}

import constants.FlashMsgConstants
import enums.RoleEnums
import models.message.{Message, MessageData}
import models.{UserCredential, UserProfile}
import models.viewmodels.{EmailAndName, ReplyToGuestMessage}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Controller, RequestHeader}
import play.twirl.api.Html
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.{MailService, MessageService, NodeEntityService, UserCredentialService}
import customUtils.authorization.WithRole

import scala.collection.JavaConverters._
import customUtils.security.SecureSocialRuntimeEnvironment
import play.api.mvc._
import models.formdata.MessageForm

class MessagesController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                    val messagesApi: MessagesApi,
                                    val userCredentialService: UserCredentialService,
                                    val messageService: MessageService,
                                    implicit val nodeEntityService: NodeEntityService,
                                    val mailService: MailService) extends Controller with SecureSocial with I18nSupport {

  /*
  @Autowired
  private var userCredentialService : UserCredentialService = _

  @Autowired
  private var messageService: MessageService = _

  @Autowired
  private var mailService: MailService = _
*/

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

  def markMessageAsRead(messageId : String) = SecuredAction { implicit request =>

    messageService.findById(UUID.fromString(messageId)) match {
      case None =>
        Ok("No Message found")
      case Some(msg) =>
        if(msg.getRecipient.objectId == request.user.asInstanceOf[UserCredential].objectId){
          msg.read = true
          messageService.saveMessage(msg)
          Ok("Ok")
        }else{
          Ok("User tried to read someone else's messages")
        }
    }
  }

  def createListOfMessages(listOfMessages: Option[List[MessageData]], currentUser: UserCredential): Option[List[ReplyToGuestMessage]] = {
    listOfMessages match {
      case Some(items) =>
        if(items.nonEmpty)
          None

        Some(items.flatMap {x =>
          mapMessageAndFillReplyForm(x, currentUser)
        })
      case _ => None
    }
  }


  def mapMessageAndFillReplyForm(msgToRender: MessageData, currentUser: UserCredential): Option[ReplyToGuestMessage] = {
    if (currentUser.objectId.equals(UUID.fromString(msgToRender.getRecipientObjectId()))) {
      val hostReply = MessageForm.apply(
        msgToRender.getOwnerFirstName(),
        msgToRender.getOwnerLastName(),
        Option(msgToRender.getPhoneNumber()),
        Option(msgToRender.getOwnerObjectId()),
        Option(currentUser.objectId.toString),
        msgToRender.getRequestedDate(),
        msgToRender.getRequestedTime(),
        msgToRender.getNumberOfGuests(),
        Option(msgToRender.getRequest()),
        Option(""),
        Option(msgToRender.getMessageType()),
        Option(msgToRender.getCreatedDate()),
        Option(msgToRender.getMessageObjectId()),
        msgToRender.getOwnerProfileLinkName())

      Some(ReplyToGuestMessage(messageFormMapping.fill(hostReply),
        msgToRender.getOwnerProfileLinkName(),
        msgToRender.getMessageObjectId(),
        msgToRender))
    }else{
      None
    }
  }

/*
  def mapMessageAndFillReplyForm(messageToRender: Message, currentUser: UserCredential): Option[ReplyToGuestMessage] = {
    if (messageToRender.getRecipient != null && messageToRender.getRecipient.equals(currentUser)) {
      val hostReply = MessageForm.apply(messageToRender.getOwner().firstName,
        messageToRender.getOwner().lastName,
        Option(messageToRender.phone),
        Option(messageToRender.getOwner().objectId.toString),
        Option(currentUser.objectId.toString),
        messageToRender.date, messageToRender.time,
        messageToRender.numberOfGuests,
        Option(messageToRender.request),
        Option(""),
        Option(messageToRender.`type`),
        Option(messageToRender.getCreatedDate),
        Option(messageToRender.objectId.toString),
        messageToRender.getOwner().profiles.iterator().next().profileLinkName)

      Some(ReplyToGuestMessage(messageFormMapping.fill(hostReply),
        messageToRender.getOwner(),
        messageToRender.objectId.toString,
        messageToRender))
    }else{
      None
    }
  }
*/
  def replyToGuest = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    val currentUser = request.user

    messageFormMapping.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.host.hostErrorMsg.render(Messages("rating.add.error"), "error", request2Messages))
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
                for(msg: MessageData <- msgItr.get){
                //while (msgItr.hasNext) {
                  //var msg: Message = msgItr.next()

                  if(msg.getRequest().equals(content.request.getOrElse(""))) {

                    // save here
                    messageService.createResponse(currentUser, receiver, UUID.fromString(msg.getMessageObjectId()), content.response.getOrElse(""), msg.getPhoneNumber())

                    val guest = EmailAndName(
                      name = receiver.firstName + " " + receiver.lastName,
                      email = receiver.emailAddress
                    )

                    val hdc = EmailAndName(
                      name = Messages("main.title"),
                      email = Messages("footer.link.mail.text")
                    )


                    val baseUrl: String = routes.StartPageController.index().absoluteURL(false).dropRight(1)
                    val userUrl: String = routes.UserProfileController.viewProfileByName(receiver.profiles.asScala.head.profileLinkName).url + "#inbox-tab"

                    val appUrl: String = " " + currentUser.getFullName + " <a href='" + (baseUrl + userUrl) + "'>" + Messages("mails.hosting.mail.link-text") + "</a>"

                    mailService.createAndSendMailNoReply(
                      subject = Messages("main.title") + " " + Messages("mails.hosting.mail.title"),
                      message = Messages("mail.hdc.text") + appUrl,
                      recipient = guest,
                      from = hdc)

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


  def renderHostForm(hostingUser: UserCredential, currentUser: Option[UserCredential])(implicit request: RequestHeader) = {
    val perf = customUtils.Helpers.startPerfLog()
    val r = currentUser match {
      case None =>
        views.html.host.hostNotLoggedIn()
      case Some(cu) =>

        // Disallow user to apply to themselves
        if(hostingUser.objectId.toString.equalsIgnoreCase(cu.objectId.toString)){
          views.html.host.hostErrorMsg.render(Messages("You can´t apply to yourself"), "info", request2Messages)
        }else{

          val today = Calendar.getInstance().getTime()
          val format = new SimpleDateFormat("HH:mm")
          val currentTime = format.parse(format.format(new Date()))

          val host = MessageForm.apply(cu.firstName, cu.lastName, Option(cu.getPhone), Option(cu.objectId.toString), Option(hostingUser.objectId.toString), new Date(), currentTime, 1, Option(""), Option(""), Option(""), Option(new Date()), Option(""), cu.profiles.asScala.head.profileLinkName)

          views.html.host.applyHost.render(messageFormMapping.fill(host), Some(hostingUser), Some(cu), request2Messages)
        }
    }
    customUtils.Helpers.endPerfLog("hostForm", perf)
    r
  }


  def applyToHost = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val currentUser = request.user

    messageFormMapping.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.host.applyHost(errors, Option(currentUser), Some(request.user)))
      },
      content => {

        if (content.request.isEmpty) {

          userCredentialService.findById(UUID.fromString(content.hostId.getOrElse(""))) match {
            case Some(hostingUser) =>
              Redirect(routes.UserProfileController.viewProfileByName(hostingUser.profiles.asScala.head.profileLinkName)).flashing(FlashMsgConstants.Error -> Messages("mails.error.no.message"))
            case None =>
              BadRequest(views.html.host.hostErrorMsg.render(Messages("mails.error.no.message"), "error", request2Messages))
          }

        } else {

          userCredentialService.findById(UUID.fromString(content.hostId.getOrElse(""))) match {
            case None =>
              BadRequest(views.html.host.hostErrorMsg.render(Messages("rating.add.error"), "error", request2Messages))
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

              mailService.createAndSendMailNoReply(
                subject = Messages("main.title") + " " + Messages("mails.hosting.mail.title"),
                message = Messages("mail.hdc.text") + appUrl,
                recipient = host,
                from = hdc)

              Redirect(routes.UserProfileController.viewProfileByName(hostingUser.profiles.asScala.head.profileLinkName))
            }
          }

        }



      }

    )


  }

}
