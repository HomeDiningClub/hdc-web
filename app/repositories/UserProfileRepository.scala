package repositories

import org.springframework.data.neo4j.repository.GraphRepository

import models.UserProfile
import org.springframework.data.neo4j.annotation.Query
import java.util


trait  UserProfileRepository extends GraphRepository[UserProfile] {

/*
  @Query("start up=node:UserProfile(userName={0}) return up")
  def getUserProfilesByName(userName: String ): UserProfile

  @Query("start up=node:UserProfile(identity={0}) return up")
  def getUserProfilesByIdentityId(identityId: IdentityId ): UserProfile
*/

  def findByKeyIdentity(keyIdentity: String): util.List[UserProfile]

}
