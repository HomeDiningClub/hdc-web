package models.viewmodels

import java.util.UUID
import play.api.libs.json._

case class BrowseProfileBox (
    objectId: Option[UUID],
    linkToProfile: String,
    fullName: String,
    location: Option[String],
    mainBody: Option[String],
    mainImage: Option[String],
    userImage: Option[String],
    userRating: Int,
    isHost: Boolean
    //boxCount: Long,
    //hasNext: Boolean,
    //hasPrevious: Boolean,
    //totalPages: Int
)

object BrowseProfileBox {

  implicit object BrowseBoxJSONFormat extends Format[BrowseProfileBox] {

    def writes(jsonObject: BrowseProfileBox): JsValue = {
      val jsonCaseSeq = Seq(
        "objectId"      -> JsString(jsonObject.objectId.getOrElse("").toString),
        "linkToProfile" -> JsString(jsonObject.linkToProfile),
        "fullName"      -> JsString(jsonObject.fullName),
        "location"      -> JsString(jsonObject.location.getOrElse("")),
        "mainBody"      -> JsString(jsonObject.mainBody.getOrElse("")),
        "mainImage"     -> JsString(jsonObject.mainImage.getOrElse("")),
        "isHost"        -> JsBoolean(jsonObject.isHost),
        "userRating"    -> JsNumber(jsonObject.userRating)
        //"boxCount"      -> JsNumber(jsonObject.boxCount),
        //"hasNext"       -> JsBoolean(jsonObject.hasNext),
        //"hasPrevious"   -> JsBoolean(jsonObject.hasPrevious),
        //"totalPages"    -> JsNumber(jsonObject.totalPages)
      )
      JsObject(jsonCaseSeq)
    }

    def reads(json: JsValue): JsResult[BrowseProfileBox] = {
      JsSuccess(BrowseProfileBox(None, "", "", None, None, None, None, 0, false))
    }

  }
}
