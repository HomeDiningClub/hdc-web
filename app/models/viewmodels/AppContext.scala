package models.viewmodels

case class AppContext(
                       headBodyBg: String,
                       headMenuItems: Option[List[MenuItem]],
                       headQuickLinks: Seq[(String,String,String,String,String)],
                       headQuickLinkTitle: String
                       )
{}
