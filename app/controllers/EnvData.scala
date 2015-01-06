package controllers

case class EnvData(
        name            : String,
        name2           : String,
        aboutmeheadline : String,
        aboutme         : String,
        county          : String,
        streetAddress   : String,
        zipCode         : String,
        city            : String,
        phoneNumber     : String,
        personnummer    : String,
        acceptTerms     : Boolean,
        childFfriendly  :Option[String],
        handicapFriendly:Option[String],
        havePets        :Option[String],
        smoke           : Option[String],
        allkoholServing :Option[String],
        mainimage       : Option[String],
        avatarimage     : Option[String]
)
