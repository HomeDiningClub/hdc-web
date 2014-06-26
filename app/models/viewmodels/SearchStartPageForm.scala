package models.viewmodels

case class SearchStartPageForm(freeText: Option[String], area: Seq[(String,String)], foodArea: Seq[(String,String)]) { }
