package models.formdata

import java.util.Date

case class MessageForm(
                            firstName: String,
                            lastName: String,
                            phone: Option[String],
                            memberId: Option[String],
                            hostId: Option[String],
                            date: Date,
                            time: Date,
                            numberOfGuests: Int,
                            request: Option[String],
                            response: Option[String],
                            messageType: Option[String],
                            createdDate: Option[Date],
                            messageId: Option[String],
                            profileLinkName: String
                            )
{ }
