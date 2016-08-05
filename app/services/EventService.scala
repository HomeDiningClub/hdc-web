package services

import java.security.InvalidParameterException
import java.time.{LocalTime, LocalDate, LocalDateTime}
import javax.inject.{Singleton, Named, Inject}
import models.files.ContentFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import play.api.libs.mailer.Email
import traits.TransactionSupport
import scala.language.existentials
import repositories._
import models.{Event, UserProfile, UserCredential}
import scala.collection.JavaConverters._
import scala.List
import java.util.UUID
import models.viewmodels.{EventDateSuggestionSuccess, EmailAndName, EventBookingSuccess, EventBox}
import controllers.routes
import customUtils.Helpers
import scala.collection.mutable.ListBuffer
import models.event.{BookedEventDate, EventDate, MealType}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import enums.SortOrderEnums
import enums.SortOrderEnums.SortOrderEnums
import org.joda.time.DateTime
import play.api.Logger
import models.formdata._
import customUtils.formhelpers.Formats._

class EventService @Inject()(val template: Neo4jTemplate,
                             val eventRepository: EventRepository,
                             val eventDateRepository: EventDateRepository,
                             val bookedEventDateRepository: BookedEventDateRepository,
                             val mailService: MailService,
                             val messagesApi: MessagesApi) extends TransactionSupport with I18nSupport {

  implicit object LocalDateTimeOrdering extends Ordering[LocalDateTime] {
    def compare(d1: LocalDateTime, d2: LocalDateTime) = d1.compareTo(d2)
  }

  def findByownerProfileProfileLinkNameAndEventLinkName(profileLinkName: String, eventLinkName: String): Option[Event] = withTransaction(template) {
    eventRepository.findByownerProfileProfileLinkNameAndEventLinkName(profileLinkName, eventLinkName) match {
      case null => None
      case profile =>
        Some(profile)
    }
  }

  def findByeventLinkName(eventLinkName: String): Option[Event] = withTransaction(template){

    var returnObject: Option[Event] = None
    if(eventLinkName.nonEmpty)
    {
      returnObject = eventRepository.findByeventLinkName(eventLinkName) match {
        case null => None
        case profile =>
          Some(profile)
      }
    }
    returnObject
  }

  def findById(objectId: UUID): Option[Event] = withTransaction(template){
    eventRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  def findEventDateById(objectId: UUID): Option[EventDate] = withTransaction(template){
    eventDateRepository.findByobjectId(objectId.toString) match {
      case null => None
      case item => Some(item)
    }
  }

  def findAllBookedDatesInAllEventsForOwner(user: UserCredential): Option[List[BookedEventDate]] = withTransaction(template){
    bookedEventDateRepository.findAllBookedDatesInAllEventsForOwner(user.getUserProfile.objectId.toString).asScala.toList match {
      case null | Nil => None
      case items => Some(items)
    }
  }

  def findBookedDatesByEvent(event: Event): Option[List[BookedEventDate]] = withTransaction(template){
    bookedEventDateRepository.findBookedDatesByEvent(event.objectId.toString).asScala.toList match {
      case null | Nil => None
      case items => Some(items)
    }
  }

  def findBookedDatesByUserAndEvent(user: UserCredential, event: Event): Option[List[BookedEventDate]] = withTransaction(template){
    bookedEventDateRepository.findBookedDatesByUserAndEvent(user.getUserProfile.objectId.toString, event.objectId.toString).asScala.toList match {
      case null | Nil => None
      case items => Some(items)
    }
  }

  def getCountOfAll: Int = withTransaction(template) {
    eventRepository.getCountOfAll()
  }

  def getListOfAll: List[Event] = withTransaction(template){
    eventRepository.findAll.iterator.asScala.toList match {
      case null => null
      case items => items
    }
  }

  def getBookingsMadeToMyEvents(user: UserCredential): Option[List[EventBookingSuccess]] = {
    this.findAllBookedDatesInAllEventsForOwner(user) match {
      case None => None
      case Some(items) => Some(items.map { booking =>
        mapEventBookingToEventBookingSuccess(None, booking)
      })
    }
  }

  def getSortedEventImages(event: Event): Option[List[ContentFile]] = {
    event.getEventImages.asScala match {
      case Nil => None
      case images =>
        Some(images.toList.sortBy(file => file.name))
    }
  }

  def getSortedEventDates(event: Event, sortOrder: SortOrderEnums = SortOrderEnums.DESC): Option[List[EventDate]] = {
    event.getEventDates.asScala match {
      case null => None
      case items =>
        Some(sortOrder match {
          case SortOrderEnums.ASC => items.toList.sortBy(date => date.getEventDateTime)
          case SortOrderEnums.DESC => items.toList.sortBy(date => date.getEventDateTime).reverse
        })
    }
  }

  def filterEventDatesValidForEditing(dates: Option[List[EventDate]]): Option[List[EventDate]] = {
    dates match {
      case Some(list) => Some(list.filter(d => d.getEventDateTime.isAfter(LocalDateTime.now())))
      case None => None
    }

  }

  def convertToEventFormDates(inputList: Option[List[EventDate]]): Option[List[EventDateForm]] = {
    inputList match {
      case None => None
      case Some(items) =>
        Some(items.map(x =>
          EventDateForm(
            id = Some(x.objectId.toString),
            date = java.time.LocalDate.of(x.getEventDateTime.getYear, x.getEventDateTime.getMonth, x.getEventDateTime.getDayOfMonth),
            time = java.time.LocalTime.of(x.getEventDateTime.getHour, x.getEventDateTime.getMinute),
            guestsBooked = x.getGuestsBooked
          ))
        )
    }
  }

  def convertToCommaSepStringOfObjectIds(listOfFiles: Option[List[ContentFile]]): Option[String] = {
    listOfFiles match {
      case None => None
      case Some(items) =>
        Some(items.map(_.objectId).mkString(","))
    }
  }

  def eventFormMapping: Form[EventForm] = {
    Form(
      mapping(
        "id" -> optional(text),
        "name" -> nonEmptyText(minLength = 6, maxLength = 60),
        "preamble" -> optional(text(maxLength = 150)),
        "body" -> optional(text),
        "price" -> number(min = 0, max = 9999),
        "minNrOfGuests" -> number(min=1, max=9),
        "maxNrOfGuests" -> number(min=1, max=9),
        "mainimage" -> optional(text),
        "images" -> optional(text),
        "eventDates" -> optional(list(
          mapping(
            "id" -> optional(text),
            "date" -> of[java.time.LocalDate],
            "time" -> of[java.time.LocalTime],
            //"time" -> nonEmptyText(minLength = 5, maxLength = 5).verifying(Messages("event.edit.add.time.validation.format-error"), { t => isValidTime(t)} ),
            "guestsbooked" -> number(min = 0)
          )(EventDateForm.apply)(EventDateForm.unapply)
        )),
        "eventOptionsForm" -> mapping(
          "childFriendly" -> boolean,
          "handicapFriendly" -> boolean,
          "havePets" -> boolean,
          "smokingAllowed" -> boolean,
          "alcoholServing" -> optional(uuid),
          "mealType" -> optional(uuid)
        )(EventOptionsForm.apply)(EventOptionsForm.unapply)
      )(EventForm.apply)(EventForm.unapply)
        verifying(Messages("event.edit.add.min-nr-of-guests.validation"), t => isValidMinValue(t.minNoOfGuest, t.maxNoOfGuest))
        verifying(Messages("event.edit.add.max-nr-of-guests.validation"), t => isValidMaxValue(t.minNoOfGuest, t.maxNoOfGuest))
    )
  }

  def eventBookingFormMapping: Form[EventBookingForm] = {
    Form(
      mapping(
        "eventId" -> uuid,
        "book-eventDateId" -> optional(uuid),
        "book-date" -> optional(of[java.time.LocalDateTime]),
        "book-guests" -> number(min = 1, max = 9),
        "book-comment" -> optional(text)
      )(EventBookingForm.apply)(EventBookingForm.unapply)
    )
  }

  def eventDateSuggestionFormMapping: Form[EventDateSuggestionForm] = {
    Form(
      mapping(
        "suggestEventId" -> uuid,
        "suggest-date" -> of[java.time.LocalDate],
        "suggest-time" -> of[java.time.LocalTime],
        "suggest-guests" -> number(min = 1, max = 9),
        "suggest-comment" -> optional(text)
      )(EventDateSuggestionForm.apply)(EventDateSuggestionForm.unapply)
    )
  }

  def getSpacesLeft(event: Event, eventDate: EventDate): Int = {
    var spaceLeft = 0
    if(event.getMaxNrOfGuests > eventDate.getGuestsBooked){
      spaceLeft = event.getMaxNrOfGuests - eventDate.getGuestsBooked
    }
    spaceLeft
  }

  def doesEventDateHasSpaceForNewBooking(event: Event, eventDate: EventDate, nrOfGuests: Int): Boolean = {
    getSpacesLeft(event, eventDate) match {
      case 0 => false
      case spacesLeft => if(spacesLeft >= nrOfGuests){ true } else { false }
    }
  }

  def isUserBookedToEventDate(eventDate: EventDate, user: UserCredential): Boolean = {
    val bookings = eventDate.getBookings.asScala
    if (bookings.nonEmpty) {
      bookings.exists(x => x.userProfile.getOwner.objectId.equals(user.objectId))
    } else {
      false
    }
  }

  private def isValidMinValue(minValue: Int, maxValue: Int): Boolean ={
    minValue <= maxValue
  }

  private def isValidMaxValue(minValue: Int, maxValue: Int): Boolean ={
    maxValue >= minValue
  }

  private def isValidTime(time: String): Boolean ={
    Helpers.isValidTime(time)
  }

  def getEventPrice(eventUUID: UUID, nrOfGuests: Int): Int = {
    verifyMinimalGuests(nrOfGuests)

    findById(eventUUID) match {
      case None => {
        Logger.debug("Not a valid event UUID")
        0
      }
      case Some(event) => event.getPrice.intValue() * nrOfGuests
    }
  }

  def getEventPrice(event: Event, nrOfGuests: Int): Int = {
    verifyMinimalGuests(nrOfGuests)
    event.getPrice.intValue() * nrOfGuests
  }

  private def verifyMinimalGuests(nrOfGuests: Int): AnyVal = {
    if (nrOfGuests == 0) {
      Logger.debug("nrOfGuests must be minimal 1")
      0
    }
  }

  def getEventTimesForDate(eventUUID: UUID, date: LocalDate): Option[List[EventDate]] = {
    findById(eventUUID) match {
      case None => {
        Logger.debug("Not a valid event UUID")
        None
      }
      case Some(event) => {
        event.getEventDates.asScala.filter(d => d.getEventDateTime.toLocalDate.isEqual(date)).toList match {
          case Nil => None
          case dateTimeList => Some(dateTimeList)
        }
      }
    }
  }

  def isUserBookedAtEvent(user: UserCredential, event: Event): Boolean = {
    this.findBookedDatesByUserAndEvent(user,event) match {
      case None => false
      case Some(items) => true
    }
  }

  def getAvailableDates(eventUUID: UUID): Option[List[LocalDate]] = {
    findById(eventUUID) match {
      case None => {
        Logger.debug("Not a valid event UUID")
        None
      }
      case Some(event) => {
        event.getEventDates.asScala.filter(d => d.getEventDateTime.isAfter(LocalDateTime.now())).toList match {
          case Nil => None
          case dateTimeList => Some(dateTimeList.map(d => d.getEventDateTime.toLocalDate))
        }
      }
    }
  }

  def addSuggestion(currentUser: UserCredential, event: Event, suggDate: LocalDate, suggTime: LocalTime, nrOfGuestsToBeBooked: Integer, comment: Option[String]): EventDateSuggestionSuccess = withTransaction(template){
    //val selectedDate = Helpers.buildDateFromDateAndTime(suggDate, suggTime)

    val successValues = EventDateSuggestionSuccess(
      eventName = event.getName,
      eventLink = event.getLink,
      date = suggDate,
      time = suggTime,
      nrOfGuests = nrOfGuestsToBeBooked,
      comment = comment,
      hostEmail = event.getOwnerProfile.getOwner.emailAddress,
      guestEmail = currentUser.emailAddress
    )

    this.createSuggestionEmail(successValues)
    successValues
  }

  def addBooking(currentUser: UserCredential, eventDate: EventDate, nrOfGuestsToBeBooked: Integer, comment: Option[String]): BookedEventDate = withTransaction(template){
    val newBooking: BookedEventDate = new BookedEventDate(currentUser.getUserProfile,nrOfGuestsToBeBooked,eventDate, comment.getOrElse(""))
    eventDate.addOrUpdateBooking(newBooking)
    newBooking
  }

  def addBookingAndSendEmail(currentUser: UserCredential, event: Event, eventDate: EventDate, nrOfGuestsToBeBooked: Integer, comment: Option[String]): EventBookingSuccess ={
    val newBooking = this.addBooking(currentUser = currentUser, eventDate = eventDate, nrOfGuestsToBeBooked = nrOfGuestsToBeBooked, comment = comment)
    val successValues: EventBookingSuccess = mapEventBookingToEventBookingSuccess(Some(currentUser), newBooking)

    // Sending it to email
    this.createBookingSuccessEmail(successValues)

    successValues
  }

  private def mapEventBookingToEventBookingSuccess(currentUser: Option[UserCredential], booking: BookedEventDate): EventBookingSuccess = withTransaction(template){

    // This might be slow
    val event = template.fetch(booking.eventDate.getOwnerEvent)

    val successValues = EventBookingSuccess(
      bookingNumber = booking.objectId,
      eventName = event.getName,
      eventLink = controllers.routes.EventPageController.viewEventByNameAndProfile(event.getOwnerProfile.profileLinkName, event.getLink).url,
      mealType = event.getMealType match {
        case null => None
        case mt => Some(mt.name)
      },
      date = booking.eventDate.getEventDateTime.toLocalDate,
      time = booking.eventDate.getEventDateTime.toLocalTime,
      locationAddress = event.getOwnerProfile.streetAddress,
      locationCity = event.getOwnerProfile.city,
      locationCounty = event.getOwnerProfile.getLocations.asScala.head.county.name,
      locationZipCode = event.getOwnerProfile.zipCode,
      phoneNumberToHost = event.getOwnerProfile.phoneNumber match {
        case "" => None
        case p => Some(p)
      },
      nrOfGuests = booking.getNrOfGuests,
      totalCost = this.getEventPrice(event, booking.getNrOfGuests),
      email = currentUser match {
        case None => ""
        case Some(user) => user.emailAddress
      }
    )
    successValues
  }

  def updateOrCreateEventDates(contentData: EventForm, event: Event) {
    if(contentData.eventDates.nonEmpty){
      for(ed <- contentData.eventDates.get){

        // Edit old date on existing event
        if(ed.id.nonEmpty && ed.guestsBooked == 0){
          event.getEventDates.asScala.toList match {
            case Nil | null => Logger.debug("Cannot find any EventDate at all on the event objectId: " + event.objectId)
            case eventDates => {
              eventDates.find(x => x.objectId.equals(UUID.fromString(ed.id.get))) match {
                case Some(matchingEventDate) => this.updateOldEventDate(ed,matchingEventDate)
                case None => Logger.debug("Cannot find earlier EventDate using UUID to update date on")
              }
            }
          }
          // Add new date on existing event
        }else if(ed.id.isEmpty && contentData.id.nonEmpty){
          event.objectId.equals(UUID.fromString(contentData.id.get)) match {
            case true => this.addEventDate(ed,event)
            case false => Logger.debug("Cannot find earlier Event using UUID, cannot add EventDate")
          }
          // Add new date on new event
        }else if(ed.id.isEmpty && contentData.id.isEmpty) {
          this.addEventDate(ed,event)
        }else{
          Logger.debug("Cannot add eventdate on event, no matching criteria is fullfilled. (Edit on existing, Add on existing, Add on new)")
        }
      }
    }
  }



  def updateOldEventDate(formDate: EventDateForm, storedDate: EventDate) {
    storedDate.setEventDateTime(Helpers.buildDateFromDateAndTime(formDate.date, formDate.time))
  }

  def addEventDate(formDate: EventDateForm, event: Event){
    val newEventDate = this.save(new EventDate(Helpers.buildDateFromDateAndTime(formDate.date, formDate.time)))
    event.addEventDate(newEventDate)
    this.save(event)
  }

  def createBookingSuccessEmail(successValues: EventBookingSuccess): Email = {
    mailService.createAndSendMailNoReply(
      subject =  Messages("event.book.success.email.subject", successValues.eventName),
      message = Messages("event.book.success.email.body") + views.html.event.bookingSuccessDetails(successValues).toString(),
      recipient = EmailAndName(successValues.email,successValues.email),
      from = mailService.getDefaultAnonSender
    )
  }

  def createSuggestionEmail(successValues: EventDateSuggestionSuccess): Email = {
    mailService.createAndSendMailNoReply(
      subject =  Messages("event.suggest.email.subject"),
      message = Messages("event.suggest.email.body", successValues.eventName) + views.html.event.suggestionSuccessDetails(successValues).toString(),
      recipient = EmailAndName(successValues.hostEmail,successValues.hostEmail),
      from = mailService.getDefaultAnonSender
    )
  }


  def getEventBoxes(user: UserCredential): Option[List[EventBox]] = withTransaction(template){
    // Without paging
    this.getEventBoxesPage(user, 0)
  }

  def getEventBoxesPage(user: UserCredential, pageNo: Integer): Option[List[EventBox]] = withTransaction(template){

    // With paging
    // 0 current page, 6 number of items for each page

    val list = eventRepository.findEventsOnPage(user.objectId.toString, new PageRequest(pageNo, 6))
    val iterator = list.iterator()
    var eventList : ListBuffer[EventBox] = new ListBuffer[EventBox]
    val location = user.getUserProfile.getLocations.asScala.headOption match {
      case None => None
      case Some(countyTag) => Some(countyTag.county.name)
    }
    while(iterator.hasNext) {

      val obj = iterator.next()

      // Rating
//      val v = obj.getRating() match {
//        case null => "0.0"
//        case _ => obj.getRating()
//      }

      // Convert string to double, round to Int and convert to Int
//      val ratingValue : Int = v.toDouble.round.toInt

      // Link
      val linkToEvent = (obj.getprofileLinkName(), obj.getLinkName()) match {
        case (null|null) | ("","") => "#"
        case (profLink,evtLink) => routes.EventPageController.viewEventByNameAndProfile(profLink, evtLink).url
      }

      // Image
      var mainImage = Some("/assets/images/event/event-box-default.png")
      if(obj.getMainImage().iterator().hasNext){
        mainImage = Some(routes.ImageController.eventBox(obj.getMainImage().iterator().next()).url)
      }

      // Build return-list
      var event = EventBox(
        Some(UUID.fromString(obj.getobjectId())),
        linkToEvent,
        obj.getName(),
        obj.getpreAmble() match {
        case "" | null =>
          var retBody = Helpers.removeHtmlTags(obj.getMainBody())

          if (retBody.length > 125)
            retBody = retBody.substring(0, 125) + "..."

          Some(retBody)
        case content => Some(content)
        },
        mainImage,
        obj.getPrice() match {
          case null => 0
          case p => p
        },
        location,
//        ratingValue,
        list.getTotalElements,
        list.hasNext,
        list.hasPrevious,
        list.getTotalPages
      )
      eventList += event
    }

    val returnBoxes: List[EventBox] = eventList.toList

    if(returnBoxes.isEmpty)
      None
    else
      Some(returnBoxes)
  }



  def getListOwnedBy(user: UserCredential): Option[List[Event]] = withTransaction(template){
    eventRepository.findByownerProfileOwner(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  def getListOwnedBy(userProfile: UserProfile): Option[List[Event]] = withTransaction(template){
    eventRepository.findByownerProfile(userProfile).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  def deleteById(objectId: UUID): Boolean = withTransaction(template){
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        item.deleteMainImage()
        item.deleteEventImages()
        item.deleteEventDates()
        //item.deleteRatings()
        item.deleteLikes()
        eventRepository.delete(item)
        true
    }
  }

  // Fetching
  def fetchEvent(event: Event): Event = withTransaction(template){
    template.fetch(event)
  }

  private def deleteAll() = withTransaction(template){
    eventRepository.deleteAll()
  }

  def save(newContent: Event): Event = withTransaction(template){
    eventRepository.save(newContent)
  }

  def save(newContent: EventDate): EventDate = withTransaction(template){
    eventDateRepository.save(newContent)
  }


}
