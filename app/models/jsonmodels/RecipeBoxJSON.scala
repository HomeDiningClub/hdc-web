package models.jsonmodels

import play.api.libs.json._
import play.api.libs.json.JsString
import play.api.libs.json.JsObject

case class RecipeBoxJSON(
  objectId: String,
  linkToRecipe: String,
  name: String,
  preAmble: String,
  mainImage: String,
  recipeRating: String
)

object RecipeBoxJSON {

  implicit object RecipeBoxJSONFormat extends Format[RecipeBoxJSON] {


    def writes(jsonObject: RecipeBoxJSON): JsValue = {

      val jsonCaseSeq = Seq(
        "objectId"   -> JsString(jsonObject.objectId),
        "linkToRecipe"     -> JsString(jsonObject.linkToRecipe),
        "name"       -> JsString(jsonObject.name),
        "preAmble"    -> JsString(jsonObject.preAmble),
        "mainImage"    -> JsString(jsonObject.mainImage),
        "recipeRating"    -> JsString(jsonObject.recipeRating)
      )
      JsObject(jsonCaseSeq)
    }


    def reads(json: JsValue): JsResult[RecipeBoxJSON] = {
      JsSuccess(RecipeBoxJSON("", "", "", "", "", ""))
    }

  }


}
