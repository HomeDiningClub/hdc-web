package models.viewmodels

import java.util.{Date, UUID}

// Used on the Start page
case class ReviewBox (
    objectId: Option[UUID],
    linkToProfile: String,
    firstName: String,
    rankedName: String,
    linkToRatedItem: String,
    reviewText: Option[String],
    ratedDate: Date,
    userImage: Option[String],
    rating: Int
)
