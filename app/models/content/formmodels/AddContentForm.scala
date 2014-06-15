package models.content.formmodels

case class AddContentForm(name: String, route: String, title: Option[String], preamble: Option[String], mainBody: Option[String]) {

}
