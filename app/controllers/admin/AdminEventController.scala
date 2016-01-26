package controllers.admin

import javax.inject.{Named, Inject}

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc._

import models.{Event, UserCredential}
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import securesocial.core.SecureSocial.SecuredRequest
import services.{EventService, UserProfileService, ContentFileService}
import enums.{ContentStateEnums, RoleEnums}
import java.util.UUID
import customUtils.authorization.WithRole
import scala.Some
import customUtils.Helpers
import play.api.Logger
import models.event.EventDate
import org.joda.time.DateTime
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.EventForm

//@Named
class AdminEventController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment, val messagesApi: MessagesApi) extends Controller with securesocial.core.SecureSocial with I18nSupport{

  @Autowired
  private var eventService: EventService = _

  @Autowired
  private var userProfileService: UserProfileService = _

  @Autowired
  private var fileService: ContentFileService = _

  // Edit - Listing
  def listAll = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val listOfPage: List[Event] = eventService.getListOfAll
    Ok(views.html.admin.event.list(listOfPage))
  }

  // Edit - Add Content
  def contentForm = eventService.eventFormMapping

  def editIndex() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.event.index())
  }

  def add() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.event.add(contentForm))
  }

  def addSubmit() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN))(parse.multipartFormData) { implicit request =>

    var currentUser = request.user

    contentForm.bindFromRequest.fold(
      errors => {
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.add.error")
        BadRequest(views.html.admin.event.add(errors)).flashing(FlashMsgConstants.Error -> errorMessage)
      },
      contentData => {


        val newRec: Option[Event] = contentData.id match {
          case Some(id) =>
            eventService.findById(UUID.fromString(id)) match {
              case None => None
              case Some(item) =>
                item.setName(contentData.name)
                currentUser = item.getOwnerProfile.getOwner
                Some(item)
            }
          case None =>
            Some(new Event(contentData.name))
        }

        if(newRec.isEmpty){
            Logger.debug("Error saving: User used a non-existing objectId")
            val errorMessage = Messages("event.add.error")
            BadRequest(views.html.admin.event.add(contentForm.fill(contentData))).flashing(FlashMsgConstants.Error -> errorMessage)
        }

        newRec.get.setPreAmble(contentData.preAmble.getOrElse(""))
        newRec.get.setMainBody(contentData.mainBody.getOrElse(""))
        eventService.updateOrCreateEventDates(contentData, newRec.get)
        newRec.get.contentState = ContentStateEnums.PUBLISHED.toString

        val saved = eventService.add(newRec.get)
        val savedProfile = userProfileService.addEventToProfile(currentUser.getUserProfile, saved)
        val successMessage = Messages("admin.success") + " - " + Messages("admin.add.success", saved.getName, saved.objectId.toString)
        Redirect(controllers.admin.routes.AdminEventController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      }
    )

  }

  // Edit - Edit content
  def edit(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val editingItem = eventService.findById(objectId)
    editingItem match {
      case None =>
        Ok(views.html.admin.event.index())
      case Some(item) =>
        val form = EventForm.apply(
          Some(item.objectId.toString),
          item.getName,
          item.getPreAmble match{case null|"" => None case _ => Some(item.getPreAmble)},
          Some(item.getMainBody),
          mainImage = item.getMainImage match {
            case null => None
            case item => Some(item.objectId.toString)
          },
          images = eventService.convertToCommaSepStringOfObjectIds(eventService.getSortedEventImages(item)),
          eventDates = eventService.convertToEventFormDates(eventService.getSortedEventDates(item))
        )

        // Get any images and sort them
        val sortedImages = eventService.getSortedEventImages(item)

        Ok(views.html.admin.event.add(contentForm.fill(form),editingItem, sortedImages))
    }
  }

  // Edit - Delete content
  def delete(objectId: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val result: Boolean = eventService.deleteById(objectId)

    result match {
      case true =>
        val successMessage = Messages("admin.success") + " - " + Messages("admin.delete.success", objectId.toString)
        Redirect(controllers.admin.routes.AdminEventController.listAll()).flashing(FlashMsgConstants.Success -> successMessage)
      case false =>
        val errorMessage = Messages("admin.error") + " - " + Messages("admin.delete.error")
        Redirect(controllers.admin.routes.AdminEventController.listAll()).flashing(FlashMsgConstants.Error -> errorMessage)
    }

  }

}