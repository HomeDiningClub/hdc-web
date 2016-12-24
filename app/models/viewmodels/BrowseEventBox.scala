package models.viewmodels

import java.util.UUID
import play.api.libs.json._

case class BrowseEventBox(
                           objectId: Option[UUID],
                           linkToEvent: String,
                           eventName: String,
                           mainBody: Option[String],
                           mainImage: Option[String],
                           userImage: Option[String],
                           price: Int,
                           location: Option[String],
                           //    eventRating: Int,
                           eventBoxCount: Long,
                           hasNext: Boolean,
                           hasPrevious: Boolean,
                           totalPages: Int
                         )

object BrowseEventBox {

  implicit object BrowseBoxJSONFormat extends Format[BrowseEventBox] {

    def writes(jsonObject: BrowseEventBox): JsValue = {
      val jsonCaseSeq = Seq(
        "objectId" -> JsString(jsonObject.objectId.getOrElse("").toString),
        "linkToEvent" -> JsString(jsonObject.linkToEvent),
        "eventName" -> JsString(jsonObject.eventName),
        "mainBody" -> JsString(jsonObject.mainBody.getOrElse("")),
        "mainImage" -> JsString(jsonObject.mainImage.getOrElse("")),
        "userImage" -> JsString(jsonObject.userImage.getOrElse("")),
        "price" -> JsNumber(jsonObject.price),
        "location" -> JsString(jsonObject.location.getOrElse("")),
        //"userRating"    -> JsNumber(jsonObject.userRating)
        "eventBoxCount"      -> JsNumber(jsonObject.eventBoxCount),
        "hasNext"       -> JsBoolean(jsonObject.hasNext),
        "hasPrevious"   -> JsBoolean(jsonObject.hasPrevious),
        "totalPages"    -> JsNumber(jsonObject.totalPages)
      )
      JsObject(jsonCaseSeq)
    }

    def reads(json: JsValue): JsResult[BrowseEventBox] = {
      JsSuccess(BrowseEventBox(None, "", "", None, None, None, 0, None, 0, false, false, 0))
    }

  }

}
