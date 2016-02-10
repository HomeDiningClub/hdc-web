package models.formdata

case class UserProfileOptions(
                               payCache: Option[String],
                               paySwish: Option[String],
                               payBankCard: Option[String],
                               payIZettle: Option[String],
                               //roleGuest       : Option[String],
                               roleHost: Option[String],
                               maxGuest: String,
                               minGuest: String,
                               quality: List[String],
                               handicapFriendly: Option[String], // moved from EnvData
                               childFfriendly: Option[String],
                               havePets: Option[String],
                               smoke: Option[String]
                               )


