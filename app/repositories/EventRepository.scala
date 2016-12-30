package repositories

import models.event.EventData
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.neo4j.annotation.{QueryResult, Query, ResultColumn}
import org.springframework.data.neo4j.repository.GraphRepository
import models.{UserProfile, UserCredential, Event}
import java.util.UUID
import java.util

import org.springframework.data.repository.query.Param

trait EventRepository extends GraphRepository[Event] {

  //@Query("MATCH (n:`Event`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): Event

  @Query("MATCH (n:`Event`) RETURN COUNT(*)")
  def getCountOfAllEvents: Int

  @Query("MATCH (uc {objectId:{0}})-[:IN_PROFILE]->(up:UserProfile)-[:HOSTS_EVENTS]-(e:Event) OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (up)-[:`MAIN_IMAGE`]-(userImage:`ContentFile`) optional match (e)-[:IMAGES]-(eventImages:`ContentFile`) optional match (e)-[g]-(ux:UserCredential) optional match (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(eventImages.storeId) as EventImages, COLLECT(mainImage.storeId) as MainImage, COLLECT(userImage.storeId) as UserImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findEvents(userObjectId: String) : util.List[EventData]

  @Query(value = "MATCH (uc {objectId:{0}})-[:IN_PROFILE]->(up:UserProfile)-[:HOSTS_EVENTS]-(e:Event) OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (up)-[:`MAIN_IMAGE`]-(userImage:`ContentFile`) optional match (e)-[:IMAGES]-(eventImages:`ContentFile`) optional match (e)-[g]-(ux:UserCredential) optional match (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(eventImages.storeId) as EventImages, COLLECT(mainImage.storeId) as MainImage, COLLECT(userImage.storeId) as UserImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName", countQuery = "MATCH (uc {objectId:{0}})-[:IN_PROFILE]->(up:UserProfile)-[:HOSTS_EVENTS]-(e:Event) OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (up)-[:`MAIN_IMAGE`]-(userImage:`ContentFile`) optional match (e)-[:IMAGES]-(eventImages:`ContentFile`) optional match (e)-[g]-(ux:UserCredential) optional match (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN Count(e.objectId)")
  def findEvents(userObjectId: String, pageable: Pageable) : Page[EventData]

  // Fetch events regardless of TagWord or County
  @Query("MATCH (up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) WHERE {role} IN up.role OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, COLLECT(userImage.storeId) as UserImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findPopularEvents(@Param("role") role: String) : util.List[EventData]
  @Query(value = "MATCH (up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) WHERE {role} IN up.role OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, COLLECT(userImage.storeId) as UserImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName", countQuery = "MATCH (up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) WHERE {role} IN up.role OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN Count(e.objectId)")
  def findPopularEvents(@Param("role") role: String, pageable: Pageable) : Page[EventData]

  // Fetch events that has TagWord defined
  @Query("MATCH (tw:`TagWord`{objectId: {tagWordId} })<-[:`TAGGED_ON`]-(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) WHERE {role} IN up.role WITH up,tw,e OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) WHERE tw.objectId = {tagWordId} RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, COLLECT(userImage.storeId) as UserImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, tw.name as TagWord, c.name as CountyName")
  def findPopularEventsWithTagWord(@Param("tagWordId") tagWordId: String, @Param("role") role: String) : util.List[EventData]
  @Query(value = "MATCH (tw:`TagWord`{objectId: {tagWordId} })<-[:`TAGGED_ON`]-(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) WHERE {role} IN up.role WITH up,tw,e OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) WHERE tw.objectId = {tagWordId} RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, COLLECT(userImage.storeId) as UserImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, tw.name as TagWord, c.name as CountyName", countQuery = "MATCH (tw:`TagWord`{objectId: {tagWordId} })<-[:`TAGGED_ON`]-(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) WHERE {role} IN up.role WITH up,tw,e OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) WHERE tw.objectId = {tagWordId} RETURN Count(e.objectId)")
  def findPopularEventsWithTagWord(@Param("tagWordId") tagWordId: String, @Param("role") role: String, pageable: Pageable) : Page[EventData]

  // Fetch events that has County defined
  @Query("MATCH (c:`County`{objectId: {countyId} })<-[:`LOCATION_AT`]-(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) WHERE {role} IN up.role WITH up,c,e OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, COLLECT(userImage.storeId) as UserImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findPopularEventsWithCounty(@Param("countyId") countyId: String, @Param("role") role: String) : util.List[EventData]
  @Query(value = "MATCH (c:`County`{objectId: {countyId} })<-[:`LOCATION_AT`]-(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) WHERE {role} IN up.role WITH up,c,e OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, COLLECT(userImage.storeId) as UserImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName", countQuery = "MATCH (c:`County`{objectId: {countyId} })<-[:`LOCATION_AT`]-(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) WHERE {role} IN up.role WITH up,c,e OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN Count(e.objectId)")
  def findPopularEventsWithCounty(@Param("countyId") countyId: String, @Param("role") role: String, pageable: Pageable) : Page[EventData]

  // Fetch events that has County and TagWord defined
  @Query("MATCH (tw:`TagWord`{objectId: {tagWordId} })<-[:`TAGGED_ON`]-(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`), (c:`County`{objectId: {countyId} })<-[:`LOCATION_AT`]-(up) WHERE {role} IN up.role WITH up,c,tw,e OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, COLLECT(userImage.storeId) as UserImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, tw.name as TagWord, c.name as CountyName")
  def findPopularEventsWithCountyAndTagWord(@Param("countyId") countyId: String, @Param("tagWordId") tagWordId: String, @Param("role") role: String) : util.List[EventData]
  @Query(value = "MATCH (tw:`TagWord`{objectId: {tagWordId} })<-[:`TAGGED_ON`]-(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`), (c:`County`{objectId: {countyId} })<-[:`LOCATION_AT`]-(up) WHERE {role} IN up.role WITH up,c,tw,e OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, COLLECT(userImage.storeId) as UserImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, tw.name as TagWord, c.name as CountyName", countQuery = "MATCH (tw:`TagWord`{objectId: {tagWordId} })<-[:`TAGGED_ON`]-(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`), (c:`County`{objectId: {countyId} })<-[:`LOCATION_AT`]-(up) WHERE {role} IN up.role WITH up,c,tw,e OPTIONAL MATCH (up)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN Count(e.objectId)")
  def findPopularEventsWithCountyAndTagWord(@Param("countyId") countyId: String, @Param("tagWordId") tagWordId: String, @Param("role") role: String, pageable: Pageable) : Page[EventData]


  def findByeventLinkName(eventLinkName: String): Event
  def findByownerProfileProfileLinkNameAndEventLinkName(profileLinkName: String, eventLinkName: String): Event
  def findByownerProfile(ownerProfile: UserProfile): util.List[Event]
  def findByownerProfileObjectId(objectId: UUID): util.List[Event]
  def findByownerProfileOwner(owner: UserCredential): util.List[Event]
  def findByownerProfileOwnerObjectId(objectId: UUID): util.List[Event]

}