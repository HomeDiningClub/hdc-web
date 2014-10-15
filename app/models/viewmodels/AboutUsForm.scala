package models.viewmodels

case class AboutUsForm(
                        id: Option[String],
                        to: String,
                        name: String,
                        subject: String,
                        message: String
                        )
{ }