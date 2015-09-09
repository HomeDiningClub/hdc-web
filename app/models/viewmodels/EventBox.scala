package models.viewmodels

import java.util.UUID

case class EventBox (
    objectId: Option[UUID],
    linkToEvent: String,
    name: String,
    preAmble: Option[String],
    mainImage: Option[String],
//    recipeRating: Int,
    eventBoxCount: Long,
    hasNext: Boolean,
    hasPrevious: Boolean,
    totalPages: Int
)
