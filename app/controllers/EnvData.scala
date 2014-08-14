package controllers

case class EnvData(
        name            : String,
        emails          : List[String],
        quality         : List[String],
        aboutmeheadline : String,
        aboutme         : String,
        county          : String,
        streetAddress   : String,
        zipCode         : String,
        city            : String
)
