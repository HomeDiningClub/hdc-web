package models.viewmodels

case class MenuItem(name: String,
                    alt: String = "",
                    title: String = "",
                    url: String,
                    target: String = "",
                    cssClass: String = "",
                    icon: String = "",
                    selected: Boolean = false) {
}
