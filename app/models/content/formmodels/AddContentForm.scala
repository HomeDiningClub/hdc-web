package models.content.formmodels

case class AddContentForm(id: Option[Int], name: String, route: String, title: Option[String], preamble: Option[String], mainBody: Option[String]) {

}
