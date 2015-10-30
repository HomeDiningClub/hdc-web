package models.viewmodels

import java.util.Date

case class EventFormDate (
                          id: Option[String],
                          date: Date,
                          guestsBooked: Int
                       )
{ }
