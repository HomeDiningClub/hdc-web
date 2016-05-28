package models.viewmodels

import java.time.{LocalTime, LocalDate}

case class EventBookingSuccess(bookingNumber: Long,
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
