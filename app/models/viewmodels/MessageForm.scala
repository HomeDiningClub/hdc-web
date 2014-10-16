package models.viewmodels

import java.util.Date

/**
 * Created by Tommy on 15/10/2014.
 */
case class MessageForm(
                            firstName: String,
                            lastName: String,
                            phone: String,
                            memberId: Option[String],
                            hostId: Option[String],
                            date: Date,
                            time: Date,
                            numberOfGuests: Int,
                            request: Option[String],
                            response: Option[String],
                            messageType: Option[String],
                            createdDate: Option[Date]
                            )
{ }
