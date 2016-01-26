package models.formdata

case class SearchStartPageForm(
                                //freeText: Option[String],
                                boxFilterCounty: Option[String],
                                boxFilterTag: Option[String],
                                boxFilterIsHost: Option[Boolean]
                                ) { }
