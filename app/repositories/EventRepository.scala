package repositories

import models.event.EventData
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.neo4j.annotation.{QueryResult, Query, ResultColumn}
import org.springframework.data.neo4j.repository.GraphRepository
import models.{UserProfile, UserCredential, Event}
import java.util.UUID
import java.util

trait EventRepository extends GraphRepository[Event] {

  // Auto-mapped by Spring
  //@Query("MATCH (n:`Event`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): Event

  @Query("MATCH (n:`Event`) RETURN COUNT(*)")
  def getCountOfAll(): Int

  @Query("MATCH (uc {objectId:{0}})-[:IN_PROFILE]->(up:UserProfile)-[:HOSTS_EVENTS]-(r:Event) optional match (uc)-[:IN_PROFILE]->(up:UserProfile)-[:HOSTS_EVENTS]-(r:Event) optional match (r)-[:IMAGES]-(eventImages:`ContentFile`) optional match (r)-[g]-(ux:UserCredential) optional match (r)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return r.name as EventName, r.preAmble as EventPreAmble, r.mainBody as EventMainBody, r.objectId as EventObjectId, COLLECT(eventImages.storeId) as EventImages, COLLECT(mainImage.storeId) as MainImage, r.price as EventPrice, up.profileLinkName as ProfileLinkName, r.eventLinkName as EventLinkName, uc.userId as UserCredUserId")
  def findEvents(userObjectId: String) : util.List[EventData]

  @Query("MATCH (uc {objectId:{0}})-[:IN_PROFILE]->(up:UserProfile)-[:HOSTS_EVENTS]-(r:Event) optional match (uc)-[:IN_PROFILE]->(up:UserProfile)-[:HOSTS_EVENTS]-(r:Event) optional match (r)-[:IMAGES]-(eventImages:`ContentFile`) optional match (r)-[g]-(ux:UserCredential) optional match (r)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return r.name as EventName, r.preAmble as EventPreAmble, r.mainBody as EventMainBody, r.objectId as EventObjectId, COLLECT(eventImages.storeId) as EventImages, COLLECT(mainImage.storeId) as MainImage, r.price as EventPrice, up.profileLinkName as ProfileLinkName, r.eventLinkName as EventLinkName, uc.userId as UserCredUserId")
  def findEventsOnPage(userObjectId: String, pageable: Pageable) : Page[EventData]

  def findByeventLinkName(eventLinkName: String): Event
  def findByownerProfileProfileLinkNameAndEventLinkName(profileLinkName: String, eventLinkName: String): Event
  def findByownerProfile(ownerProfile: UserProfile): util.List[Event]
  def findByownerProfileObjectId(objectId: UUID): util.List[Event]
  def findByownerProfileOwner(owner: UserCredential): util.List[Event]
  def findByownerProfileOwnerObjectId(objectId: UUID): util.List[Event]

}