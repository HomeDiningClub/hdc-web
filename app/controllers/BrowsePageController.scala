package controllers

import java.util.UUID
import javax.inject.{Inject, Named}

import customUtils.Helpers
import models.event.EventData
import models.UserProfile
import models.location.County
import models.profile.TagWord
import models.viewmodels.{BrowseEventBox, BrowseProfileBox}
import org.springframework.data.domain.Page
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Controller
import securesocial.core.SecureSocial
import services._

import scala.collection.JavaConverters._
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.SearchFilterForm
import play.api.Environment

class BrowsePageController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                      val userProfileService: UserProfileService,
                                      val eventService: EventService,
                                      val tagWordService : TagWordService,
                                      val countyService: CountyService,
                                      val ratingService: RatingService,
                                      val messagesApi: MessagesApi,
                                      val environment: Environment) extends Controller with SecureSocial with I18nSupport {

  // Search form
  val searchForm = Form(
    mapping(
      //"freeText" -> optional(text),
      "fCounty" -> optional(text),
      "fTag" -> optional(text),
      "fHost" -> optional(boolean)
    )(SearchFilterForm.apply)(SearchFilterForm.unapply)
  )

  def browseEvents(fTag: String, fCounty: String) = UserAwareAction { implicit request =>

    val form = SearchFilterForm.apply(
      fCounty match { case null | "" => None case item => Some(item)},
      fTag match { case null | "" => None case item => Some(item)},
      None
    )

    Ok(views.html.browse.browseEvents(
      searchForm = searchForm.fill(form),
      optionsFoodAreas = tagWordService.getFoodAreas,
      optionsLocationAreas = countyService.getCounties
      //browseBoxes = browseBoxes
    ))
  }

  def browseProfiles(fTag: String, fCounty: String, fHost: Boolean) = UserAwareAction { implicit request =>

    //val browseBoxes = getBrowseBoxes(fTag, fCounty, fHost)
    val isHost = fHost

    val form = SearchFilterForm.apply(
      fCounty match { case null | "" => None case item => Some(item)},
      fTag match { case null | "" => None case item => Some(item)},
      isHost match { case false => None case item => Some(item)}
    )

    Ok(views.html.browse.browseProfiles(
      searchForm = searchForm.fill(form),
      optionsFoodAreas = tagWordService.getFoodAreas,
      optionsLocationAreas = countyService.getCounties,
      optionsIsHost = if(isHost) Some(true) else Some(false)
      //browseBoxes = browseBoxes
    ))
  }

  def getBrowseEventBoxesPagedJSON(boxFilterTag: String, boxFilterCounty: String, page: Int = 0) = UserAwareAction { implicit request =>

    // Get items
    val listOfBoxes: Option[Page[EventData]] = getBrowseEventBoxesPaged(boxFilterTag, boxFilterCounty, page)

    listOfBoxes match {
      case Some(list) =>
        if (list.hasContent) {
          // First fetch the list of items
          var returnString: String = views.html.browse.browseEventBox.render(boxes = buildEventBoxes(list.asScala.toList), messages = request2Messages).toString
          // Attach pagination if more then one page
          if (list.getTotalPages > 1) {
            returnString += views.html.shared.pagination.render(jsMethodName = "getBoxesAsJSON", hasNext = list.hasNext, hasPrev = list.hasPrevious, currentPage = page, totalCount = list.getTotalElements, totalPages = list.getTotalPages, messages = request2Messages).toString
          }
          Ok(returnString)
        }else{
          Ok(views.html.browse.browseNoHit.render(Messages("browse.boxes.not-found", routes.BrowsePageController.browseEvents().url)))
        }
      case None =>
        Ok(views.html.browse.browseNoHit.render(Messages("browse.boxes.not-found", routes.BrowsePageController.browseEvents().url)))
    }
  }


  def getBrowseProfileBoxesPagedJSON(boxFilterTag: String, boxFilterCounty: String, boxFilterIsHost: Boolean, page: Int = 0) = UserAwareAction { implicit request =>

    // Get items
    val isHost = boxFilterIsHost
    val listOfBoxes: Option[Page[UserProfile]] = getBrowseProfileBoxesPaged(boxFilterTag, boxFilterCounty, isHost, page)

    listOfBoxes match {
      case Some(list) =>
        if (list.hasContent) {
          // First fetch the list of items
          var returnString: String = views.html.browse.browseProfileBox.render(boxes = buildProfileBoxes(list.asScala.toList), messages = request2Messages).toString
          // Attach pagination if more then one page
          if (list.getTotalPages > 1) {
            returnString += views.html.shared.pagination.render(jsMethodName = "getBoxesAsJSON", hasNext = list.hasNext, hasPrev = list.hasPrevious, currentPage = page, totalCount = list.getTotalElements, totalPages = list.getTotalPages, messages = request2Messages).toString
          }
          Ok(returnString)
        }else{
          Ok(views.html.browse.browseNoHit.render(Messages("browse.boxes.not-found", routes.BrowsePageController.browseProfiles().url)))
        }
      case None =>
        Ok(views.html.browse.browseNoHit.render(Messages("browse.boxes.not-found", routes.BrowsePageController.browseProfiles().url)))
    }
  }

  private def getBrowseProfileBoxesPaged(boxFilterTag: String, boxFilterCounty: String, boxFilterIsHost: Boolean, pageNo: Int): Option[Page[UserProfile]] = {
    userProfileService.getUserProfilesFiltered(filterTag = fetchTag(boxFilterTag), filterCounty = fetchCounty(boxFilterCounty), filterIsHost = boxFilterIsHost, Some(pageNo), 12).right.get
  }

  private def getBrowseProfileBoxes(boxFilterTag: String, boxFilterCounty: String, boxFilterIsHost: Boolean): Option[List[UserProfile]] = {
    userProfileService.getUserProfilesFiltered(filterTag = fetchTag(boxFilterTag), filterCounty = fetchCounty(boxFilterCounty), filterIsHost = boxFilterIsHost).left.get
  }

  private def getBrowseEventBoxesPaged(boxFilterTag: String, boxFilterCounty: String, pageNo: Int): Option[Page[EventData]] = {
    eventService.getEventsFiltered(filterTag = fetchTag(boxFilterTag), filterCounty = fetchCounty(boxFilterCounty), Some(pageNo), 12).right.get
  }

  private def getBrowseEventBoxes(boxFilterTag: String, boxFilterCounty: String): Option[List[EventData]] = {
    eventService.getEventsFiltered(filterTag = fetchTag(boxFilterTag), filterCounty = fetchCounty(boxFilterCounty)).left.get
  }


  // Helper methods
  private def fetchTag(boxFilterTag: String): Option[TagWord] ={
    boxFilterTag match {
      case "" | null => None
      case tagWord =>
        tagWordService.findById(UUID.fromString(tagWord))
    }
  }

  private def fetchCounty(boxFilterCounty: String): Option[County] ={
    boxFilterCounty match {
      case "" | null => None
      case county =>
        countyService.findById(UUID.fromString(county))
    }
  }

  private def buildEventBoxes(list: List[EventData]): List[BrowseEventBox] = {



    list.map {
      evtData: EventData =>
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


        BrowseEventBox(
          objectId = Some(UUID.fromString(evtData.getobjectId())),
          linkToEvent = linkToEvent,
          eventName = evtData.getName(),
          location = location,
          mainBody = evtData.getpreAmble() match {
            case "" | null =>
              var retBody = Helpers.removeHtmlTags(evtData.getMainBody())

              if (retBody.length > 125)
                retBody = retBody.substring(0, 125) + "..."

              Some(retBody)
            case content => Some(content)
          },
          mainImage = mainImage,
          userImage = userImage,
          price = evtData.getPrice() match {
            case null => 0
            case p => p.toInt
          }
        )
    }
  }


  private def buildProfileBoxes(list: List[UserProfile]): List[BrowseProfileBox] = {
    list.map {
      userProfile: UserProfile =>
        BrowseProfileBox(
          objectId = Some(userProfile.objectId),
          linkToProfile = userProfile.profileLinkName match {
            case null => ""
            case pfName => routes.UserProfileController.viewProfileByName(pfName).url
          },
          fullName = userProfile.profileLinkName,
          location = userProfile.getLocations.asScala.headOption match {
            case None => None
            case Some(countyTag) => Some(countyTag.county.name)
          },
          mainBody = None,
          mainImage = userProfile.getMainImage match {
            case null => None
            case image => Some(routes.ImageController.profileBox(image.getStoreId).url)
          },
          userImage = userProfile.getAvatarImage match {
            case null => None
            case image => Some(routes.ImageController.userThumb(image.getStoreId).url)
          },
          userRating = userProfile.getOwner.getAverageRating,
          isHost = userProfile.isUserHost
        )
    }
  }

}


