package controllers

case class UserProfileOptions (
  payCache : Option[String],
  paySwish : Option[String],
  payBankCard : Option[String],
  payIZettle : Option[String],
  //roleGuest       : Option[String],
  roleHost        : Option[String],
  maxGuest : String,
  minGuest : String
                                )


