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

  // Auto-mapped by Spring
  //@Query("MATCH (n:`Event`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): Event

  @Query("MATCH (n:`Event`) RETURN COUNT(*)")
  def getCountOfAll(): Int


  @Query("MATCH (uc {objectId:{0}})-[:IN_PROFILE]->(up:UserProfile)-[:HOSTS_EVENTS]-(e:Event) OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) optional match (e)-[:IMAGES]-(eventImages:`ContentFile`) optional match (e)-[g]-(ux:UserCredential) optional match (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(eventImages.storeId) as EventImages, COLLECT(mainImage.storeId) as MainImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findEvents(userObjectId: String) : util.List[EventData]

  @Query("MATCH (uc {objectId:{0}})-[:IN_PROFILE]->(up:UserProfile)-[:HOSTS_EVENTS]-(e:Event) OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) optional match (e)-[:IMAGES]-(eventImages:`ContentFile`) optional match (e)-[g]-(ux:UserCredential) optional match (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(eventImages.storeId) as EventImages, COLLECT(mainImage.storeId) as MainImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findEvents(userObjectId: String, pageable: Pageable) : Page[EventData]

  // Fetch events regardless of TagWord or County
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findPopularEvents() : util.List[EventData]
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`) OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findPopularEvents(pageable: Pageable) : Page[EventData]

  // Fetch events that has TagWord defined
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`), (up)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH up,tw,uc,e OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) WHERE tw.objectId = {tagWordId} RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, tw.tagName as TagName, c.name as CountyName")
  def findPopularEventsWithTagWord(@Param("tagWordId") tagWordId: String) : util.List[EventData]
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`), (up)-[:`TAGGED_ON`]->(tw:`TagWord`) WITH up,tw,uc,e OPTIONAL MATCH (c:`County`)<-[:`LOCATION_AT`]-(up) OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) WHERE tw.objectId = {tagWordId} RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, tw.tagName as TagName, c.name as CountyName")
  def findPopularEventsWithTagWord(@Param("tagWordId") tagWordId: String, pageable: Pageable) : Page[EventData]

  // Fetch events that has County defined
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`), (c:`County`)<-[:`LOCATION_AT`]-(up) WITH up,c,uc,e OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) WHERE c.objectId = {countyId} RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findPopularEventsWithCounty(@Param("countyId") countyId: String) : util.List[EventData]
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`), (c:`County`)<-[:`LOCATION_AT`]-(up) WITH up,c,uc,e OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) WHERE c.objectId = {countyId} RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findPopularEventsWithCounty(@Param("countyId") countyId: String, pageable: Pageable) : Page[EventData]

  // Fetch events that has County and TagWord defined
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`), (c:`County`)<-[:`LOCATION_AT`]-(up) WITH up,c,uc,e OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) WHERE c.objectId = {countyId} RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findPopularEventsWithCountyAndTagWord(@Param("countyId") countyId: String, @Param("tagWordId") tagWordId: String) : util.List[EventData]
  @Query("MATCH (uc:`UserCredential`)-[:`IN_PROFILE`]->(up:`UserProfile`)-[:`HOSTS_EVENTS`]-(e:`Event`), (c:`County`)<-[:`LOCATION_AT`]-(up) WITH up,c,uc,e OPTIONAL MATCH (e)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) WHERE c.objectId = {countyId} RETURN e.name as EventName, e.preAmble as EventPreAmble, e.mainBody as EventMainBody, e.objectId as EventObjectId, COLLECT(mainImage.storeId) as MainImage, e.price as EventPrice, up.profileLinkName as ProfileLinkName, e.eventLinkName as EventLinkName, c.name as CountyName")
  def findPopularEventsWithCountyAndTagWord(@Param("countyId") countyId: String, @Param("tagWordId") tagWordId: String, pageable: Pageable) : Page[EventData]


  def findByeventLinkName(eventLinkName: String): Event
  def findByownerProfileProfileLinkNameAndEventLinkName(profileLinkName: String, eventLinkName: String): Event
  def findByownerProfile(ownerProfile: UserProfile): util.List[Event]
  def findByownerProfileObjectId(objectId: UUID): util.List[Event]
  def findByownerProfileOwner(owner: UserCredential): util.List[Event]
  def findByownerProfileOwnerObjectId(objectId: UUID): util.List[Event]

}