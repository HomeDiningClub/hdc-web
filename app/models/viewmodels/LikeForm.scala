package models.viewmodels

case class LikeForm(
                       userLikesThisObjectId: String,
                       likeValue: Boolean,
                       likeType: String
                       )
{ }
