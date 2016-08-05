package repositories

import java.util
import java.util.UUID

import models.Event
import models.event.BookedEventDate
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository

trait BookedEventDateRepository extends GraphRepository[BookedEventDate] {

  @Query("MATCH (n:`BookedEventDate`{objectId:{0}}) RETURN n")
  def findByobjectId(objectId: UUID): BookedEventDate

  @Query("MATCH (a)-[bookings:`BOOKED_EVENT_DATE`]->(b) RETURN bookings")
  def findAllBookedEventDates(): util.List[BookedEventDate]

  @Query("MATCH (event:`Event`{objectId:{0}})-[`EVENT_TIMES`]->(ed:`EventDate`)->[bookings:`BOOKED_EVENT_DATE`] RETURN bookings")
  def findBookedDatesByEvent(event: String): util.List[BookedEventDate]

  @Query("MATCH (ownerUp:`UserProfile`{objectId:{0}})-[:HOSTS_EVENTS]->(e:Event)-[`EVENT_TIMES`]->(ed:`EventDate`)<-[bookings:`BOOKED_EVENT_DATE`]-() RETURN bookings ORDER BY bookings.bookingDateTime DESC")
  def findAllBookedDatesInAllEventsForOwner(ownerUserProfileId: String): util.List[BookedEventDate]

  @Query("MATCH (up:`UserProfile`{objectId:{0}})-[bookings:`BOOKED_EVENT_DATE`]->(ed:`EventDate`)<-[`EVENT_TIMES`]-(event:`Event`{objectId:{1}}) RETURN bookings")
  def findBookedDatesByUserAndEvent(userWhoBooked: String, event: String): util.List[BookedEventDate]

  @Query("MATCH (up:`UserProfile`{objectId:{0}})-[bookings:`BOOKED_EVENT_DATE`]->(ed:`EventDate`) RETURN bookings")
  def findBookedDatesByUser(userWhoBooked: UUID): util.List[BookedEventDate]

}
