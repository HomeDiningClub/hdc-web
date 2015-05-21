package models.jsonmodels

import play.api.libs.json._

case class BlogPostBoxJSON(
                            objectId: String,
                            title: String,
                            text : String,
                            dateCreated : String,
                            dateMod : String,
                            mainImage: String,
                            hasNext: Boolean,
                            hasPrevious: Boolean,
                            totalPages: Int
                            )


object BlogPostBoxJSON {

  implicit object BlogPostBoxJSONFormat extends Format[BlogPostBoxJSON] {


    def writes(jsonObject: BlogPostBoxJSON): JsValue = {

      val jsonCaseSeq = Seq(
        "objectId"          -> JsString(jsonObject.objectId),
        "title"             -> JsString(jsonObject.title),
        "text"              -> JsString(jsonObject.text),
        "dateCreated"       -> JsString(jsonObject.dateCreated),
        "dateMod"           -> JsString(jsonObject.dateMod),
        "mainImage"         -> JsString(jsonObject.mainImage),
        "hasNext"           -> JsBoolean(jsonObject.hasNext),
        "hasPrevious"       -> JsBoolean(jsonObject.hasPrevious),
        "totalPages"        -> JsNumber(jsonObject.totalPages)
      )
      JsObject(jsonCaseSeq)
    }


    def reads(json: JsValue): JsResult[BlogPostBoxJSON] = {
      JsSuccess(BlogPostBoxJSON("", "", "", "", "", "",  false, false, 0))
    }

  }


}
