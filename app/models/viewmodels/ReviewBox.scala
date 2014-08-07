package models.viewmodels

import java.util.UUID

// Used on the Start page
case class ReviewBox (
    objectId: Option[UUID],
    linkToProfile: String,
    fullName: String,
    reviewText: Option[String],
    userImage: String,
    rating: Int
)
