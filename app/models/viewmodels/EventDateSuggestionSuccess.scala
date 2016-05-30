package models.viewmodels

import java.time.{LocalDate, LocalTime}

case class EventDateSuggestionSuccess(eventName: String,
                                        eventLink: String,
                                        date: LocalDate,
                                        time: LocalTime,
                                        nrOfGuests: Int,
                                        comment: Option[String],
                                        hostEmail: String,
                                        guestEmail: String) {

}
