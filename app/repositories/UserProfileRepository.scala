package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.UserProfileData
import org.springframework.data.neo4j.annotation.Query
import securesocial.core.{IdentityId, Identity}

trait  UserProfileRepository extends GraphRepository[UserProfileData] {

  @Query("start up=node:UserProfileData(userName={0}) return up")
  def getUserProfilesByName(userName: String ): UserProfileData

  @Query("start up=node:UserProfileData(identity={0}) return up")
  def getUserProfilesByIdentityId(identityId: IdentityId ): UserProfileData

}