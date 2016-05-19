package models.formdata

import java.time.LocalDateTime
import java.util.UUID

case class EventBookingForm(eventId: UUID,
                            eventDateId: Option[UUID],
                            date: Option[LocalDateTime],
                            isSuggestedDate: Boolean,
                            guests: Int,
                            comment: Option[String]
                             ) {}
