package models.formdata

import java.time.{LocalTime, LocalDate, LocalDateTime}
import java.util.UUID

case class EventDateSuggestionForm(eventId: UUID,
                            date: LocalDate,
                            time: LocalTime,
                            guests: Int,
                            comment: Option[String]
                             ) {}
