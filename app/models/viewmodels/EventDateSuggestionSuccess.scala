package models.viewmodels

import java.time.{LocalDate, LocalTime}

import models.UserCredential

case class EventDateSuggestionSuccess(eventName: String,
                                        eventLink: String,
                                        date: LocalDate,
                                        time: LocalTime,
                                        nrOfGuests: Int,
                                        comment: Option[String],
                                        hostEmail: String,
                                        host: UserCredential,
                                        guestEmail: String) {

}
