package repositories

import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.neo4j.repository.GraphRepository
import models.{UserCredential, UserProfile}
import org.springframework.data.neo4j.annotation.Query
import java.util.UUID

trait UserProfileRepository extends GraphRepository[UserProfile] {

  // Get all profiles matching both a tag and a county and is a host
  @Query("MATCH (c:`County`)<-[:`LOCATION_AT`]-(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,tw,c WHERE tw.objectId={0} AND c.objectId={1} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {2} IN userP.role RETURN userP ORDER BY mi.objectId")
  def findByTagWordIdAndCountyIdAndIsHost(tagWordId: String, countyId: String, hostRole: String) : java.util.List[UserProfile]
  @Query("MATCH (c:`County`)<-[:`LOCATION_AT`]-(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,tw,c WHERE tw.objectId={0} AND c.objectId={1} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {2} IN userP.role RETURN userP ORDER BY mi.objectId")
  def findByTagWordIdAndCountyIdAndIsHost(tagWordId: String, countyId: String, hostRole: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles matching both a tag and a county
  @Query("MATCH (c:`County`)<-[:`LOCATION_AT`]-(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,tw,c WHERE tw.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND c.objectId={1} RETURN userP ORDER BY mi.objectId")
  def findByTagWordIdAndCountyId(tagWordId: String, countyId: String) : java.util.List[UserProfile]
  @Query("MATCH (c:`County`)<-[:`LOCATION_AT`]-(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,tw,c WHERE tw.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND c.objectId={1} RETURN userP ORDER BY mi.objectId")
  def findByTagWordIdAndCountyId(tagWordId: String, countyId: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles matching a tag
  @Query("MATCH (userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,tw WHERE tw.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY mi.objectId")
  def findByTagWordId(tagWordId: String) : java.util.List[UserProfile]
  @Query("MATCH (userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,tw WHERE tw.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY mi.objectId")
  def findByTagWordId(tagWordId: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles matching a tag and is a host
  @Query("MATCH (userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,tw WHERE tw.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {1} IN userP.role RETURN userP ORDER BY mi.objectId")
  def findByTagWordIdAndIsHost(tagWordId: String, hostRole: String) : java.util.List[UserProfile]
  @Query("MATCH (userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,tw WHERE tw.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {1} IN userP.role RETURN userP ORDER BY mi.objectId")
  def findByTagWordIdAndIsHost(tagWordId: String, hostRole: String, pageable: Pageable) : Page[UserProfile]


  // Get all profiles matching a county
  @Query("MATCH (userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WITH userP,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,c WHERE c.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY mi.objectId")
  def findByCountyId(countyId: String) : java.util.List[UserProfile]
  @Query("MATCH (userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WITH userP,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,c WHERE c.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY mi.objectId")
  def findByCountyId(countyId: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles matching a county and is a host
  @Query("MATCH (userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WITH userP,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,c WHERE c.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {1} IN userP.role RETURN userP ORDER BY mi.objectId")
  def findByCountyIdAndIsHost(countyId: String, hostRole: String) : java.util.List[UserProfile]
  @Query("MATCH (userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WITH userP,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,c WHERE c.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {1} IN userP.role RETURN userP ORDER BY mi.objectId")
  def findByCountyIdAndIsHost(countyId: String, hostRole: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles matching being a host
  @Query("MATCH (userP:`UserProfile`) WITH userP OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi WHERE has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {0} IN userP.role RETURN userP ORDER BY mi.objectId")
  def findByIsHost(hostRole: String) : java.util.List[UserProfile]
  @Query("MATCH (userP:`UserProfile`) WITH userP OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi WHERE has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {0} IN userP.role RETURN userP ORDER BY mi.objectId")
  def findByIsHost(hostRole: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles
  @Query("MATCH (userP:`UserProfile`) WITH userP OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi WHERE has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY mi.objectId")
  def findAllProfiles() : java.util.List[UserProfile]
  @Query("MATCH (userP:`UserProfile`) WITH userP OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi WHERE has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY mi.objectId")
  def findAllProfiles(pageable: Pageable) : Page[UserProfile]

  // Get all who has me as favorite
  @Query("MATCH (u:`UserProfile`)<-[:FAVORITE_USER]-(f:`UserProfile`) WHERE u.objectId={0} RETURN f")
  def findFriendsToUser(userId: UUID) : java.util.List[UserProfile]

  // Auto-mapped by spring
  def findByUserIdentityAndProviderIdentity(userIdentity: String, providerIdentity: String) :  UserProfile
  def findByprofileLinkName(profileLinkName: String) : UserProfile
  def findByowner(owner: UserCredential) : UserProfile

}
