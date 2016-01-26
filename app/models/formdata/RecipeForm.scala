package models.formdata

case class RecipeForm(
                       id: Option[String],
                       name: String,
                       preAmble: Option[String],
                       mainBody: Option[String],
                       mainImage: Option[String],
                       images: Option[String]
                       )
{ }
