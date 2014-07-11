package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.UserCredential
import org.springframework.data.neo4j.annotation.Query
import securesocial.core.{IdentityId, Identity}
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID


trait UserCredentialRepository extends GraphRepository[UserCredential]
{

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): UserCredential

  @Query("start up=node:UserCredential(email={0}) return up")
  def getUserCredentials(email: String ): Array[UserCredential]

  @Query("start up=node:UserCredential(identity={0}) return up")
  def getUserCredential(identityId: IdentityId ): UserCredential

}
