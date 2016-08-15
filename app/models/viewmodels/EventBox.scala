package models.viewmodels

import java.util.UUID

case class EventBox (
    objectId: Option[UUID],
    linkToEvent: String,
    name: String,
    preAmble: Option[String],
    mainImage: Option[String],
    userImage: Option[String],
    price: Long,
    location: Option[String],
    //    eventRating: Int,
    eventBoxCount: Long,
    hasNext: Boolean,
    hasPrevious: Boolean,
    totalPages: Int
)
