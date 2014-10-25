package models.viewmodels

import java.util.{Date, UUID}
import play.api.mvc.Call

// Used on the Start page
case class ReviewBox (
    objectId: Option[UUID],
    linkToProfile: Call,
    firstName: String,
    rankedName: String,
    linkToRatedItem: Call,
    reviewText: Option[String],
    ratedDate: Date,
    userImage: Option[String],
    rating: Int
)
