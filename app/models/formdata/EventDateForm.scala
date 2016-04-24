package models.formdata

case class EventDateForm (
                          id: Option[String],
                          date: java.time.LocalDate,
                          time: java.time.LocalTime,
                          guestsBooked: Int = 0
                       )
{ }
