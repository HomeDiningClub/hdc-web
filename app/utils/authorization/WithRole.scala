package utils.authorization

import securesocial.core.{Identity, Authorization}
import models.{UserRole, UserCredential}
import enums.RoleEnums.RoleEnums
import scala.collection.JavaConverters._

case class WithRole(role: RoleEnums) extends Authorization {
  def isAuthorized(user: Identity) = {
    user match {
      case user: UserCredential =>
        val isInRole = user.roles.iterator.asScala.find((r: UserRole) => r.name.equalsIgnoreCase(role.toString)) match {
          case Some(foundRole) => true
          case None => false
        }
        isInRole
    }

    false
  }
}
