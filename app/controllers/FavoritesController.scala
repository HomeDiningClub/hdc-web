package controllers

import java.util
import javax.inject.{Inject, Named}

import models.profile.TaggedFavoritesToUserProfile
import models.{UserCredential, UserProfile, formdata}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.{Environment, Logger}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Controller, RequestHeader}
import play.twirl.api.Html
import securesocial.core.SecureSocial
import services.{NodeEntityService, UserProfileService}

import scala.collection.JavaConverters._
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.FavoriteForm

import scala.collection.mutable.ListBuffer

class FavoritesController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                     val messagesApi: MessagesApi,
                                     implicit val nodeEntityService: NodeEntityService,
                                     val userProfileService: UserProfileService,
                                     val environment: Environment) extends Controller with SecureSocial with I18nSupport {
/*
  // Services
  @Autowired
  var userProfileService: UserProfileService = _
*/

  // List favorites
  def renderFavorites(userProfile: UserProfile)(implicit request: RequestHeader): Html = {

    val listOfMyFavorites = userProfile.getFavorites.iterator()
    val listOfUsersWhoFavorMe = userProfileService.getUserWhoFavoritesUser(userProfile).toIterator

    // Return partial view
    views.html.profile.showListOfFavorites.render(buildMyFavoritesList(listOfMyFavorites), buildFavorsMeList(listOfUsersWhoFavorMe), request2Messages)
  }


  private def buildFavorsMeList(listOfUserFavorMe: Iterator[UserProfile]): List[FavoriteForm] = {
    var favMe: ListBuffer[FavoriteForm] = ListBuffer[FavoriteForm]()

    while (listOfUserFavorMe.hasNext) {
      val cur = listOfUserFavorMe.next()

      val userImage: Option[String] = cur.getAvatarImage match {
        case null => None
        case image =>
          Some(routes.ImageController.userThumb(image.getStoreId).url)
      }

      var preAmble = cur.aboutMeHeadline
      if (preAmble.length > 50) {
        preAmble = preAmble.substring(0, 50) + "..."
      }

      // Add to return variable
      favMe += formdata.FavoriteForm(cur.profileLinkName, cur.getOwner.firstName, preAmble, userImage, cur.objectId.toString, cur.getOwner.objectId.toString)
      Logger.debug("ObjectId : " + cur.objectId + ", email : " + cur.getOwner.emailAddress + ", LinkName: " + cur.profileLinkName)
    }
    favMe.toList
  }

  private def buildMyFavoritesList(listOfFavorites: util.Iterator[TaggedFavoritesToUserProfile]): List[FavoriteForm] = {
    var favorites: ListBuffer[FavoriteForm] = ListBuffer[FavoriteForm]()

    while (listOfFavorites.hasNext) {
      val cur = listOfFavorites.next()
      val fav: UserProfile = cur.favoritesUserProfile

      val userImage: Option[String] = fav.getAvatarImage match {
        case null => None
        case image =>
          Some(routes.ImageController.userThumb(image.getStoreId).url)
      }

      var preAmble = fav.aboutMeHeadline
      if (preAmble.length > 50) {
        preAmble = preAmble.substring(0, 50) + "..."
      }

      // Add to return variable
      favorites += formdata.FavoriteForm(fav.profileLinkName, fav.getOwner.firstName, preAmble, userImage, fav.objectId.toString, fav.getOwner.objectId.toString)
      Logger.debug("ObjectId : " + fav.objectId + ", email : " + fav.getOwner.emailAddress)
    }
    favorites.toList
  }

}
