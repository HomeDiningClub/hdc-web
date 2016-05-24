package models.viewmodels

import java.time.{LocalDate, LocalTime}

case class EventDateSuggestionSuccess(date: LocalDate,
                                        time: LocalTime,
                                        nrOfGuests: Int,
                                        comment: Option[String]) {

}
