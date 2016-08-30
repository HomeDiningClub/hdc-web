package repositories

import java.util
import java.util.UUID
import models.event.{BookedEventDate, BookedEventDateData}
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository

trait BookedEventDateRepository extends GraphRepository[BookedEventDate] {

  @Query("MATCH (a)-[booking:`BOOKED_EVENT_DATE`{objectId:{0}}]->(b) RETURN booking")
  def findByobjectId(objectId: String): BookedEventDate

  @Query("MATCH (a)-[bookings:`BOOKED_EVENT_DATE`]->(b) RETURN bookings")
  def findAllBookedEventDates(): util.List[BookedEventDate]

  @Query("MATCH (event:`Event`{objectId:{0}})-[`EVENT_TIMES`]->(ed:`EventDate`)->[bookings:`BOOKED_EVENT_DATE`] RETURN bookings")
  def findBookedDatesByEvent(event: String): util.List[BookedEventDate]

  @Query("MATCH (up:`UserProfile`{objectId:{0}})-[bookings:`BOOKED_EVENT_DATE`]->(ed:`EventDate`)<-[`EVENT_TIMES`]-(event:`Event`{objectId:{1}}) RETURN bookings")
  def findBookedDatesByUserAndEvent(userWhoBooked: String, event: String): util.List[BookedEventDate]

  @Query("MATCH (bookerUc:`UserCredential`)-[:`IN_PROFILE`]->(bookerUp:`UserProfile`)-[bed:`BOOKED_EVENT_DATE`]->(ed:`EventDate`)<-[`EVENT_TIMES`]-(e:`Event`)<-[`HOSTS_EVENTS`]-(hostUp:`UserProfile`{objectId:{0}})<-[:`IN_PROFILE`]-(hostUc:`UserCredential`) OPTIONAL MATCH (e)<-[:`MEAL_TYPE`]-(mt:`MealType`) OPTIONAL MATCH (hostUp)-[:`LOCATION_AT`]->(c:`County`) RETURN bed.objectId as BookingObjectId, bed.bookingDateTime as BookingDateTime, bed.bookedAtDateTime as BookedAtDateTime, bed.nrOfGuests as BookingNrOfGuests, bed.comment as BookingGuestComment, hostUp.profileLinkName as ProfileLinkName, hostUp.streetAddress as AddressToHost, hostUp.city as CityToHost, hostUp.zipCode as ZipCodeToHost, hostUp.phoneNumber as PhoneNumberToHost, e.eventLinkName as EventLinkName, e.name as EventName, e.objectId as EventObjectId, e.price as EventPricePerPerson, mt.name as EventMealType, c.name as CountyToHost, hostUc.emailAddress as EmailToHost, bookerUc.emailAddress as EmailToGuest ORDER BY BookingDateTime DESC")
  def findAllBookedDatesInAllEventsForOwner(ownerUserProfileId: String): util.List[BookedEventDateData]

  @Query("MATCH (bookerUc:`UserCredential`)-[:`IN_PROFILE`]->(bookerUp:`UserProfile`{objectId:{0}})-[bed:`BOOKED_EVENT_DATE`]->(ed:`EventDate`)<-[`EVENT_TIMES`]-(e:`Event`)<-[`HOSTS_EVENTS`]-(hostUp:`UserProfile`)<-[:`IN_PROFILE`]-(hostUc:`UserCredential`) OPTIONAL MATCH (e)<-[:`MEAL_TYPE`]-(mt:`MealType`) OPTIONAL MATCH (hostUp)-[:`LOCATION_AT`]->(c:`County`) RETURN bed.objectId as BookingObjectId, bed.bookingDateTime as BookingDateTime, bed.bookedAtDateTime as BookedAtDateTime, bed.nrOfGuests as BookingNrOfGuests, bed.comment as BookingGuestComment, hostUp.profileLinkName as ProfileLinkName, hostUp.streetAddress as AddressToHost, hostUp.city as CityToHost, hostUp.zipCode as ZipCodeToHost, hostUp.phoneNumber as PhoneNumberToHost, e.eventLinkName as EventLinkName, e.name as EventName, e.objectId as EventObjectId, e.price as EventPricePerPerson, mt.name as EventMealType, c.name as CountyToHost, hostUc.emailAddress as EmailToHost, bookerUc.emailAddress as EmailToGuest ORDER BY BookingDateTime DESC")
  def findBookedDatesByUserWhoBooked(userWhoBooked: String): util.List[BookedEventDateData]

  @Query("MATCH (bookerUc:`UserCredential`)-[:`IN_PROFILE`]->(bookerUp:`UserProfile`)-[bed:`BOOKED_EVENT_DATE`{objectId:{0}}]->(ed:`EventDate`)<-[`EVENT_TIMES`]-(e:`Event`)<-[`HOSTS_EVENTS`]-(hostUp:`UserProfile`)<-[:`IN_PROFILE`]-(hostUc:`UserCredential`) OPTIONAL MATCH (e)<-[:`MEAL_TYPE`]-(mt:`MealType`) OPTIONAL MATCH (hostUp)-[:`LOCATION_AT`]->(c:`County`) RETURN bed.objectId as BookingObjectId, bed.bookingDateTime as BookingDateTime, bed.bookedAtDateTime as BookedAtDateTime, bed.nrOfGuests as BookingNrOfGuests, bed.comment as BookingGuestComment, hostUp.profileLinkName as ProfileLinkName, hostUp.streetAddress as AddressToHost, hostUp.city as CityToHost, hostUp.zipCode as ZipCodeToHost, hostUp.phoneNumber as PhoneNumberToHost, e.eventLinkName as EventLinkName, e.name as EventName, e.objectId as EventObjectId, e.price as EventPricePerPerson, mt.name as EventMealType, c.name as CountyToHost, hostUc.emailAddress as EmailToHost, bookerUc.emailAddress as EmailToGuest ORDER BY BookingDateTime DESC")
  def findByObjectIdWithData(objectId: String): BookedEventDateData


}
