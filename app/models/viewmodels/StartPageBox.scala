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
    mainImage: String,
    userImage: String,
    userRating: Int
)
