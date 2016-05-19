package models.viewmodels

import java.time.{LocalTime, LocalDate}

case class EventBookingSuccess(date: LocalDate,
                                time: LocalTime,
                                locationAddress: String,
                                locationCounty: String,
                                locationZipCode: String,
                                phoneNumberToHost: String,
                                nrOfGuests: Int,
                                totalCost: Int,
                                email: String) {

}
