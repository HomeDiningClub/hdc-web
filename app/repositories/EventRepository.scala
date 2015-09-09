package repositories

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.neo4j.annotation.{QueryResult, Query, ResultColumn, MapResult}
import org.springframework.data.neo4j.repository.GraphRepository
import models.{UserProfile, UserCredential, Event}
import java.util.UUID
import java.util

trait EventRepository extends GraphRepository[Event] {

  // Auto-mapped by Spring
  @Query("MATCH (n:`Event`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): Event

  @Query("MATCH (n:`Event`) RETURN COUNT(*)")
  def getCountOfAll(): Int

  @Query("MATCH (tag {objectId:{0}})-[:IN_PROFILE]->(uc:UserProfile)-[:HOSTS_EVENTS]-(r:Event) optional match (tag)-[:IN_PROFILE]->(uc:UserProfile)-[:HOSTS_EVENTS]-(r:Event) optional match (r)-[:IMAGES]-(eventImages:`ContentFile`) optional match (r)-[g]-(ux:UserCredential) optional match (r)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return r.name, r.preAmble, r.mainBody, r.objectId, COLLECT(eventImages.storeId) as EventImages, COLLECT(mainImage.storeId) as MainImage, uc.profileLinkName, r.eventLinkName, tag.userId")
  def findEvents(userObjectId: String) : util.List[EventData]

  @Query("MATCH (tag {objectId:{0}})-[:IN_PROFILE]->(uc:UserProfile)-[:HOSTS_EVENTS]-(r:Event) optional match (tag)-[:IN_PROFILE]->(uc:UserProfile)-[:HOSTS_EVENTS]-(r:Event) optional match (r)-[:IMAGES]-(eventImages:`ContentFile`) optional match (r)-[g]-(ux:UserCredential) optional match (r)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return r.name, r.preAmble, r.mainBody, r.objectId, COLLECT(eventImages.storeId) as EventImages, COLLECT(mainImage.storeId) as MainImage, uc.profileLinkName, r.eventLinkName, tag.userId")
  def findEventsOnPage(userObjectId: String, pageable: Pageable) : Page[EventData]

  def findByeventLinkName(eventLinkName: String): Event
  def findByownerProfileProfileLinkNameAndEventLinkName(profileLinkName: String, eventLinkName: String): Event
  def findByownerProfile(ownerProfile: UserProfile): util.List[Event]
  def findByownerProfileObjectId(objectId: UUID): util.List[Event]
  def findByownerProfileOwner(owner: UserCredential): util.List[Event]
  def findByownerProfileOwnerObjectId(objectId: UUID): util.List[Event]


  @QueryResult
  trait EventData {

    @ResultColumn("uc.profileLinkName")
    def getprofileLinkName() : String

    @ResultColumn("r.eventLinkName")
    def getLinkName() : String

    @ResultColumn("r.name")
    def getName() : String

    @ResultColumn("r.objectId")
    def getobjectId() : String

    @ResultColumn("r.preAmble")
    def getpreAmble() : String

    @ResultColumn("r.mainBody")
    def getMainBody() : String

    @ResultColumn("EventImages")
    def getEventImage() : util.List[String]

    @ResultColumn("MainImage")
    def getMainImage() : util.List[String]

    @ResultColumn("tag.userId")
    def getUserId() : String

  }
}