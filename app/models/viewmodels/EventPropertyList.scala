package models.viewmodels

case class EventPropertyList(locationAddress: Option[String],
                              locationZipCode: Option[String],
                              locationCounty: Option[String],
                              locationCity: Option[String],
                              childFriendly: Boolean,
                              handicapFriendly: Boolean,
                              havePets: Boolean,
                              smokingAllowed: Boolean,
                              alcoholServing: Option[String],
                              mealType: Option[String]) {}
