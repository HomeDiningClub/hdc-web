package models.viewmodels

import java.util.{Date, UUID}
import play.api.mvc.Call

// Used on the Start page
case class ReviewBox (
    objectId: Option[UUID],
    linkToProfile: Call,    // link to the person rating
    firstName: String,      // profileName who is rating a profile ...
    rankedName: String,     // profileName on the rated profile
    linkToRatedItem: Call,  // link to rated profile
    reviewText: Option[String],
    ratedDate: Date,
    userImage: Option[String],
    rating: Int
)
