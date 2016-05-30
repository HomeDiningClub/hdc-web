package models.formdata

import java.time.{LocalTime, LocalDate, LocalDateTime}
import java.util.UUID

case class EventDateSuggestionForm(suggestEventId: UUID,
                            date: LocalDate,
                            time: LocalTime,
                            guests: Int,
                            comment: Option[String]
                             ) {}
