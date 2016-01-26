package models.formdata

case class RatingForm(
                       id: String,
                       ratingValue: Int,
                       ratingComment: Option[String],
                       ratingReferrer: Option[String],
                       ratingType: String
                       )
{ }
