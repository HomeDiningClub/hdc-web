package models.viewmodels

case class SearchFilterForm(
                                //freeText: Option[String],
                                boxFilterCounty: Option[String],
                                boxFilterTag: Option[String],
                                boxFilterIsHost: Option[Boolean]
                                ) { }
