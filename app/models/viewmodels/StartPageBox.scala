package models.viewmodels

import models.files.ContentFile
import models.rating.RatingUserCredential
import java.util.UUID

// Used on the Start page, collects profile information and user information
case class StartPageBox (
    objectId: Option[UUID],
    linkToProfile: String,
    fullName: String,
    location: String,
    mainBody: Option[String],
    mainImage: Option[String],
    userImage: Option[String],
    userRating: Int
)
