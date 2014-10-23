package controllers

case class UserProfileOptions (
  payCache : Option[String],
  paySwish : Option[String],
  payBankCard : Option[String],
  payIZettle : Option[String],
  maxGuest : String
                                )


