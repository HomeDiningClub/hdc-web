package models.jsonmodels

import play.api.libs.json._
import play.api.libs.json.JsString

case class EventBoxJSON(
  objectId: String,
  linkToEvent: String,
  name: String,
  preAmble: String,
  mainImage: String,
//  eventRating: String,
  eventBoxCount: Long,
  hasNext: Boolean,
  hasPrevious: Boolean,
  totalPages: Int
)

object EventBoxJSON {

  implicit object EventBoxJSONFormat extends Format[EventBoxJSON] {


    def writes(jsonObject: EventBoxJSON): JsValue = {

      val jsonCaseSeq = Seq(
        "objectId"        -> JsString(jsonObject.objectId),
        "linkToEvent"    -> JsString(jsonObject.linkToEvent),
        "name"            -> JsString(jsonObject.name),
        "preAmble"        -> JsString(jsonObject.preAmble),
        "mainImage"       -> JsString(jsonObject.mainImage),
//        "eventRating"    -> JsString(jsonObject.eventRating),
        "eventBoxCount"  -> JsNumber(jsonObject.eventBoxCount),
        "hasNext"         -> JsBoolean(jsonObject.hasNext),
        "hasPrevious"     -> JsBoolean(jsonObject.hasPrevious),
        "totalPages"      -> JsNumber(jsonObject.totalPages)
      )
      JsObject(jsonCaseSeq)
    }


    def reads(json: JsValue): JsResult[EventBoxJSON] = {
      JsSuccess(EventBoxJSON("", "", "", "", "", 0, false, false, 0))
    }

  }


}
