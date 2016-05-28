package controllers

import java.time.{LocalDateTime, LocalDate}
import javax.inject.{Named, Inject}

import models.event.{BookedEventDate, MealType, AlcoholServing}
import models.files.ContentFile
import models.jsonmodels.{EventBoxJSON}
import org.springframework.stereotype.{Controller => SpringController}
import play.api.data.Form
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._
import models.{UserCredential, Event}
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import play.twirl.api.Html
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.{SecuredRequest, RequestWithUser}
import services._
import enums.{ContentStateEnums, RoleEnums}
import java.util.UUID
import customUtils.authorization.{WithRoleAndOwnerOfObject, WithRole}

import scala.Some
import models.viewmodels._
import customUtils.Helpers
import play.api.Logger
import scala.collection.JavaConverters._
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.{EventDateSuggestionForm, EventOptionsForm, EventBookingForm, EventForm}

class EventPageController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                     val likeController: LikeController,
                                     val eventService: EventService,
                                     val mealTypeService: MealTypeService,
                                     val alcoholServingService: AlcoholServingService,
                                     val userProfileService: UserProfileService,
                                     val fileService: ContentFileService,
                                     implicit val nodeEntityService: NodeEntityService,
                                     val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {


  def viewEventByNameAndProfile(profileName: String, eventName: String) = UserAwareAction() { implicit request =>

    // Try getting the item from name, if failure show 404
    eventService.findByownerProfileProfileLinkNameAndEventLinkName(profileName,eventName) match {
      case Some(event) =>
          Ok(views.html.event.event(
            event = event,
            eventDates = event.getEventDates.asScala.toList,
            eventBookingForm = createEventBookingForm(event),
            eventDateSuggestionForm = createDateSuggestionForm(event),
            eventPropertyList = createEventPropertyList(event, request.user),
            metaData = buildMetaData(event, request),
            eventBoxes = eventService.getEventBoxes(event.getOwnerProfile.getOwner),
            shareUrl = createShareUrl(event),
            isThisMyEvent = isThisMyEvent(event),
            memberUser = request.user,
            eventLikeForm = getEventLikeForm(event)))
          case None =>
            val errMess = "Cannot find event using name:" + eventName + " and profileName:" + profileName
            Logger.debug(errMess)
            BadRequest(errMess)
        }
  }


  def getEventLikeForm(event: Event)(implicit request: RequestWithUser[AnyContent,UserCredential]): Html = {
    val likeForm = likeController.renderEventLikeForm(event, request.user)
    likeForm
  }

  def getEventPriceJSON(eventUUID: UUID, nrOfGuests: Int) = UserAwareAction() {
    Ok(Json.toJson(eventService.getEventPrice(eventUUID, nrOfGuests)))
  }

  def getEventTimesForDateAJAX(eventUUID: UUID, date: LocalDate) = UserAwareAction() {
    Ok(views.html.event.bookingTimeList(eventService.getEventTimesForDate(eventUUID, date)))
  }

  def getAllAvailableDatesJSON(eventUUID: UUID) = UserAwareAction() {
    Ok(Json.toJson(eventService.getAvailableDates(eventUUID)))
  }

  def viewEventByNameAndProfilePageJSON(profileName: String, page: Int) = UserAwareAction() { implicit request =>

    val listOfEvents: Option[List[EventBox]] = userProfileService.findByprofileLinkName(profileName) match {
      case Some(profile) => {
        eventService.getEventBoxesPage(profile.getOwner, page)
      }
      case None => { None }
    }

    if(listOfEvents.isEmpty){
      Logger.debug("Cannot find any events using profileName:" + profileName)
      Ok("")
    }

    val list: List[EventBoxJSON] = listOfEvents match {
      case None => Nil
      case Some(items) => items.map {
        e: EventBox =>
          EventBoxJSON(
            e.objectId.toString,
            e.linkToEvent,
            e.name,
            e.preAmble.getOrElse(""),
            e.mainImage.getOrElse(""),
            //e.eventRating.toString,
            e.eventBoxCount,
            e.hasNext,
            e.hasPrevious,
            e.totalPages
          )
      }
    }

    Ok(convertToJson(list))
  }

  def convertToJson(jsonCase: Seq[EventBoxJSON]): JsValue = Json.toJson(jsonCase)

  def viewEventByNameAndProfilePage(profileName: String, eventName: String, page: Int) = UserAwareAction() { implicit request =>

    // Try getting from name, if failure show 404
    eventService.findByownerProfileProfileLinkNameAndEventLinkName(profileName,eventName) match {
      case Some(event) =>
        Ok(views.html.event.event(
          event = event,
          eventDates = event.getEventDates.asScala.toList,
          eventBookingForm = createEventBookingForm(event),
          eventDateSuggestionForm = createDateSuggestionForm(event),
          eventPropertyList = createEventPropertyList(event, request.user),
          metaData = buildMetaData(event, request),
          eventBoxes = eventService.getEventBoxes(event.getOwnerProfile.getOwner),
          shareUrl = createShareUrl(event),
          isThisMyEvent = isThisMyEvent(event),
          memberUser = request.user,
          eventLikeForm = getEventLikeForm(event)
        ))
      case None =>
        val errMess = "Cannot find event using name:" + eventName + " and profileName:" + profileName
        Logger.debug(errMess)
        BadRequest(errMess)
    }
  }


  def viewEventByName(eventName: String) = UserAwareAction { implicit request =>

    // Try getting the recipe from name, if failure show 404
    eventService.findByeventLinkName(eventName) match {
      case Some(item) =>
        Redirect(controllers.routes.EventPageController.viewEventByNameAndProfile(item.getOwnerProfile.profileLinkName,item.getLink))
      case None =>
        val errMess = "Cannot find event using name:" + eventName
        Logger.debug(errMess)
        BadRequest(errMess)
    }
  }

  private def createShareUrl(event: Event): String = {
    controllers.routes.EventPageController.viewEventByNameAndProfile(event.getOwnerProfile.profileLinkName,event.getLink).url + "?ts=" + Helpers.getDateForSharing(event)
  }

  private def buildMetaData(event: Event, request: RequestHeader): Option[MetaData] = {
    val domain = "//" + request.domain

    Some(MetaData(
      fbUrl = domain + request.path,
      fbTitle = event.getName,
      fbDesc = event.getPreAmble match {
        case null | "" =>
          event.getMainBody match {
            case null | "" => ""
            case item: String => customUtils.Helpers.limitLength(Helpers.removeHtmlTags(item), 125)
          }
        case item => {
          customUtils.Helpers.limitLength(item, 125)
        }
      },
      fbImage = event.getMainImage match {
        case image: ContentFile => { domain + routes.ImageController.profileNormal(image.getStoreId).url }
        case _ => { domain + "/images/event/event-default-main-image.jpg" }
      }
    ))
  }


  private def isThisMyEvent(item: Event)(implicit request: RequestWithUser[AnyContent,UserCredential]): Boolean = {
    request.user match {
      case None =>
        false
      case Some(user) =>
        if(item.getOwnerProfile.getOwner.objectId == user.objectId)
          true
        else
          false
    }
  }

  // Forms
  def evtForm = eventService.eventFormMapping
  def evtBookingForm = eventService.eventBookingFormMapping
  def evtDateSuggestionForm = eventService.eventDateSuggestionFormMapping


  // Booking
  private def createEventBookingForm(event: Event): Form[EventBookingForm] = {
    val formDefaults = EventBookingForm(
      eventId = event.objectId,
      eventDateId = None,
      date = None,
      guests = 2,
      comment = None
    )
    evtBookingForm.fill(formDefaults).discardingErrors
  }

  // Suggestion
  private def createDateSuggestionForm(event: Event): Form[EventDateSuggestionForm] = {
    evtDateSuggestionForm
  }

  private def createEventPropertyList(event: Event, currentUser: Option[UserCredential] = None): EventPropertyList = {
    val op = event.getOwnerProfile
    val locCounty: Option[String] = op.getLocations.asScala.toList match {
      case null | Nil => None
      case items => Some(items.head.county.name)
    }
    var locStreetAddress: Option[String] = None
    var locCity: Option[String] = None
    var locZipCode: Option[String] = None

    if(currentUser.isDefined){
      if(eventService.isGuestBookedAtEvent(currentUser.get, event)){
        locStreetAddress = Some(op.streetAddress)
        locZipCode = Some(op.zipCode)
        locCity = Some(op.city)
      }
    }

    EventPropertyList(
      locationAddress = locStreetAddress,
      locationCounty = locCounty,
      locationZipCode = locZipCode,
      locationCity = locCity,
      childFriendly = event.getChildFriendly,
      handicapFriendly = event.getHandicapFriendly,
      havePets = event.getHavePets,
      smokingAllowed = event.getSmokingAllowed,
      minNrOfGuests = event.getMinNrOfGuests,
      maxNrOfGuests = event.getMaxNrOfGuests,
      alcoholServing = event.getAlcoholServing match {
        case null => None
        case as => Some(as.name)
      },
      mealType = event.getMealType match {
        case null => None
        case mt => Some(mt.name)
      }
    )
  }

  // Edit - Add Content
  private def setExtraValues(item: Option[Event] = None): EditEventExtraValues = {

    if(item.isDefined){
      // Other values that does not fit to be in form-classes
      val mainImage = item.get.getMainImage match {
        case null => None
        case image => Some(image.getStoreId)
      }
      val recipeImages = item.get.getEventImages.asScala.toList match {
        case null | Nil => Nil
        case images => images.map { image =>
          image.getStoreId
        }
      }

      var bookedGuestsCount = 0
      val listOfBookedGuests = item.get.getEventDates.asScala
      if(listOfBookedGuests.nonEmpty){
        bookedGuestsCount = listOfBookedGuests.map(ed => ed.getGuestsBooked).sum
      }

      EditEventExtraValues(
        mainImage match {
          case Some(mItem) => Some(List(routes.ImageController.imgChooserThumb(mItem).url))
          case None => None
        },
        recipeImages match {
          case Nil => None
          case items => Some(items.map{ item => routes.ImageController.imgChooserThumb(item).url})
        },
        item.get.getMaxNrOfMainImages,
        item.get.getMaxNrOfEventImages,
        bookedGuestsCount
      )
    }else{

      // Not brilliant, consider moving config
      var tempRec = new Event("temporary")
      val maxMainImage = tempRec.getMaxNrOfMainImages
      val maxImages = tempRec.getMaxNrOfEventImages
      tempRec = null

      EditEventExtraValues(None,None,maxMainImage,maxImages,0)
    }
  }




  def add() = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>

    val mealSeq = mealTypeService.getMealTypesAsSeq
    val mealDefault =  mealSeq match {
      case None => None
      case Some(x) => Some(UUID.fromString(x.head._1)) // 1 = Object id, 2 = Name
    }

    var alcoSeq = alcoholServingService.getAlcoholServingsAsSeq
    val alcoDefault =  alcoSeq match {
      case None => None
      case Some(x) => Some(UUID.fromString(x.head._1)) // 1 = Object id, 2 = Name
    }

    val defaultForm = EventForm(
      name = "",
      preAmble = None,
      id = None,
      mainBody = None,
      mainImage = None,
      price = 0,
      images = None,
      eventDates = None,
      maxNoOfGuest = 6,
      minNoOfGuest = 2,
      eventOptionsForm = EventOptionsForm(
        childFriendly = true,
        handicapFriendly = true,
        havePets = false,
        smokingAllowed = false,
        alcoholServing = alcoDefault,
        mealType = mealDefault
      )
    )

    Ok(views.html.event.addOrEdit(
      eventForm = evtForm.fill(defaultForm).discardingErrors,
      extraValues = setExtraValues(None),
      optionsAlcoholServings = alcoSeq,
      optionsMealTypes = mealSeq))
  }

  def edit(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val editingItem = eventService.findById(objectId)

    eventService.findById(objectId) match {
      case None =>
        val errorMsg = "Wrong ID, cannot edit, Page cannot be found."
        Logger.debug(errorMsg)
        NotFound(errorMsg)
      case Some(item) =>
        item.isEditableBy(request.user.objectId).asInstanceOf[Boolean] match {
          case true =>
            val form = EventForm.apply(
              id = Some(item.objectId.toString),
              name = item.getName,
              preAmble = item.getPreAmble match{case null|"" => None case _ => Some(item.getPreAmble)},
              mainBody = Some(item.getMainBody),
              mainImage = item.getMainImage match {
                case null => None
                case mainImg => Some(mainImg.objectId.toString)
              },
              price = item.getPrice.intValue(),
              images = eventService.convertToCommaSepStringOfObjectIds(eventService.getSortedEventImages(item)),
              eventDates = eventService.convertToEventFormDates(eventService.filterEventDatesValidForEditing(eventService.getSortedEventDates(item))),
              minNoOfGuest = item.getMinNrOfGuests,
              maxNoOfGuest = item.getMaxNrOfGuests,
              eventOptionsForm = EventOptionsForm(
                childFriendly = item.getChildFriendly,
                handicapFriendly = item.getHandicapFriendly,
                havePets = item.getHavePets,
                smokingAllowed = item.getSmokingAllowed,
                alcoholServing = item.getAlcoholServing match {
                  case null => None
                  case as => Some(as.objectId)
                },
                mealType = item.getMealType match {
                  case null => None
                  case mt => Some(mt.objectId)
                }
              )
            )

            Ok(views.html.event.addOrEdit(
              eventForm = evtForm.fill(form),
              editingEvent = editingItem,
              extraValues = setExtraValues(editingItem),
              optionsAlcoholServings = alcoholServingService.getAlcoholServingsAsSeq,
              optionsMealTypes = mealTypeService.getMealTypesAsSeq,
              activateMultipleStepsForm = false))

          case false => {
            val errorMsg = "Cannot edit, not owner of event"
            Logger.debug(errorMsg)
            NotFound(errorMsg)
          }
        }
    }
  }


  def addDateSuggestionSubmit() = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>
    val currentUser = request.user

    evtDateSuggestionForm.bindFromRequest.fold(
      errors => {
        Logger.debug("Cannot return to event to show error, no valid eventUUID")
        NotFound(Messages("event.suggest.add.error"))
      },
      contentData => {
        eventService.findById(contentData.eventId) match {
          case Some(event) => {
            // TODO: Add suggestion

            val successValues = EventDateSuggestionSuccess(
              date = contentData.date,
              time = contentData.time,
              nrOfGuests = contentData.guests,
              comment = contentData.comment
            )

            val successMessage = Messages("event.suggest.add.success")
            Ok(views.html.event.suggestionSuccess(event,successValues)).flashing(FlashMsgConstants.Success -> successMessage)
          }
          case _ => {
            val errorMsg = "Cannot suggest date to event, no valid eventUUID"
            Logger.debug(errorMsg)
            NotFound(errorMsg)
          }
        }
      }
    )
  }

  def addBookingSubmit() = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    val currentUser = request.user

    evtBookingForm.bindFromRequest.fold(
      errors => {
          Logger.debug("Cannot return to event to show error, no valid eventUUID")
          NotFound(Messages("event.book.add.error"))
      },
      contentData => {
        eventService.findById(contentData.eventId) match {
          case Some(event) => {

            // If user selected a pre-chosen date
            if(contentData.eventDateId.nonEmpty){
              // Verify that the date actually exists
              eventService.findEventDateById(contentData.eventDateId.get)  match {
                case Some(eventDate) => {

                  if(!eventService.isUserBookedToEventDate(eventDate, currentUser)) {

                    // Lastly check space and if user hasn't booked before
                    if (eventService.doesEventDateHasSpaceFor(event, eventDate, contentData.guests)) {

                      // TODO: Add booking-number
                      val newBooking = eventService.addBooking(currentUser,eventDate,contentData.guests)

                      val successValues = EventBookingSuccess(
                        bookingNumber = newBooking.objectId.hashCode().toLong,
                        date = eventDate.getEventDateTime.toLocalDate,
                        time = eventDate.getEventDateTime.toLocalTime,
                        locationAddress = event.getOwnerProfile.streetAddress,
                        locationCity = event.getOwnerProfile.city,
                        locationCounty = event.getOwnerProfile.getLocations.asScala.head.county.name,
                        locationZipCode = event.getOwnerProfile.zipCode,
                        phoneNumberToHost = event.getOwnerProfile.phoneNumber match {
                          case "" => None
                          case p => Some(p)
                        },
                        nrOfGuests = contentData.guests,
                        totalCost = eventService.getEventPrice(event, contentData.guests),
                        email = currentUser.emailAddress
                      )

                      Logger.debug("Successful booking performed eventDateId: " + eventDate.objectId)
                      Ok(views.html.event.bookingSuccess(event, successValues))
                    }else{
                      val errorMsg = Messages("event.book.add.too-many-bookings")
                      Redirect(controllers.routes.EventPageController.viewEventByNameAndProfile(currentUser.getUserProfile.profileLinkName,event.getLink)).flashing(FlashMsgConstants.Error -> errorMsg)
                    }

                  }else{
                    val errorMsg = Messages("event.book.add.already-booked")
                    Redirect(controllers.routes.EventPageController.viewEventByNameAndProfile(currentUser.getUserProfile.profileLinkName,event.getLink)).flashing(FlashMsgConstants.Error -> errorMsg)
                  }


                }
                case None => {
                  val errorMsg = "Cannot add booking to event, no valid eventUUID"
                  Logger.debug(errorMsg)
                  NotFound(errorMsg)
                }
              }
            }else{
              val errorMsg = "Cannot add booking nor suggest a booking, not correct values posted"
              Logger.debug(errorMsg)
              NotFound(errorMsg)
            }

          }
          case _ => {
            val errorMsg = "Cannot add booking to event, no valid eventUUID"
            Logger.debug(errorMsg)
            NotFound(errorMsg)
          }

        }
      }

    )

  }


  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    val currentUser = request.user

    evtForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("event.add.error")
        BadRequest(views.html.event.addOrEdit(
          eventForm = errors,
          extraValues = setExtraValues(None),
          optionsAlcoholServings = alcoholServingService.getAlcoholServingsAsSeq,
          optionsMealTypes = mealTypeService.getMealTypesAsSeq)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val newRec: Option[Event] = contentData.id match {
          case Some(id) =>
            eventService.findById(UUID.fromString(id)) match {
              case None => None
              case Some(item) =>
                item.isEditableBy(currentUser.objectId).asInstanceOf[Boolean] match {
                  case true =>
                    item.setName(contentData.name)
                    Some(item)
                  case false =>
                    None
                }
            }
          case None =>
            Some(new Event(contentData.name))
        }

        if (newRec.isEmpty) {
          Logger.debug("Error saving Event: User used a non-existing, or someone else's Event")
          val errorMessage = Messages("event.add.error")
          BadRequest(views.html.event.addOrEdit(
            eventForm = evtForm.fill(contentData),
            extraValues = setExtraValues(None),
            optionsAlcoholServings = alcoholServingService.getAlcoholServingsAsSeq,
            optionsMealTypes = mealTypeService.getMealTypesAsSeq)).flashing(FlashMsgConstants.Error -> errorMessage)
        }

        // Main image
        contentData.mainImage match {
          case Some(imageId) => UUID.fromString(imageId) match {
            case imageUUID: UUID =>
              fileService.getFileByObjectIdAndOwnerId(imageUUID, currentUser.objectId) match {
                case Some(item) => newRec.get.setAndRemoveMainImage(item)
                case _  => None
              }
          }
          case None =>
            newRec.get.deleteMainImage()
            None
        }

        // Images list
        var hasDeletedImages = false
        contentData.images match {
          case None =>
            newRec.get.deleteEventImages()
            hasDeletedImages = true
          case Some(imageStr) =>
            // This is just a comma sep list of object id's, split, validate UUID, and verify each entry
            imageStr.split(",").take(newRec.get.getMaxNrOfEventImages).foreach { imageId =>
              UUID.fromString(imageId) match {
                case imageUUID: UUID =>
                  fileService.getFileByObjectIdAndOwnerId(imageUUID, currentUser.objectId) match {
                    case Some(item) =>
                      // Found at least one valid image, clean the current list, but only one time
                      if (!hasDeletedImages) {
                        newRec.get.deleteEventImages()
                        hasDeletedImages = true
                      }
                      // Add it
                      newRec.get.addEventImage(item)
                    case _ =>
                      None
                  }
              }
            }
        }

        contentData.eventOptionsForm.alcoholServing match {
          case None => Logger.debug("Error adding AlcoholServing to event, no uuid")
          case Some(uuid) => alcoholServingService.findById(uuid) match {
            case None => Logger.debug("Error adding AlcoholServing to event, the uuid didn't match any alcoholservings-entry:" + uuid)
            case Some(as) => newRec.get.setAlcoholServing(as)
          }
        }

        contentData.eventOptionsForm.mealType match {
          case None => Logger.debug("Error adding Mealtype to event, no uuid")
          case Some(uuid) => mealTypeService.findById(uuid) match {
            case None => Logger.debug("Error adding Mealtype to event, the uuid didn't match any mealtype-entry:" + uuid)
            case Some(mt) => newRec.get.setMealType(mt)
          }
        }

        newRec.get.setChildFriendly(contentData.eventOptionsForm.childFriendly)
        newRec.get.setHandicapFriendly(contentData.eventOptionsForm.handicapFriendly)
        newRec.get.setHavePets(contentData.eventOptionsForm.havePets)
        newRec.get.setSmokingAllowed(contentData.eventOptionsForm.smokingAllowed)
        newRec.get.setMaxNrOfGuests(contentData.maxNoOfGuest)
        newRec.get.setMinNrOfGuests(contentData.minNoOfGuest)
        newRec.get.setMainBody(contentData.mainBody.getOrElse(""))
        newRec.get.setPreAmble(contentData.preAmble.getOrElse(""))
        newRec.get.setPrice(contentData.price.toLong)
        eventService.updateOrCreateEventDates(contentData, newRec.get)
        newRec.get.contentState = ContentStateEnums.PUBLISHED.toString

        val savedItem = eventService.save(newRec.get)
        val savedProfile = userProfileService.addEventToProfile(currentUser.getUserProfile, savedItem)
        val successMessage = Messages("event.add.success", savedItem.getName)
        Redirect(controllers.routes.EventPageController.viewEventByNameAndProfile(currentUser.getUserProfile.profileLinkName,savedItem.getLink)).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }

  // Delete
  def delete(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val item: Option[Event] = eventService.findById(objectId)

    if(item.isEmpty){
        val errorMessage = Messages("event.delete.error")
        Redirect(controllers.routes.UserProfileController.viewProfileByLoggedInUser()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

    val eventLinkName = item.get.getLink
    val ownerProfileName =  item.get.getOwnerProfile.profileLinkName
    val result: Boolean = eventService.deleteById(item.get.objectId)

    result match {
      case true =>
        val successMessage = Messages("event.delete.success")
        Redirect(controllers.routes.UserProfileController.viewProfileByLoggedInUser()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("event.delete.error")
        Redirect(controllers.routes.EventPageController.viewEventByNameAndProfile(ownerProfileName,eventLinkName)).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}