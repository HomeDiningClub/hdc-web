package customUtils.authorization


import javax.inject.Inject
import securesocial.core.Authorization
import models.{Recipe, UserProfile, UserRole, UserCredential}
import enums.RoleEnums.RoleEnums
import scala.collection.JavaConverters._
import services.NodeEntityService
import java.util.UUID
import scala.collection.JavaConverters._
import play.api.mvc.RequestHeader

case class WithRoleAndOwnerOfObject (role: RoleEnums, objectIdToControl: UUID) extends Authorization[UserCredential] {

  @Inject private lazy val nodeEntityService: NodeEntityService = null

  def isAuthorized(user: UserCredential, request: RequestHeader) = {

    user match {
      case userCred: UserCredential =>
        userCred.roles.iterator.asScala.find((r: UserRole) => r.name.equalsIgnoreCase(role.toString)) match {
          case Some(foundRole) =>

            // User is a credential and has the correct role, now check the access to the objectId
            val node = nodeEntityService.getAnyNodeUsingId(objectIdToControl)
            node match {
              case None => false
              case Some(anyItem) =>
                // TODO: Security check might be slow, we do three different DB-queries
                nodeEntityService.isNodeEditableBy(anyItem, userCred)
            }
          case None =>
            false
        }
      case _ =>
        false
    }
//
//    if(optionUser){
//      //val dataIndex = InstancedServices.userCredentialService.template.getIndex(classOf[NodeEntity], "objectId")
//      userInRole
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

//    } else {
//      userInRole
//    }
  }
}
