package repositories

import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.neo4j.repository.GraphRepository
import models.{UserCredential, UserProfile}
import org.springframework.data.neo4j.annotation.Query
import java.util.UUID

import models.profile.{FavoriteData, TaggedFavoritesToUserProfile}

trait UserProfileRepository extends GraphRepository[UserProfile] {

  // Get all profiles matching both a tag and a county and is a host
  @Query("MATCH (c:`County`)<-[:`LOCATION_AT`]-(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`), (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP) WITH userP,tw,c,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,tw,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,tw,c,(count(mi) + totalRat + avgRat) as upOrder WHERE tw.objectId={0} AND c.objectId={1} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {2} IN userP.role RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByTagWordIdAndCountyIdAndIsHost(tagWordId: String, countyId: String, hostRole: String) : java.util.List[UserProfile]
  @Query("MATCH (c:`County`)<-[:`LOCATION_AT`]-(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`), (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP) WITH userP,tw,c,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,tw,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,tw,c,(count(mi) + totalRat + avgRat) as upOrder WHERE tw.objectId={0} AND c.objectId={1} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {2} IN userP.role RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByTagWordIdAndCountyIdAndIsHost(tagWordId: String, countyId: String, hostRole: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles matching both a tag and a county
  @Query("MATCH (c:`County`)<-[:`LOCATION_AT`]-(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`), (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP) WITH userP,tw,c,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,tw,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,tw,c,(count(mi) + totalRat + avgRat) as upOrder WHERE tw.objectId={0} AND c.objectId={1} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByTagWordIdAndCountyId(tagWordId: String, countyId: String) : java.util.List[UserProfile]
  @Query("MATCH (c:`County`)<-[:`LOCATION_AT`]-(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`), (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP) WITH userP,tw,c,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,tw,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,tw,c,(count(mi) + totalRat + avgRat) as upOrder WHERE tw.objectId={0} AND c.objectId={1} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByTagWordIdAndCountyId(tagWordId: String, countyId: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles matching a tag
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,tw OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,tw,(count(mi) + totalRat + avgRat) as upOrder WHERE tw.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByTagWordId(tagWordId: String) : java.util.List[UserProfile]
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,tw OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,tw,(count(mi) + totalRat + avgRat) as upOrder WHERE tw.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByTagWordId(tagWordId: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles matching a tag and is a host
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,tw OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,tw,(count(mi) + totalRat + avgRat) as upOrder WHERE tw.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {1} IN userP.role RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByTagWordIdAndIsHost(tagWordId: String, hostRole: String) : java.util.List[UserProfile]
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP:`UserProfile`)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH userP,tw,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,tw OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,tw,(count(mi) + totalRat + avgRat) as upOrder WHERE tw.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {1} IN userP.role RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByTagWordIdAndIsHost(tagWordId: String, hostRole: String, pageable: Pageable) : Page[UserProfile]


  // Get all profiles matching a county
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WITH userP,c,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,c,(count(mi) + totalRat + avgRat) as upOrder WHERE c.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByCountyId(countyId: String) : java.util.List[UserProfile]
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WITH userP,c,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,c,(count(mi) + totalRat + avgRat) as upOrder WHERE c.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  //@Query("MATCH (userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WITH userP,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,mi,c WHERE c.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY mi.objectId")
  def findByCountyId(countyId: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles matching a county and is a host
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WITH userP,c,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,c,(count(mi) + totalRat + avgRat) as upOrder WHERE c.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {1} IN userP.role RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByCountyIdAndIsHost(countyId: String, hostRole: String) : java.util.List[UserProfile]
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(userP:`UserProfile`)-[:`LOCATION_AT`]->(c:`County`) WITH userP,c,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat,c OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,c,(count(mi) + totalRat + avgRat) as upOrder WHERE c.objectId={0} AND has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {1} IN userP.role RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByCountyIdAndIsHost(countyId: String, hostRole: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles matching being a host
  @Query("MATCH (userP:`UserProfile`)<-[:`IN_PROFILE`]-(uc:`UserCredential`) WITH userP,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,(count(mi) + totalRat + avgRat) as upOrder WHERE has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {0} IN userP.role RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByIsHost(hostRole: String) : java.util.List[UserProfile]
  @Query("MATCH (userP:`UserProfile`)<-[:`IN_PROFILE`]-(uc:`UserCredential`) WITH userP,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,(count(mi) + totalRat + avgRat) as upOrder WHERE has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 AND {0} IN userP.role RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findByIsHost(hostRole: String, pageable: Pageable) : Page[UserProfile]

  // Get all profiles
  @Query("MATCH (userP:`UserProfile`)<-[:`IN_PROFILE`]-(uc:`UserCredential`) WITH userP,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,(count(mi) + totalRat + avgRat) as upOrder WHERE has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findAllProfiles() : java.util.List[UserProfile]
  @Query("MATCH (userP:`UserProfile`)<-[:`IN_PROFILE`]-(uc:`UserCredential`) WITH userP,uc OPTIONAL MATCH (uc)<-[r:`RATED_USER`]-(:`UserCredential`) WITH userP,COUNT(r.ratingValue) as totalRat,AVG(CASE WHEN r.ratingValue is NULL THEN 0 ELSE r.ratingValue END) as avgRat OPTIONAL MATCH (userP)-[:`MAIN_IMAGE`]-(mi:`ContentFile`) WITH userP,(count(mi) + totalRat + avgRat) as upOrder WHERE has(userP.profileLinkName) AND LENGTH(userP.profileLinkName) > 0 RETURN userP ORDER BY upOrder DESC, userP.lastModifiedDate DESC")
  def findAllProfiles(pageable: Pageable) : Page[UserProfile]

  // Get all who has me as favorite
  @Query("MATCH (userP:`UserProfile`)<-[:FAVORITE_USER]-(friendUp:`UserProfile`)<-[:`IN_PROFILE`]-(friendUc:`UserCredential`) WHERE userP.objectId={0} AND EXISTS(friendUp.profileLinkName) WITH userP, friendUp, friendUc OPTIONAL MATCH (friendUp)-[:`AVATAR_IMAGE`]-(friendAi:`ContentFile`) RETURN friendUp.objectId AS UserProfileObjectId, friendUc.objectId AS UserCredentialObjectId, friendUc.firstName AS FirstName, friendUc.lastName AS LastName, friendUp.profileLinkName AS ProfileLinkName, friendUp.aboutMeHeadline AS AboutMeHeadline, COLLECT(friendAi.storeId) as AvatarImage")
  def findMyFriends(userProfileObjectId: String) : java.util.List[FavoriteData]

  // Get all my favorites
  @Query("MATCH (userP:`UserProfile`)-[:FAVORITE_USER]->(friendUp:`UserProfile`)<-[:`IN_PROFILE`]-(friendUc:`UserCredential`) WHERE userP.objectId={0} AND EXISTS(friendUp.profileLinkName) WITH userP, friendUp, friendUc OPTIONAL MATCH (friendUp)-[:`AVATAR_IMAGE`]-(friendAi:`ContentFile`) RETURN friendUp.objectId AS UserProfileObjectId, friendUc.objectId AS UserCredentialObjectId, friendUc.firstName AS FirstName, friendUc.lastName AS LastName, friendUp.profileLinkName AS ProfileLinkName, friendUp.aboutMeHeadline AS AboutMeHeadline, COLLECT(friendAi.storeId) as AvatarImage")
  def findFriendsToUser(userProfileObjectId: String) : java.util.List[FavoriteData]

  @Query("MATCH (userP:`UserProfile`)-[relFU:FAVORITE_USER]->(friendUp:`UserProfile`)<-[:`IN_PROFILE`]-(friendUc:`UserCredential`) WHERE userP.objectId={0} AND friendUc.objectId={1} RETURN Count(*)")
  def isFavouriteToMe(userProfileObjectId: String, friendUserCredObjectId: String) : Int

  @Query("MATCH (userP:`UserProfile`)-[relFU:FAVORITE_USER]->(friendUp:`UserProfile`) WHERE userP.objectId={0} AND friendUp.objectId={1} RETURN relFU")
  def findFavRelationToMe(userProfileObjectId: String, friendUserCredObjectId: String) : TaggedFavoritesToUserProfile


  // Auto-mapped by spring
  def findByUserIdentityAndProviderIdentity(userIdentity: String, providerIdentity: String) :  UserProfile
  def findByprofileLinkName(profileLinkName: String) : UserProfile
  def findByowner(owner: UserCredential) : UserProfile

}
