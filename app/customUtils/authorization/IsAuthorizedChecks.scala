package customUtils.authorization

import enums.RoleEnums._
import models.{UserRole, UserCredential}
import scala.collection.JavaConverters._

object IsAuthorizedChecks {

  def ValidateWithRole(user: UserCredential, role: RoleEnums): Boolean = {
    val retValue = user match {
      case user: UserCredential =>
        val isInRole = user.roles.iterator.asScala.find((r: UserRole) => r.name.equalsIgnoreCase(role.toString)) match {
          case Some(foundRole) => true
          case None => false
        }
        isInRole
      case _ =>
        false
    }
    retValue
  }



}
