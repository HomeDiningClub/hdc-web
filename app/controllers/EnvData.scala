package controllers

/***********************************************************************************
 * Max number of properites is 18
 * *
 * 01. name
 * 02. name2
 * 03. aboutmeheadline
 * 04. aboutme
 * 05. county
 * 06. streetAddress
 * 07. zipCode
 * 08. city
 * 09. phoneNumber
 * 10. personnummer
 * 11. acceptTerms
 * 12.allkoholServing
 * 13. mainimage
 * 14. avatarimage
 * 15. firstName
 * 16. lastName
 *
 **********************************************************************************/



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
        // childFfriendly  :Option[String],
        // handicapFriendly removed
        // havePets        :Option[String],
        // smoke           : Option[String],
        allkoholServing :Option[String],
        mainimage       : Option[String],
        avatarimage     : Option[String],
        firstName       : String,
        lastName        : String
)
