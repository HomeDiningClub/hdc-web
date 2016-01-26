package services

import javax.inject.{Singleton, Named, Inject}
import models.files.ContentFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

//@Named
@Service
class EventService @Inject()(val template: Neo4jTemplate, val eventRepository: EventRepository, val mealTypeService: MealTypeService) {

  /*
  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var eventRepository: EventRepository = _
*/

  @Transactional(readOnly = true)
  def findByownerProfileProfileLinkNameAndEventLinkName(profileLinkName: String, eventLinkName: String): Option[Event] = {
    eventRepository.findByownerProfileProfileLinkNameAndEventLinkName(profileLinkName, eventLinkName) match {
      case null => None
      case profile =>
        Some(profile)
    }
  }

  @Transactional(readOnly = true)
  def findByeventLinkName(eventLinkName: String): Option[Event] = {

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

  @Transactional(readOnly = true)
  def findById(objectId: UUID): Option[Event] = {
    eventRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  @Transactional(readOnly = true)
  def getCountOfAll: Int = {
    eventRepository.getCountOfAll()
  }


  @Transactional(readOnly = true)
  def getListOfAll: List[Event] = {
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
        Some(items.map(x => EventDateForm(Some(x.objectId.toString), x.getEventDateTime, 0)))
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
        "mainimage" -> optional(text),
        "images" -> optional(text),
        "eventDates" -> optional(list(
          mapping(
            "id" -> optional(text),
            "date" -> date,
            "guestsbooked" -> number
          )(EventDateForm.apply)(EventDateForm.unapply)
        ))
      )(EventForm.apply)(EventForm.unapply)
    )
  }


  def updateOrCreateEventDates(contentData: EventForm, event: Event) {
    if (contentData.eventDates.nonEmpty) {
      val currentEventDates = this.getSortedEventDates(event)

      contentData.eventDates.get.foreach { formDate =>

        // Found id, verify it
        if(formDate.id.nonEmpty){

          if(currentEventDates.nonEmpty){
            val formDateId = UUID.fromString(formDate.id.get)

            currentEventDates.get.foreach { storedDate =>
              storedDate.objectId.equals(formDateId) match {
                case true => {
                  updateOldEventDate(formDate, storedDate)
                }
                case false => {
                  Logger.debug("Error updating event date: FormEventDate has a non-existing objectId")
                }
              }
            } // end foreach
          }

        } else {
          addEventDate(formDate, event)
        }

      } // end foreach
    }
  }

  def updateOldEventDate(formDate: EventDateForm, storedDate: EventDate) {
    // TODO: Add guest reminder email
    storedDate.setEventDateTime(formDate.date)
  }

  def addEventDate(formDate: EventDateForm, event: Event){
    event.addEventDate(new EventDate(formDate.date))
  }


  @Transactional(readOnly = true)
  def getMealTypes(): Option[List[MealType]] = {
    mealTypeService.listAll()
  }

  @Transactional(readOnly = true)
  def getEventBoxes(user: UserCredential): Option[List[EventBox]] = {
    // Without paging
    this.getEventBoxesPage(user, 0)
  }

  @Transactional(readOnly = true)
  def getEventBoxesPage(user: UserCredential, pageNo: Integer): Option[List[EventBox]] = {

    // With paging
    // 0 current page, 6 number of items for each page

    val list = eventRepository.findEventsOnPage(user.objectId.toString, new PageRequest(pageNo, 6))
    val iterator = list.iterator()
    var eventList : ListBuffer[EventBox] = new ListBuffer[EventBox]

    while(iterator.hasNext()) {

      val obj = iterator.next()

      // Rating
//      val v = obj.getRating() match {
//        case null => "0.0"
//        case _ => obj.getRating()
//      }

      // Convert string to double, round to Int and convert to Int
//      val ratingValue : Int = v.toDouble.round.toInt

      // Link
      val linkToEvent = (obj.getprofileLinkName, obj.getLinkName) match {
        case (null|null) | ("","") => "#"
        case (profLink,evtLink) => routes.EventPageController.viewEventByNameAndProfile(profLink, evtLink).url
      }

      // Image
      var mainImage = Some("/assets/images/event/event-box-default.png")
      if(obj.getMainImage().iterator().hasNext()){
        mainImage = Some(routes.ImageController.eventBox(obj.getMainImage().iterator().next()).url)
      }

      // Build return-list
      var event = EventBox(
        Some(UUID.fromString(obj.getobjectId)),
        linkToEvent,
        obj.getName,
        obj.getpreAmble match {
        case "" | null =>
          var retBody = Helpers.removeHtmlTags(obj.getMainBody)

          if (retBody.length > 125)
            retBody = retBody.substring(0, 125) + "..."

          Some(retBody)
        case content => Some(content)
        },
        mainImage,
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




  @Transactional(readOnly = true)
  def getListOwnedBy(user: UserCredential): Option[List[Event]] = {
    eventRepository.findByownerProfileOwner(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  @Transactional(readOnly = true)
  def getListOwnedBy(userProfile: UserProfile): Option[List[Event]] = {
    eventRepository.findByownerProfile(userProfile).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  @Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = {
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
  @Transactional(readOnly = true)
  def fetchEvent(event: Event): Event = {
    template.fetch(event)
  }


  @Transactional(readOnly = false)
  private def deleteAll() {
    eventRepository.deleteAll()
  }

  @Transactional(readOnly = false)
  def add(newContent: Event): Event = {
    eventRepository.save(newContent)
  }


}
