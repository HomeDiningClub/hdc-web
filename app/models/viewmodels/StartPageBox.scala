package models.viewmodels

import models.files.ContentFile
import models.rating.RatingUserCredential

// Used on the startpage, collects profile information and userinformation
case class StartPageBox (
    id: Option[Int],
    linkToProfile: String,
    fullName: String,
    location: String,
    mainBody: Option[String],
    mainImage: ContentFile,
    userImage: ContentFile,
    userRating: RatingUserCredential
)
