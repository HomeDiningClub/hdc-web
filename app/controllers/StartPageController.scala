package controllers

import javax.inject.{Inject, Named}

import models.event.EventData
import org.springframework.data.domain.Page
import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.viewmodels.{EventBox, ProfileBox}
import org.springframework.beans.factory.annotation.Autowired
import securesocial.core.SecureSocial
import services._
import models.{UserCredential, UserProfile}
import views.html.helper.{options, select}
import models.profile.TagWord
import models.location.County
import java.util.UUID

import scala.collection.JavaConverters._
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.SearchStartPageForm
import play.api.Environment

class StartPageController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                     val userProfileService: UserProfileService,
                                     val eventService: EventService,
                                     val tagWordService: TagWordService,
                                     val countyService: CountyService,
                                     val ratingService: RatingService,
                                     val contentService: ContentService,
                                     val messagesApi: MessagesApi,
                                     val environment: Environment) extends Controller with SecureSocial with I18nSupport {


  // Search form
  val searchForm = Form(
    mapping(
      //"freeText" -> optional(text),
      "fCounty" -> optional(text),
      "fTag" -> optional(text),
      "fHost" -> optional(boolean)
    )(SearchStartPageForm.apply)(SearchStartPageForm.unapply)
  )

  def index(fTag: String, fCounty: String, fHost: Int) = UserAwareAction() { implicit request =>

    val isHost = if(fHost == 1) true else false

    val profileBoxes = getProfileBoxes(fTag, fCounty, isHost, 6)
    val eventBoxes = getEventBoxes(fTag, fCounty, 6)

    val form = SearchStartPageForm.apply(
      fCounty match { case null | "" => None case item => Some(item)},
      fTag match { case null | "" => None case item => Some(item)},
      isHost match { case false => None case item => Some(item)}
    )

    Ok(views.html.startpage.index(
      searchForm = searchForm.fill(form),
      optionsFoodAreas = tagWordService.getFoodAreas,
      optionsLocationAreas = countyService.getCounties,
      optionsIsHost = if(isHost) Some(true) else Some(false),
      eventBoxes = eventBoxes,
      profileBoxes = profileBoxes,
      reviewBoxes = ratingService.getUserReviewBoxesStartPage(4), //TODO: Fix reviewBoxes speed
      asideNews = contentService.getAsideNewsItems,
      news = contentService.getNewsItems,
      currentUser = request.user
    ))
  }

  private def getEventBoxes(boxFilterTag: String, boxFilterCounty: String, maxNr: Int = 8): Option[List[EventBox]] = {

    val fetchedTag: Option[TagWord] = verifySelectedTagWord(boxFilterTag)
    val fetchedCounty: Option[County] = verifySelectedCounty(boxFilterCounty)

    val eventBoxes: Option[List[EventBox]] = eventService.getEventsFiltered(fetchedTag, fetchedCounty, Some(0), maxNr).right.get match {
      case None => None
      case Some(events) => eventService.mapEventDataToEventBox(events)
    }
    eventBoxes
  }

  private def getProfileBoxes(boxFilterTag: String, boxFilterCounty: String, boxFilterIsHost: Boolean, maxNr: Int = 8): Option[List[ProfileBox]] = {

    val fetchedTag: Option[TagWord] = verifySelectedTagWord(boxFilterTag)
    val fetchedCounty: Option[County] = verifySelectedCounty(boxFilterCounty)

    val profBoxes: Option[List[ProfileBox]] = userProfileService.getUserProfilesFiltered(filterTag = fetchedTag, filterCounty = fetchedCounty, filterIsHost = boxFilterIsHost).asInstanceOf[Option[List[UserProfile]]] match {
      case None => None
      case Some(profile) => Some(profile.filter(prof => prof.getMainImage != null).take(maxNr).map {
        userProfile: UserProfile =>
          ProfileBox(
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
      })
    }
    profBoxes
  }


  private def verifySelectedCounty(boxFilterCounty: String): Option[County] = {
    boxFilterCounty match {
      case "" | null => None
      case county =>
        countyService.findById(UUID.fromString(county))
    }
  }

  private def verifySelectedTagWord(boxFilterTag: String): Option[TagWord] = {
    boxFilterTag match {
      case "" | null => None
      case tagWord =>
        tagWordService.findById(UUID.fromString(tagWord))

    }
  }
}


