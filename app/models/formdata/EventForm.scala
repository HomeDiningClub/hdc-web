package models.formdata

case class EventForm(id: Option[String],
                       name: String,
                       preAmble: Option[String],
                       mainBody: Option[String],
                       mainImage: Option[String],
                       images: Option[String],
                       eventDates: Option[List[EventDateForm]]
                       ){ }
