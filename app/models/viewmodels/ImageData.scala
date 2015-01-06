package models.viewmodels

import java.util.UUID

case class ImageData(objectId: Option[String],
                     name: String,
                     url: String,
                     selected: Boolean,
                     action: String = "")
