package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.UserCredential
import org.springframework.data.neo4j.annotation.Query
import securesocial.core.{IdentityId, Identity}
import org.springframework.data.neo4j.repository.GraphRepository


trait UserCredentialRepository extends GraphRepository[UserCredential]
{


  @Query("start up=node:UserCredential(email={0}) return up")
  def getUserCredentials(email: String ): Array[UserCredential]

  @Query("start up=node:UserCredential(identity={0}) return up")
  def getUserCredential(identityId: IdentityId ): UserCredential

}
