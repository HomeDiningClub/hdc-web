package models.viewmodels

import java.time.{LocalDate, LocalTime}

import models.UserCredential

case class EventDateSuggestionSuccess(eventName: String,
                                        eventLink: String,
                                        date: LocalDate,
                                        time: LocalTime,
                                        nrOfGuests: Int,
                                        hostEmail: String,
                                        host: UserCredential,
                                        guestComment: Option[String],
                                        guestEmail: String,
                                        guestFullName: String,
                                        guestProfileName: String,
                                        guestProfileLink: String,
                                        guestPhone: Option[String]) {

}
