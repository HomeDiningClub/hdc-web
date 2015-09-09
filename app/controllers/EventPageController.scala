package controllers

import models.files.ContentFile
import models.jsonmodels.{EventBoxJSON}
import org.springframework.stereotype.{Controller => SpringController}
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._
import securesocial.core.SecureSocial
import models.{UserCredential, Event}
import play.api.i18n.Messages
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import services.{EventService, UserProfileService, ContentFileService}
import enums.{ContentStateEnums, RoleEnums}
import java.util.UUID
import utils.authorization.{WithRoleAndOwnerOfObject, WithRole}
import scala.Some
import models.viewmodels.{MetaData, EditEventExtraValues, EventBox, EventForm}
import utils.Helpers
import play.api.Logger
import scala.collection.JavaConverters._

@SpringController
class EventPageController extends Controller with SecureSocial {

  @Autowired
  private var eventService: EventService = _

  @Autowired
  private var userProfileService: UserProfileService = _

  @Autowired
  private var fileService: ContentFileService = _


  def viewEventByNameAndProfile(profileName: String, eventName: String) = UserAwareAction { implicit request =>

    // Try getting the item from name, if failure show 404
    eventService.findByownerProfileProfileLinkNameAndEventLinkName(profileName,eventName) match {
      case Some(event) =>
             Ok(views.html.event.event(
               event,
               metaData = buildMetaData(event, request),
               eventBoxes = eventService.getEventBoxes(event.getOwnerProfile.getOwner),
               shareUrl = createShareUrl(event),
               isThisMyEvent = isThisMyEvent(event)))
          case None =>
            val errMess = "Cannot find event using name:" + eventName + " and profileName:" + profileName
            Logger.debug(errMess)
            BadRequest(errMess)
        }
  }


  def viewEventByNameAndProfilePageJSON(profileName: String, page: Int) = UserAwareAction { implicit request =>

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

  def viewEventByNameAndProfilePage(profileName: String, eventName: String, page: Int) = UserAwareAction { implicit request =>

    // Try getting from name, if failure show 404
    eventService.findByownerProfileProfileLinkNameAndEventLinkName(profileName,eventName) match {
      case Some(event) =>
        Ok(views.html.event.event(
          event,
          metaData = buildMetaData(event, request),
          eventBoxes = eventService.getEventBoxes(event.getOwnerProfile.getOwner),
          shareUrl = createShareUrl(event),
          isThisMyEvent = isThisMyEvent(event)))
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
            case item: String => utils.Helpers.limitLength(Helpers.removeHtmlTags(item), 125)
          }
        case item => {
          utils.Helpers.limitLength(item, 125)
        }
      },
      fbImage = event.getMainImage match {
        case image: ContentFile => { domain + routes.ImageController.profileNormal(image.getStoreId).url }
        case _ => { domain + "/images/event/event-default-main-image.jpg" }
      }
    ))
  }


  private def isThisMyEvent(item: Event)(implicit request: RequestHeader): Boolean = {
    utils.Helpers.getUserFromRequest match {
      case None =>
        false
      case Some(user) =>
        if(item.getOwnerProfile.getOwner.objectId == user.objectId)
          true
        else
          false
    }
  }


  // Edit - Add Content
  def evtForm = eventService.eventFormMapping
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

      EditEventExtraValues(
        mainImage match {
          case Some(item) => Some(List(routes.ImageController.imgChooserThumb(item).url))
          case None => None
        },
        recipeImages match {
          case Nil => None
          case items => Some(items.map{ item => routes.ImageController.imgChooserThumb(item).url})
        },
        item.get.getMaxNrOfMainImages,
        item.get.getMaxNrOfEventImages
      )
    }else{

      // Not brilliant, consider moving config
      var tempRec = new Event("temporary")
      val maxMainImage = tempRec.getMaxNrOfMainImages
      val maxImages = tempRec.getMaxNrOfEventImages
      tempRec = null

      EditEventExtraValues(None,None,maxMainImage,maxImages)
    }
  }


  def add() = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request =>
    Ok(views.html.event.addOrEdit(eventForm = evtForm, extraValues = setExtraValues(None)))
  }

  def edit(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request =>
    val editingItem = eventService.findById(objectId)

    editingItem match {
      case None =>
        val errorMsg = "Wrong ID, cannot edit, Page cannot be found."
        Logger.debug(errorMsg)
        NotFound(errorMsg)
      case Some(item) =>
        item.isEditableBy(Helpers.getUserFromRequest.get.objectId)
        val form = EventForm.apply(
          id = Some(item.objectId.toString),
          name = item.getName,
          preAmble = item.getPreAmble match{case null|"" => None case _ => Some(item.getPreAmble)},
          mainBody = Some(item.getMainBody),
          mainImage = item.getMainImage match {
            case null => None
            case item => Some(item.objectId.toString)
          },
          images = eventService.convertToCommaSepStringOfObjectIds(eventService.getSortedEventImages(item))
        )

        // Get any images and sort them
        //val sortedImages = recipeService.getSortedRecipeImages(item)

        Ok(views.html.event.addOrEdit(eventForm = evtForm.fill(form), editingEvent = editingItem, extraValues = setExtraValues(editingItem)))
    }
  }


  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    val currentUser: Option[UserCredential] = Helpers.getUserFromRequest

    evtForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("event.add.error")
        BadRequest(views.html.event.addOrEdit(errors,extraValues = setExtraValues(None))).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {

        val newRec: Option[Event] = contentData.id match {
          case Some(id) =>
            eventService.findById(UUID.fromString(id)) match {
              case None => None
              case Some(item) =>
                item.isEditableBy(currentUser.get.objectId).asInstanceOf[Boolean] match {
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
          BadRequest(views.html.event.addOrEdit(evtForm.fill(contentData), extraValues = setExtraValues(None))).flashing(FlashMsgConstants.Error -> errorMessage)
        }

        // Main image
        contentData.mainImage match {
          case Some(imageId) => UUID.fromString(imageId) match {
            case imageUUID: UUID =>
              fileService.getFileByObjectIdAndOwnerId(imageUUID, currentUser.get.objectId) match {
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
                  fileService.getFileByObjectIdAndOwnerId(imageUUID, currentUser.get.objectId) match {
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

        newRec.get.setMainBody(contentData.mainBody.getOrElse(""))
        newRec.get.setPreAmble(contentData.preAmble.getOrElse(""))
        newRec.get.contentState = ContentStateEnums.PUBLISHED.toString

        val savedItem = eventService.add(newRec.get)
        val savedProfile = userProfileService.addEventToProfile(currentUser.get.getUserProfile, savedItem)
        val successMessage = Messages("event.add.success", savedItem.getName)
        Redirect(controllers.routes.EventPageController.viewEventByNameAndProfile(currentUser.get.getUserProfile.profileLinkName,savedItem.getLink)).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }

  // Delete
  def delete(objectId: UUID) = SecuredAction(authorize = WithRoleAndOwnerOfObject(RoleEnums.USER,objectId)) { implicit request =>
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