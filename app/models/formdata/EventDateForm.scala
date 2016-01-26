package models.formdata

import java.util.Date

case class EventDateForm (
                          id: Option[String],
                          date: Date,
                          guestsBooked: Int
                       )
{ }
