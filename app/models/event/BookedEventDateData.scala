package models.event

import org.springframework.data.neo4j.annotation.{QueryResult, ResultColumn}

@QueryResult
trait BookedEventDateData {

  // Event
  @ResultColumn("EventObjectId")
  def getEventObjectId() : String

  @ResultColumn("EventName")
  def getEventName() : String

  @ResultColumn("EventLinkName")
  def getEventLinkName() : String

  @ResultColumn("EventMealType")
  def getEventMealType() : String

  @ResultColumn("EventPricePerPerson")
  def getEventPricePerPerson() : java.lang.Integer

  // Booking
  @ResultColumn("BookingObjectId")
  def getBookingObjectId() : String

  @ResultColumn("BookingDateTime")
  def getBookingDateTime() : java.util.Date

  @ResultColumn("BookedAtDateTime")
  def getBookedAtDateTime() : java.util.Date

  @ResultColumn("BookingNrOfGuests")
  def getBookingNrOfGuests() : java.lang.Integer

  @ResultColumn("BookingGuestComment")
  def getBookingGuestComment() : String

  @ResultColumn("EmailToGuest")
  def getEmailToGuest() : String


  // UserProfile
  @ResultColumn("ProfileLinkName")
  def getHostProfileLinkName() : String

  @ResultColumn("AddressToHost")
  def getAddressToHost() : String

  @ResultColumn("CityToHost")
  def getCityToHost() : String

  @ResultColumn("ZipCodeToHost")
  def getZipCodeToHost() : String

  @ResultColumn("CountyToHost")
  def getCountyToHost() : String

  @ResultColumn("PhoneNumberToHost")
  def getPhoneNumberToHost() : String

  @ResultColumn("EmailToHost")
  def getEmailToHost() : String

  @ResultColumn("GuestFirstName")
  def getGuestFirstName() : String

  @ResultColumn("GuestLastName")
  def getGuestLastName() : String

  @ResultColumn("GuestProfileLinkName")
  def getGuestProfileLinkName() : String

  @ResultColumn("GuestPhone")
  def getGuestPhone() : String

}

