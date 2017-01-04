package services

import java.time.{LocalDate, LocalDateTime, LocalTime}
import javax.inject.Inject

import models.files.ContentFile
import models.location.County
import models.profile.TagWord
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.data.neo4j.conversion.ResultConverter
import org.springframework.data.neo4j.support.Neo4jTemplate
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.mailer.Email
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import traits.TransactionSupport

import scala.language.existentials
import repositories._
import models.{Event, UserCredential, UserProfile}

import scala.collection.JavaConverters._
import java.util.UUID

import models.viewmodels._
import controllers.routes
import customUtils.Helpers

import scala.collection.mutable.ListBuffer
import models.event.{BookedEventDate, BookedEventDateData, EventData, EventDate}
import play.api.data.Form
import play.api.data.Forms._
import enums.SortOrderEnums
import enums.SortOrderEnums.SortOrderEnums

import scala.collection.JavaConverters._
import play.api.Logger
import models.formdata._
import customUtils.formhelpers.Formats._
import models.message.Message
import models.modelconstants.UserLevelScala

class EventService @Inject()(val template: Neo4jTemplate,
                             val eventRepository: EventRepository,
                             val eventDateRepository: EventDateRepository,
                             val recipeRepository: RecipeRepository,
                             val bookedEventDateRepository: BookedEventDateRepository,
                             val messageService: MessageService,
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

  def findAllBookedDatesInAllEventsForOwner(user: UserCredential): Option[List[BookedEventDateData]] = withTransaction(template){
    bookedEventDateRepository.findAllBookedDatesInAllEventsForOwner(user.getUserProfile.objectId.toString).asScala.toList match {
      case null | Nil => None
      case items => Some(items)
    }
  }

  def findBookedDatesByUserWhoBooked(user: UserCredential): Option[List[BookedEventDateData]] = withTransaction(template){
    bookedEventDateRepository.findBookedDatesByUserWhoBooked(user.getUserProfile.objectId.toString).asScala.toList match {
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

  def getCountOfAllEvents: Int = withTransaction(template) {
    eventRepository.getCountOfAllEvents
  }

  def getCountOfAllEventBookings: Int = withTransaction(template) {
    bookedEventDateRepository.getCountOfAllEventBookings
  }

  def getCountOfMyEvents(userCredential: UserCredential): Int = withTransaction(template) {
    eventRepository.getCountOfMyEvents(userCredential.getUserProfile.objectId.toString)
  }

  def getListOfAll: List[Event] = withTransaction(template){
    eventRepository.findAll.iterator.asScala.toList match {
      case null => null
      case items => items
    }
  }

  def getBookingsMadeToMyEvents(user: UserCredential, baseUrl: String): Option[List[EventBookingSuccess]] = {
    this.findAllBookedDatesInAllEventsForOwner(user) match {
      case None => None
      case Some(items) => Some(items.map { booking =>
        mapEventBookingToEventBookingSuccess(booking, baseUrl)
      })
    }
  }

  def getBookingsMadeByMe(user: UserCredential, baseUrl: String): Option[List[EventBookingSuccess]] = {
    this.findBookedDatesByUserWhoBooked(user) match {
      case None => None
      case Some(items) => Some(items.map { booking =>
        mapEventBookingToEventBookingSuccess(booking, baseUrl)
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
        )(EventOptionsForm.apply)(EventOptionsForm.unapply),
        "userProfileOptionsForm" -> optional(mapping(
          "payCash" -> boolean,
          "paySwish" -> boolean,
          "payBankCard" -> boolean,
          "payIZettle" -> boolean,
          "wantsToBeHost" -> boolean
        )(UserProfileOptionsForm.apply)(UserProfileOptionsForm.unapply)),
        "eventDatesToDelete" -> optional(list(uuid))
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
        "suggest-date" -> of[java.time.LocalDate].verifying(Messages("event.suggestion.add.date.validation"), d => d.isInstanceOf[java.time.LocalDate]),
        "suggest-time" -> of[java.time.LocalTime].verifying(Messages("event.suggestion.add.time.validation"), d => d.isInstanceOf[java.time.LocalTime]),
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

  def isUserBookedToEventDate(eventDate: EventDate, user: UserCredential): Boolean = withTransaction(template){
    val bookings = eventDate.getBookings.asScala
    if (bookings.nonEmpty) {
      bookings.exists(x => template.fetch(x.userProfile).getOwner.objectId.equals(user.objectId))
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
      case Some(event) => calculatePrice(event.getPrice.intValue(), nrOfGuests)
    }
  }

  def getEventPrice(event: Event, nrOfGuests: Int): Int = {
    verifyMinimalGuests(nrOfGuests)
    calculatePrice(event.getPrice.intValue(), nrOfGuests)
  }

  private def calculatePrice(price: Int, nrOfGuests: Int): Int = {
    price * nrOfGuests
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



  def addSuggestionAndSendEmail(userSendingSuggestion: UserCredential, event: Event, suggDate: LocalDate, suggTime: LocalTime, nrOfGuestsToBeBooked: Integer, comment: Option[String], baseUrl: String): EventDateSuggestionSuccess = withTransaction(template){
    //val selectedDate = Helpers.buildDateFromDateAndTime(suggDate, suggTime)
    val guestUserProfileLinkName = userSendingSuggestion.getUserProfile.profileLinkName

    val successValues = EventDateSuggestionSuccess(
      eventName = event.getName,
      eventLink = baseUrl + routes.EventPageController.viewEventByNameAndProfile(event.getOwnerProfile.profileLinkName, event.getLink).url,
      date = suggDate,
      time = suggTime,
      nrOfGuests = nrOfGuestsToBeBooked,
      hostEmail = event.getOwnerProfile.getOwner.emailAddress,
      host = event.getOwnerProfile.getOwner,
      guestEmail = userSendingSuggestion.emailAddress,
      guestComment = comment,
      guestFullName = userSendingSuggestion.getFullName,
      guestProfileName = guestUserProfileLinkName,
      guestProfileLink = baseUrl + routes.UserProfileController.viewProfileByName(guestUserProfileLinkName).url,
      guestPhone = userSendingSuggestion.getUserProfile.phoneNumber match {
        case "" | null => None
        case p => Some(p)
      }
    )

    // Sending Guest a notice email
    this.sendSuggestionSuccessEmailToGuest(successValues, baseUrl)

    // Send a message to Hosts-inbox
    val message = this.sendSuggestionMessageToHostInbox(userSendingSuggestion, event, suggDate, suggTime, nrOfGuestsToBeBooked, successValues)

    // Send a message to Guest-inbox
    this.sendSuggestionMessageToGuestInbox(userSendingSuggestion, event, successValues, message)

    // Extra mail to host
    this.sendSuggestionSuccessEmailToHost(successValues, baseUrl)
    successValues
  }

  def addBooking(currentUser: UserCredential, eventDate: EventDate, nrOfGuestsToBeBooked: Integer, comment: Option[String]): BookedEventDate = withTransaction(template){
    val newBooking: BookedEventDate = new BookedEventDate(currentUser.getUserProfile,nrOfGuestsToBeBooked,eventDate, comment.getOrElse(""))
    eventDate.addOrUpdateBooking(newBooking)
    newBooking
  }

  def addBookingAndSendEmail(currentUser: UserCredential, event: Event, eventDate: EventDate, nrOfGuestsToBeBooked: Integer, comment: Option[String], baseUrl: String): EventBookingSuccess = {
    // Add the booking
    val newBooking = this.addBooking(currentUser = currentUser, eventDate = eventDate, nrOfGuestsToBeBooked = nrOfGuestsToBeBooked, comment = comment)

    // Re-fetch with more information
    val newBookingData = bookedEventDateRepository.findByObjectIdWithData(newBooking.objectId.toString)

    // Map as normal
    val successValues: EventBookingSuccess = mapEventBookingToEventBookingSuccess(newBookingData, baseUrl)

    // Send a message to Hosts-inbox
    val message = this.sendBookingSuccessMessageToHostInbox(currentUser, event, eventDate, nrOfGuestsToBeBooked, comment, successValues)

    // Send a message to Guest-inbox
    this.sendBookingSuccessMessageToGuestInbox(currentUser, event, message, successValues)

    // Sending Host a notice email
    this.sendBookingSuccessEmailToHost(successValues, baseUrl)

    // Sending Guest a copy to email
    this.sendBookingSuccessEmailToGuest(successValues)

    successValues
  }


  def updateOrCreateEventDates(contentData: EventForm, event: Event) = withTransaction(template){
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
          Logger.debug("Cannot add EventDate on event, no matching criteria is fullfilled. (Edit on existing, Add on existing, Add on new)")
        }
      }
    }
  }

  def deleteEventDates(contentData: EventForm, event: Event) {
    // Delete event dates, if any
    if(contentData.eventDatesToDelete.nonEmpty) {
      for (ed <- contentData.eventDatesToDelete.get) {
        this.deleteEventDateById(ed, event)
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



  //region Sending messages
  private def sendBookingSuccessMessageToHostInbox(currentUser: UserCredential, event: Event, eventDate: EventDate, nrOfGuestsToBeBooked: Integer, comment: Option[String], successValues: EventBookingSuccess): Message = {
    val body = Html(Messages("event.book.success.to-host.inbox.body.part01", successValues.eventName) +
      " <br>" +
      views.html.event.bookingSuccessDetails(successValues) +
      Messages("event.book.success.to-host.inbox.body.part02")).toString()

    messageService.createRequest(
      user = currentUser,
      host = event.getOwnerProfile.getOwner,
      date = Helpers.castLocalDateToDate(eventDate.getEventDateTime.toLocalDate),
      time = Helpers.castLocalTimeToDate(eventDate.getEventDateTime.toLocalTime),
      numberOfGuests = nrOfGuestsToBeBooked,
      request = body,
      phone = currentUser.getPhone match {
        case null | "" => None
        case p => Some(p)
      })
  }

  private def sendBookingSuccessMessageToGuestInbox(userSendingBooking: UserCredential, event: Event, message: Message, successValues: EventBookingSuccess) {
    val body = Html(Messages("event.book.success.to-guest.inbox.body.part01", successValues.eventName) +
      " <br>" +
      Messages("event.book.success.to-guest.inbox.body.part02") +
      "<br><br>" +
      views.html.event.bookingSuccessDetails(successValues) +
      Messages("event.book.success.to-guest.inbox.body.part03")).toString()

    messageService.createResponse(
      user = event.getOwnerProfile.getOwner,
      guest = userSendingBooking,
      message = message,
      response = body,
      phone = successValues.phoneNumberToHost match {
        case None => ""
        case Some(p) => p
      })
  }

  private def sendSuggestionMessageToHostInbox(userSendingSuggestion: UserCredential, event: Event, suggDate: LocalDate, suggTime: LocalTime, nrOfGuestsToBeBooked: Integer, suggestionSuccess: EventDateSuggestionSuccess): Message = {
    val body = Html(Messages("event.suggest.inbox.body.part01", suggestionSuccess.eventName) +
      views.html.event.suggestionSuccessDetails(suggestionSuccess) +
      Messages("event.suggest.inbox.body.part02")).toString()

    messageService.createRequest(
      user = userSendingSuggestion,
      host = event.getOwnerProfile.getOwner,
      date = Helpers.castLocalDateToDate(suggDate),
      time = Helpers.castLocalTimeToDate(suggTime),
      numberOfGuests = nrOfGuestsToBeBooked,
      request = body,
      phone = userSendingSuggestion.getPhone match {
        case null | "" => None
        case p => Some(p)
      })
  }

  private def sendSuggestionMessageToGuestInbox(userSendingSuggestion: UserCredential, event: Event, suggestionSuccess: EventDateSuggestionSuccess, message: Message) {
    val body = Html(Messages("event.suggest.success.body") +
      views.html.event.suggestionSuccessDetails(suggestionSuccess) +
      Messages("event.suggest.success.body-extra-info-for-inbox")).toString()

    messageService.createResponse(
      user = event.getOwnerProfile.getOwner,
      guest = userSendingSuggestion,
      message = message,
      response = body,
      phone = event.getOwnerProfile.getOwner.getPhone match {
        case null | "" => ""
        case p => p
      })
  }

  private def sendBookingSuccessEmailToGuest(successValues: EventBookingSuccess): Email = {

    // To guest
    val body = Html(Messages("event.book.success.to-guest.email.body.part01") +
      views.html.event.bookingSuccessDetails(successValues) +
      Messages("event.book.success.to-guest.email.body.part02")).toString()

    mailService.createAndSendMailNoReply(
      subject =  Messages("event.book.success.to-guest.email.subject", successValues.eventName),
      message = body,
      recipient = EmailAndName(successValues.guestEmail,successValues.guestEmail),
      from = mailService.getDefaultAnonSender
    )

  }

  private def sendBookingSuccessEmailToHost(successValues: EventBookingSuccess, baseUrl: String): Email = {

    // To Host
    val pathToHostProfile = baseUrl + successValues.hostLink
    val linkToBookings = "<a href='" + pathToHostProfile + "#bookings-tab'>" + Messages("event.book.success.to-host.email.body.link.bookings") + "</a>"
    val linkToInbox = "<a href='" + pathToHostProfile + "#inbox-tab'>" + Messages("event.book.success.to-host.email.body.link.inbox") + "</a>"
    val msgBody = Html(Messages("event.book.success.to-host.email.body.part01", linkToBookings, linkToInbox)
      + "<br><br>" +
      views.html.event.bookingSuccessDetails(successValues).toString()
      + "<br><br>" + Messages("event.book.success.to-host.email.body.part02")).toString

    mailService.createAndSendMailNoReply(
      subject =  Messages("event.book.success.to-host.email.subject", successValues.eventName),
      message = msgBody,
      recipient = EmailAndName(successValues.hostEmail,successValues.hostEmail),
      from = mailService.getDefaultAnonSender
    )

  }

  private def sendSuggestionSuccessEmailToHost(successValues: EventDateSuggestionSuccess, baseUrl: String): Email = {

    // To host
    val path = routes.UserProfileController.viewProfileByName(successValues.host.getUserProfile.profileLinkName).url
    val msgBody = Messages("event.suggest.email.body.part01", successValues.eventName) +
      views.html.event.suggestionSuccessDetails(successValues).toString() +
      Messages("event.suggest.email.body.part02", "<a href='" + (baseUrl + path) + "#inbox-tab'>" + Messages("event.suggest.email.body.part02.link") + "</a>")

    mailService.createAndSendMailNoReply(
      subject =  Messages("event.suggest.email.subject"),
      message = msgBody,
      recipient = EmailAndName(successValues.hostEmail,successValues.hostEmail),
      from = mailService.getDefaultAnonSender
    )
  }

  private def sendSuggestionSuccessEmailToGuest(successValues: EventDateSuggestionSuccess, baseUrl: String): Email = {

    // To Guest
    val path = routes.UserProfileController.viewProfileByName(successValues.host.getUserProfile.profileLinkName).url
    val msgBody = Messages("event.suggest.success.header") + " - " + Messages("event.suggest.success.sub-header") + "<br><br>"
        Messages("event.suggest.success.body") +
        views.html.event.suggestionSuccessDetails(successValues).toString()

    mailService.createAndSendMailNoReply(
      subject =  Messages("event.suggest.add.success"),
      message = msgBody,
      recipient = EmailAndName(successValues.guestEmail,successValues.hostEmail),
      from = mailService.getDefaultAnonSender
    )
  }
  //endregion


  //region Mappings
  def mapEventDataToEventBox(list: Page[EventData]): Option[List[BrowseEventBox]] = {
    var eventList : ListBuffer[BrowseEventBox] = new ListBuffer[BrowseEventBox]

    for(evtData <- list.asScala){

      // Rating
      //      val v = obj.getRating() match {
      //        case null => "0.0"
      //        case _ => obj.getRating()
      //      }

      // Convert string to double, round to Int and convert to Int
      //      val ratingValue : Int = v.toDouble.round.toInt

      // County
      val location = evtData.getCountyName() match {
        case null => None
        case countyName => Some(countyName)
      }

      // Link
      val linkToEvent = (evtData.getprofileLinkName(), evtData.getLinkName()) match {
        case (null|null) | ("","") => "#"
        case (profLink,evtLink) => routes.EventPageController.viewEventByNameAndProfile(profLink, evtLink).url
      }

      // Image
      var mainImage: Option[String] = Some("/assets/images/event/event-box-default.png")
      if(evtData.getMainImage().asScala.nonEmpty){
        mainImage = Some(routes.ImageController.eventBox(evtData.getMainImage().asScala.head).url)
      }

      // User image
      var userImage: Option[String] = None
      if(evtData.getUserImage().asScala.nonEmpty){
        userImage = Some(routes.ImageController.userThumb(evtData.getUserImage().asScala.head).url)
      }

      // Build return-list
      var event = BrowseEventBox(
        Some(UUID.fromString(evtData.getobjectId())),
        linkToEvent,
        evtData.getName(),
        Option(evtData.getMainBody()),
        evtData.getpreAmble() match {
          case "" | null =>
            var retBody = Helpers.removeHtmlTags(evtData.getMainBody())

            if (retBody.length > 80)
              retBody = retBody.substring(0, 80) + "..."

            Some(retBody)
          case content => Some(content)
        },
        mainImage,
        userImage,
        evtData.getPrice() match {
          case null => 0
          case p => p.toInt
        },
        location,
        //ratingValue,
        list.getTotalElements,
        firstBookableDateTime = evtData.getEventDateTimes().asScala.toList match {
          case Nil => None
          case javaDates =>
            Some(customUtils.Helpers.formatDate(javaDates.sorted.head, "YYYY-MM-dd"))
        },
        list.hasNext,
        list.hasPrevious,
        list.getTotalPages
      )
      eventList += event
    }

    val returnBoxes: List[BrowseEventBox] = eventList.toList

    if(returnBoxes.isEmpty)
      None
    else
      Some(returnBoxes)

  }

  private def mapEventBookingToEventBookingSuccess(booking: BookedEventDateData, baseUrl: String): EventBookingSuccess = {

    val bookedDate = customUtils.Helpers.castDateToLocalDateTime(booking.getBookingDateTime() match {
      case null => new java.util.Date()
      case bDate => bDate
    })

    val successValues = EventBookingSuccess(
      bookingNumber = UUID.fromString(booking.getBookingObjectId()),
      eventName = booking.getEventName(),
      eventLink = baseUrl + routes.EventPageController.viewEventByNameAndProfile(booking.getHostProfileLinkName(), booking.getEventLinkName()).url,
      hostLink = baseUrl + routes.UserProfileController.viewProfileByName(booking.getHostProfileLinkName()).url,
      mealType = booking.getEventMealType() match {
        case null | "" => None
        case mt => Some(mt)
      },
      date = bookedDate.toLocalDate,
      time = bookedDate.toLocalTime,
      locationAddress = booking.getAddressToHost(),
      locationCity = booking.getCityToHost(),
      locationCounty = booking.getCountyToHost(),
      locationZipCode = booking.getZipCodeToHost(),
      phoneNumberToHost = booking.getPhoneNumberToHost() match {
        case null | "" => None
        case p => Some(p)
      },
      nrOfGuests = booking.getBookingNrOfGuests(),
      guestComment =  booking.getBookingGuestComment() match {
        case null | "" => None
        case c => Some(c)
      },
      totalCost = this.calculatePrice(booking.getEventPricePerPerson(), booking.getBookingNrOfGuests()),
      guestEmail = booking.getEmailToGuest(),
      hostEmail = booking.getEmailToHost(),
      guestFullName = booking.getGuestFirstName() + " " + booking.getGuestLastName(),
      guestProfileName = booking.getHostProfileLinkName(),
      guestProfileLink = baseUrl + routes.UserProfileController.viewProfileByName(booking.getGuestProfileLinkName()).url,
      guestPhone = booking.getGuestPhone() match {
        case null | "" => None
        case p => Some(p)
      }
    )
    successValues
  }
  //endregion

  def getEventBoxes(user: UserCredential): Option[List[BrowseEventBox]] = withTransaction(template){
    this.getEventBoxes(user, 0)
  }

  def getEventBoxes(user: UserCredential, pageNo: Integer): Option[List[BrowseEventBox]] = withTransaction(template){
    // With paging - 0 current page, 6 number of items for each page
    val pagedListJava = eventRepository.findEvents(user.objectId.toString, new PageRequest(pageNo, 6))
    mapEventDataToEventBox(pagedListJava)
  }

  // Use .right.get to fetch Option[Page[EventData]]
  // Use .left.get to fetch Option[List[EventData]]
  // Or match case Left(item) => item
  def getEventsFiltered(filterTag: Option[TagWord], filterCounty: Option[County], pageNo: Option[Integer] = None, nrPerPage: Int = 9): Either[Option[List[EventData]], Option[Page[EventData]]] = withTransaction(template) {

    var returnList: List[EventData] = Nil
    var returnPaged: Page[EventData] = null

    (filterTag, filterCounty) match {
      case (Some(tw), Some(cnt)) =>
        if (pageNo.isDefined) {
          returnPaged = eventRepository.findPopularEventsWithCountyAndTagWord(cnt.objectId.toString, tw.objectId.toString, UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = eventRepository.findPopularEventsWithCountyAndTagWord(cnt.objectId.toString, tw.objectId.toString, UserLevelScala.HOST.Constant).asScala.toList
        }
      case (None, Some(cnt)) =>
        if (pageNo.isDefined) {
          returnPaged = eventRepository.findPopularEventsWithCounty(cnt.objectId.toString, UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = eventRepository.findPopularEventsWithCounty(cnt.objectId.toString, UserLevelScala.HOST.Constant).asScala.toList
        }
      case (Some(tw), None) =>
        if (pageNo.isDefined) {
          returnPaged = eventRepository.findPopularEventsWithTagWord(tw.objectId.toString, UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = eventRepository.findPopularEventsWithTagWord(tw.objectId.toString, UserLevelScala.HOST.Constant).asScala.toList
        }
      case (None, None) =>
        if (pageNo.isDefined) {
          returnPaged = eventRepository.findPopularEvents(UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = eventRepository.findPopularEvents(UserLevelScala.HOST.Constant).asScala.toList
        }
    }

    //var temp = eventRepository.getPopularEvents().asScala.toList
    //var temp2 = recipeRepository.findRecipes("00f0b94f-bf51-4888-97a5-0f1eee69fb08").asScala.toList

    if (returnList != Nil) {
      Left(Some(returnList))
    } else {
      Right(Option(returnPaged))
    }
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



  //region Saving & Deleting
  def deleteEventDateById(objectId: UUID, event: Event): Event = withTransaction(template){
    this.findEventDateById(objectId) match {
      case Some(ed) =>
        if(ed.getGuestsBooked == 0){
          Logger.debug("Removed EventDate: " + ed.getEventDateTime + " from " + event.getName)
          event.deleteEventDate(ed)
          eventDateRepository.delete(ed)
          event
        }else{
          Logger.debug("Cannot delete EventDate, there are already guests booked: " + ed.getGuestsBooked.toString + " (UUID:" + objectId.toString + ")")
          event
        }
      case None =>
        Logger.debug("Cannot delete EventDate, found no matching UUID:" + objectId.toString)
        event
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

  def save(newContent: Event): Event = withTransaction(template){
    eventRepository.save(newContent)
  }

  def save(newContent: EventDate): EventDate = withTransaction(template){
    eventDateRepository.save(newContent)
  }

  def save(newContent: BookedEventDate): BookedEventDate = withTransaction(template){
    bookedEventDateRepository.save(newContent)
  }
  //endregion

}
