package models.viewmodels

import java.time.{LocalTime, LocalDate}
import java.util.UUID

import models.event.MealType

case class EventBookingSuccess(bookingNumber: UUID,
                                eventName: String,
                                eventLink: String,
                                mealType: Option[String],
                                date: LocalDate,
                                time: LocalTime,
                                locationAddress: String,
                                locationCity: String,
                                locationZipCode: String,
                                locationCounty: String,
                                phoneNumberToHost: Option[String],
                                nrOfGuests: Int,
                                totalCost: Int,
                                email: String) {

}