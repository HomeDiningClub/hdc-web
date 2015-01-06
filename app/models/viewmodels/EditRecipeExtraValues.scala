package models.viewmodels

case class EditRecipeExtraValues(mainImagePrev : Option[List[String]],
                                  recipeImagesPrev : Option[List[String]],
                                  mainImageMaxNr : Integer,
                                  recipeImagesMaxNr : Integer
                                  )
