package models.formdata

import java.util.UUID

case class EventForm(id: Option[String],
                     name: String,
                     preAmble: Option[String],
                     mainBody: Option[String],
                     price: Int,
                     minNoOfGuest: Int,
                     maxNoOfGuest: Int,
                     mainImage: Option[String],
                     images: Option[String],
                     eventDates: Option[List[EventDateForm]],
                     eventOptionsForm: EventOptionsForm,
                     userProfileOptionsForm: Option[UserProfileOptionsForm],
                     eventDatesToDelete: Option[List[UUID]]
                    ) {}
