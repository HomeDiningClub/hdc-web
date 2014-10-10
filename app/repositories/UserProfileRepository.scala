package repositories

import org.springframework.data.neo4j.repository.GraphRepository

import models.{UserCredential, UserProfile}
import org.springframework.data.neo4j.annotation.Query
import models.profile.TagWord
import models.location.County
import java.util.UUID

trait UserProfileRepository extends GraphRepository[UserProfile] {

  // Get all profiles matching a tag and a county
  //@Query("MATCH (userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) MATCH (userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WHERE tw.objectId={0} AND c.objectId={1} RETURN userP")
  //@Query("MATCH (c:`County`)<-[:`LOCATION_AT`]-(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WHERE tw.objectId={0} AND c.objectId={1} RETURN userP")
  @Query("MATCH (c:`County`)<-[:`LOCATION_AT`]-(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WHERE tw.objectId={0} AND c.objectId={1} RETURN userP")
  def findByTagWordIdAndCountyId(tagWordId: String, countyId: String) : java.util.List[UserProfile]

  // Get all profiles matching a tag
  @Query("MATCH (userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WHERE tw.objectId={0} RETURN userP")
  def findByTagWordId(tagWordId: UUID) : java.util.List[UserProfile]

  // Get all profiles matching a county
  @Query("MATCH (userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WHERE c.objectId={0} RETURN userP")
  def findByCountyId(countyId: UUID) : java.util.List[UserProfile]

  // Get all who has me as favorite
  @Query("MATCH (u:`UserProfile`)<-[:FAVORITE_USER]-(f:`UserProfile`) WHERE u.objectId={0} RETURN f")
  def findFriendsToUser(userId: UUID) : java.util.List[UserProfile]


  // Auto-mapped by spring
  def findByUserIdentityAndProviderIdentity(userIdentity: String, providerIdentity: String) :  UserProfile
  def findByprofileLinkName(profileLinkName: String) : UserProfile
  def findByowner(owner: UserCredential) : UserProfile

}
