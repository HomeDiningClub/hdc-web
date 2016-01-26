package models.formdata

case class LikeForm(
                       userLikesThisObjectId: String,
                       likeValue: Boolean,
                       likeType: String
                       )
{ }
