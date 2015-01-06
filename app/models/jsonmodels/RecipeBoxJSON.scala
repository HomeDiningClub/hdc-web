package models.jsonmodels

import play.api.libs.json._
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsNumber
import play.api.libs.json.JsString
import play.api.libs.json.JsObject

case class RecipeBoxJSON(
  objectId: String,
  linkToRecipe: String,
  name: String,
  preAmble: String,
  mainImage: String,
  recipeRating: String,
  recipeBoxCount: Long,
  hasNext: Boolean,
  hasPrevious: Boolean,
  totalPages: Int
)

object RecipeBoxJSON {

  implicit object RecipeBoxJSONFormat extends Format[RecipeBoxJSON] {


    def writes(jsonObject: RecipeBoxJSON): JsValue = {

      val jsonCaseSeq = Seq(
        "objectId"        -> JsString(jsonObject.objectId),
        "linkToRecipe"    -> JsString(jsonObject.linkToRecipe),
        "name"            -> JsString(jsonObject.name),
        "preAmble"        -> JsString(jsonObject.preAmble),
        "mainImage"       -> JsString(jsonObject.mainImage),
        "recipeRating"    -> JsString(jsonObject.recipeRating),
        "recipeBoxCount"  -> JsNumber(jsonObject.recipeBoxCount),
        "hasNext"         -> JsBoolean(jsonObject.hasNext),
        "hasPrevious"     -> JsBoolean(jsonObject.hasPrevious),
        "totalPages"      -> JsNumber(jsonObject.totalPages)
      )
      JsObject(jsonCaseSeq)
    }


    def reads(json: JsValue): JsResult[RecipeBoxJSON] = {
      JsSuccess(RecipeBoxJSON("", "", "", "", "", "", 0, false, false, 0))
    }

  }


}
