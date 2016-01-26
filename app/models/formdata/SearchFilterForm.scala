package models.formdata

case class SearchFilterForm(
                                //freeText: Option[String],
                                boxFilterCounty: Option[String],
                                boxFilterTag: Option[String],
                                boxFilterIsHost: Option[Boolean]
                                ) { }
