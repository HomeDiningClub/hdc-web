package models.viewmodels

case class EventPropertyList(showAddress: Boolean,
                              locationAddress: Option[String],
                              locationZipCode: Option[String],
                              locationCounty: Option[String],
                              locationCity: Option[String],
                              childFriendly: Boolean,
                              handicapFriendly: Boolean,
                              havePets: Boolean,
                              smokingAllowed: Boolean,
                              minNrOfGuests: Int,
                              maxNrOfGuests: Int,
                              alcoholServing: Option[String],
                              mealType: Option[String]) {}
