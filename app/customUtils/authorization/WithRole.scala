package customUtils.authorization

import securesocial.core.Authorization
import models.UserCredential
import enums.RoleEnums.RoleEnums
import play.api.mvc.RequestHeader

case class WithRole(role: RoleEnums) extends Authorization[UserCredential] {

  def isAuthorized(user: UserCredential, request: RequestHeader): Boolean = {
    IsAuthorizedChecks.ValidateWithRole(user, role)
  }
}
