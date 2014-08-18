package utils.authorization

import securesocial.core.{Identity, Authorization}
import models.{UserProfile, UserRole, UserCredential}
import enums.RoleEnums.RoleEnums
import scala.collection.JavaConverters._
import services.{InstancedServices, UserCredentialService}
import java.util.UUID
import org.springframework.data.neo4j.annotation.NodeEntity
import play.api.Logger
import models.base.AbstractEntity
import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.Node

case class WithRoleAndOwnerOfObject(role: RoleEnums, objectIdToControl: UUID) extends Authorization {

  def isAuthorized(user: Identity) = {
    val userInRole = user match {
      case user: UserCredential =>
        val isInRole = user.roles.iterator.asScala.find((r: UserRole) => r.name.equalsIgnoreCase(role.toString)) match {
          case Some(foundRole) => true
          case None => false
        }
        isInRole
      case _ =>
        false
    }

    if(userInRole){
      //val dataIndex = InstancedServices.userCredentialService.template.getIndex(classOf[NodeEntity], "objectId")
      userInRole
//      if(dataIndex == null){
//        false
//      } else {
        //val node: Node = dataIndex.query("objectId", objectIdToControl).getSingle
        //val node = InstancedServices.userCredentialService.template.lookup("objectId","objectId",objectIdToControl)
        //template.lookup(Video.class, "id", id).to(Video.class).singleOrNull();
//        val node = InstancedServices.userCredentialService.nodeEntityRepository.getAnyNodeUsingId(objectIdToControl)
//
//        node match {
//          case null => false
//          case anyItem =>
//            InstancedServices.userCredentialService.template.getStoredEntityType(anyItem).getType match {
//              case item: Class[UserCredential] =>
//                Logger.debug("Found UserCredential")
//                false
//              case item: Class[UserProfile] =>
//                Logger.debug("Found UserCredential")
//                false
//              case _ =>
//                false
//            }
//        }
//      }

    } else {
      userInRole
    }
  }
}
