package models.formdata

case class AddContentForm(
                          id: Option[String],
                          relatedPages: Option[List[String]],
                          name: String,
                          route: String,
                          title: Option[String],
                          preamble: Option[String],
                          mainBody: Option[String],
                          contentState: String,
                          contentCategories: Option[List[String]],
                          visibleInMenus: Boolean
                           ){}
