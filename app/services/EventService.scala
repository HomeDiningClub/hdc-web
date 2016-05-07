package services

import java.time.LocalDateTime
import javax.inject.{Singleton, Named, Inject}
import models.files.ContentFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import traits.TransactionSupport
import scala.language.existentials
import repositories._
import models.{Event, UserProfile, UserCredential}
import scala.collection.JavaConverters._
import scala.List
import java.util.UUID
import models.viewmodels.EventBox
import controllers.routes
import customUtils.Helpers
import scala.collection.mutable.ListBuffer
import models.event.{EventDate, MealType}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import enums.SortOrderEnums
import enums.SortOrderEnums.SortOrderEnums
import org.joda.time.DateTime
import play.api.Logger
import models.formdata.{EventDateForm, EventForm}
import customUtils.formhelpers.Formats._

class EventService @Inject()(val template: Neo4jTemplate,
                             val eventRepository: EventRepository,
                             val eventDateRepository: EventDateRepository,
                             val mealTypeService: MealTypeService,
                             val messagesApi: MessagesApi) extends TransactionSupport with I18nSupport {

  implicit object LocalDateTimeOrdering extends Ordering[LocalDateTime] {
    def compare(d1: LocalDateTime, d2: LocalDateTime) = d1.compareTo(d2)
  }

  //@Transactional(readOnly = true)
  def findByownerProfileProfileLinkNameAndEventLinkName(profileLinkName: String, eventLinkName: String): Option[Event] = withTransaction(template) {
    eventRepository.findByownerProfileProfileLinkNameAndEventLinkName(profileLinkName, eventLinkName) match {
      case null => None
      case profile =>
        Some(profile)
    }
  }

  //@Transactional(readOnly = true)
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

  //@Transactional(readOnly = true)
  def findById(objectId: UUID): Option[Event] = withTransaction(template){
    eventRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  def findEventDateById(objectId: UUID): Option[EventDate] = withTransaction(template){
    eventDateRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }


  //@Transactional(readOnly = true)
  def getCountOfAll: Int = withTransaction(template) {
    eventRepository.getCountOfAll()
  }


  //@Transactional(readOnly = true)
  def getListOfAll: List[Event] = withTransaction(template){
    eventRepository.findAll.iterator.asScala.toList match {
      case null => null
      case items => items
    }
  }

  // Get sorted images
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

  def convertToEventFormDates(inputList: Option[List[EventDate]]): Option[List[EventDateForm]] = {
    inputList match {
      case None => None
      case Some(items) =>
        Some(items.map(x =>
          EventDateForm(Some(x.objectId.toString),
            java.time.LocalDate.of(x.getEventDateTime.getYear, x.getEventDateTime.getMonth, x.getEventDateTime.getDayOfMonth),
            java.time.LocalTime.of(x.getEventDateTime.getHour, x.getEventDateTime.getMinute)))
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
        "price" -> number(min = 0, max = 9999, strict = true),
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
        ))
      )(EventForm.apply)(EventForm.unapply)
    )
  }

  private def isValidTime(time: String): Boolean ={
    Helpers.isValidTime(time)
  }

  def updateOrCreateEventDates(contentData: EventForm, event: Event) {
    if(contentData.eventDates.nonEmpty){
      for(ed <- contentData.eventDates.get){
        val selectedDate = Helpers.buildDateFromDateAndTime(ed.date, ed.time)

        // Edit old date on existing event
        if(ed.id.nonEmpty && ed.guestsBooked == 0){
          this.findEventDateById(UUID.fromString(ed.id.get)) match {
            case Some(eventDate) => this.updateOldEventDate(ed, eventDate)
            case None => Logger.debug("Cannot find earlier EventDate using UUID to update date on")
          }
          // Add new date on existing event
        }else if(ed.id.isEmpty && contentData.id.nonEmpty){
          this.findById(UUID.fromString(contentData.id.get)) match {
            case Some(event) => this.addEventDate(ed,event)
            case None => Logger.debug("Cannot find earlier Event using UUID, cannot add EventDate")
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
    // TODO: Add guest reminder email
    storedDate.setEventDateTime(Helpers.buildDateFromDateAndTime(formDate.date, formDate.time))
  }

  def addEventDate(formDate: EventDateForm, event: Event){
    val newEventDate = this.add(new EventDate(Helpers.buildDateFromDateAndTime(formDate.date, formDate.time)))
    event.addEventDate(newEventDate)
  }


  //@Transactional(readOnly = true)
  def getMealTypes(): Option[List[MealType]] = withTransaction(template){
    mealTypeService.listAll()
  }

  //@Transactional(readOnly = true)
  def getEventBoxes(user: UserCredential): Option[List[EventBox]] = withTransaction(template){
    // Without paging
    this.getEventBoxesPage(user, 0)
  }

  //@Transactional(readOnly = true)
  def getEventBoxesPage(user: UserCredential, pageNo: Integer): Option[List[EventBox]] = withTransaction(template){

    // With paging
    // 0 current page, 6 number of items for each page

    val list = eventRepository.findEventsOnPage(user.objectId.toString, new PageRequest(pageNo, 6))
    val iterator = list.iterator()
    var eventList : ListBuffer[EventBox] = new ListBuffer[EventBox]

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




  //@Transactional(readOnly = true)
  def getListOwnedBy(user: UserCredential): Option[List[Event]] = withTransaction(template){
    eventRepository.findByownerProfileOwner(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  //@Transactional(readOnly = true)
  def getListOwnedBy(userProfile: UserProfile): Option[List[Event]] = withTransaction(template){
    eventRepository.findByownerProfile(userProfile).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  //@Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = withTransaction(template){
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        item.deleteMainImage()
        item.deleteEventImages()
        //item.deleteRatings()
        item.deleteLikes()
        eventRepository.delete(item)
        true
    }
  }

  // Fetching
  //@Transactional(readOnly = true)
  def fetchEvent(event: Event): Event = withTransaction(template){
    template.fetch(event)
  }


  //@Transactional(readOnly = false)
  private def deleteAll() = withTransaction(template){
    eventRepository.deleteAll()
  }

  //@Transactional(readOnly = false)
  def add(newContent: Event): Event = withTransaction(template){
    eventRepository.save(newContent)
  }

  def add(newContent: EventDate): EventDate = withTransaction(template){
    eventDateRepository.save(newContent)
  }


}
