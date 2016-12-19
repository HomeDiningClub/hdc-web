package controllers

import javax.inject.{Inject, Named}

import models.event.EventData
import org.springframework.data.domain.Page
import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.viewmodels.{EventBox, ProfileBox, ReviewBox}
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
import models.content.ContentPage
import models.formdata.SearchStartPageForm
import play.api.Environment

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

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

  def index(fTag: String, fCounty: String, fHost: Boolean) = UserAwareAction() { implicit request =>

    //val isHost = if(fHost == 1) true else false
    val isHost = fHost
    val perf = customUtils.Helpers.startPerfLog()

    val dataAsync = for {
      profileBoxes <- Future(getProfileBoxes(fTag, fCounty, isHost, 6)) // TODO: This is slow, improve performance
      eventBoxes <- Future(getEventBoxes(fTag, fCounty, 6))
      foodAreas <- Future(tagWordService.getFoodAreas)
      counties <- Future(countyService.getCounties)
      reviewBoxes <- Future(ratingService.getUserReviewBoxesStartPage(4))
      asideNews <- Future(contentService.getAsideNewsItems)
      news <- Future(contentService.getNewsItems)
    } yield (profileBoxes, eventBoxes, foodAreas, counties, reviewBoxes, asideNews, news)

    val form = SearchStartPageForm.apply(
      fCounty match { case null | "" => None case item => Some(item)},
      fTag match { case null | "" => None case item => Some(item)},
      isHost match { case false => None case item => Some(item)}
    )

    val res = Await.result(dataAsync, Duration.Inf)
    customUtils.Helpers.endPerfLog("StartPage: - Loading time: ", perf)

      Ok(views.html.startpage.index(
        searchForm = searchForm.fill(form),
        optionsFoodAreas = res._3,
        optionsLocationAreas = res._4,
        optionsIsHost = if(isHost) Some(true) else Some(false),
        eventBoxes = res._2,
        profileBoxes = res._1,
        reviewBoxes = res._5,
        asideNews = res._6,
        news = res._7,
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

    var perf = customUtils.Helpers.startPerfLog()
    val maxNrTemp = 70
    val dataOpt = userProfileService.getUserProfilesFiltered(filterTag = fetchedTag, filterCounty = fetchedCounty, filterIsHost = boxFilterIsHost, pageNo = Some(0), nrPerPage = maxNrTemp).right.get
    customUtils.Helpers.endPerfLog("getData: ", perf)

    val data = dataOpt match {
      case None => None
      case Some(d) => Some(d.getContent.asScala.toList)
    }

    perf = customUtils.Helpers.startPerfLog()
    val profBoxes: Option[List[ProfileBox]] = data match {
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
            userRating = ratingService.getAverageRatingForUser(userProfile.getOwner.objectId), //.getAverageRating,
            isHost = userProfile.isUserHost
          )
      })
    }
    customUtils.Helpers.endPerfLog("building: ", perf)
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


