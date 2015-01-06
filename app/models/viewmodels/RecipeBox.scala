package models.viewmodels

import java.util.UUID

// Used on the Start page, collects profile information and user information
case class RecipeBox (
    objectId: Option[UUID],
    linkToRecipe: String,
    name: String,
    preAmble: Option[String],
    mainImage: Option[String],
    recipeRating: Int,
    recipeBoxCount: Long,
    hasNext: Boolean,
    hasPrevious: Boolean,
    totalPages: Int
)
