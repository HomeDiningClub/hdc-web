package controllers

import java.util
import javax.inject.{Inject, Named}

import models.profile.{FavoriteData, TaggedFavoritesToUserProfile}
import models.{UserCredential, UserProfile, formdata}
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
import org.springframework.data.neo4j.support.Neo4jTemplate

import scala.collection.mutable.ListBuffer

class FavoritesController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                     val messagesApi: MessagesApi,
                                     val template: Neo4jTemplate,
                                     implicit val nodeEntityService: NodeEntityService,
                                     val userProfileService: UserProfileService,
                                     val environment: Environment) extends Controller with SecureSocial with I18nSupport {

  // List favorites
  def renderFavorites(userProfile: UserProfile)(implicit request: RequestHeader): Option[Html] = {
    val perf = customUtils.Helpers.startPerfLog()
    val listOfMyFavorites = userProfileService.getMyFavorites(userProfile)
    val listOfUsersWhoFavorMe = userProfileService.getUserWhoFavoritesUser(userProfile)
    // Return partial view
    val r =  Some(views.html.profile.showListOfFavorites.render(buildFavForm(listOfMyFavorites), buildFavForm(listOfUsersWhoFavorMe), request2Messages))
    customUtils.Helpers.endPerfLog("renderFavo", perf)
    r
  }

  private def buildFavForm(listOfUserFavorMe: Option[List[FavoriteData]]): List[FavoriteForm] = {
    listOfUserFavorMe match {
      case None => Nil
      case Some(list) => list.map { fav =>

        var avatarImage: Option[String] = None
        if(fav.getAvatarImage().asScala.nonEmpty){
          avatarImage = Some(routes.ImageController.userThumb(fav.getAvatarImage().asScala.head).url)
        }

        var preAmble = fav.getAboutMeHeadline()
        if (preAmble.length > 50) {
          preAmble = preAmble.substring(0, 50) + "..."
        }

        // Add to return variable
        formdata.FavoriteForm(fav.getProfileLinkName(), fav.getFirstName(), preAmble, avatarImage, fav.getUserProfileObjectId(), fav.getUserCredentialObjectId())
      }
    }
  }

/*
  private def buildMyFavoritesList(listOfFavorites: Option[List[FavoriteData]]): List[FavoriteForm] = {
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
    }
    favorites.toList
  }
  */

}
