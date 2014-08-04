package models.viewmodels

case class AddContentForm(
                          id: Option[String],
                          parentId: Option[String],
                          name: String,
                          route: String,
                          title: Option[String],
                          preamble: Option[String],
                          mainBody: Option[String],
                          contentState: String,
                          contentCategories: Option[List[String]],
                          visibleInMenus: Boolean
                           ){}
