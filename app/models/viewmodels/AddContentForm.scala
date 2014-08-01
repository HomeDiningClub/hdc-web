package models.viewmodels

case class AddContentForm(
                          id: Option[Int],
                          parentId: Option[String],
                          name: String,
                          route: String,
                          title: Option[String],
                          preamble: Option[String],
                          mainBody: Option[String],
                          visible: Boolean
                           ){}
