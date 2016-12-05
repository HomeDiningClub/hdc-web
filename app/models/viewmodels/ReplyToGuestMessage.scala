package models.viewmodels
import models.UserCredential
import models.formdata.MessageForm
import models.message.{Message, MessageData}
import play.api.data.Form

case class ReplyToGuestMessage(form: Form[MessageForm],
                               ownerProfileLinkName: String,
                               msgId: String,
                               message: MessageData) {
}
