package models.viewmodels
import models.UserCredential
import models.formdata.MessageForm
import models.message.Message
import play.api.data.Form

case class ReplyToGuestMessage(form: Form[MessageForm],
                               owner: UserCredential,
                               msgId: String, message: Message) {
}
