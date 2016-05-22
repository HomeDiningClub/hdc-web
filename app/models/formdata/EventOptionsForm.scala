package models.formdata

import java.util.UUID

case class EventOptionsForm(childFriendly: Boolean,
                             handicapFriendly: Boolean,
                             havePets: Boolean,
                             smokingAllowed: Boolean,
                             alcoholServing: Option[UUID],
                             mealType: Option[UUID]
                             ) {

}

