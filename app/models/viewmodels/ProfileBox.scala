package models.viewmodels

import java.util.UUID

// Used on the Start page, collects profile information and user information
case class ProfileBox (
    objectId: Option[UUID],
    linkToProfile: String,
    fullName: String,
    location: Option[String],
    mainBody: Option[String],
    mainImage: Option[String],
    userImage: Option[String],
    userRating: Int,
    isHost: Boolean
)
