package models.formdata

case class EventForm(id: Option[String],
                       name: String,
                       preAmble: Option[String],
                       mainBody: Option[String],
                       price: Int,
                       mainImage: Option[String],
                       images: Option[String],
                       eventDates: Option[List[EventDateForm]]
                       ){ }
