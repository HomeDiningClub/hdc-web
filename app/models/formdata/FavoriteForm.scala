package models.formdata

case class FavoriteForm(profileName : String,
                        userName: String,
                        profilePreAmble: String,
                        profilePicture: Option[String],
                        userProfileObjectId: String,
                        userCredObjectId : String)
