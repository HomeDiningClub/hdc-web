package controllers

import models.{UserProfile, UserCredential}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.{RequestHeader, Controller}
import securesocial.core.SecureSocial
import services.UserProfileService
import scala.collection.JavaConverters._

class FavoritesController extends Controller with SecureSocial {}

@SpringController
object FavoritesController extends Controller with SecureSocial {

  // Services
  @Autowired
  var userProfileService: UserProfileService = _

  // List favorites
  def renderFavorites(userProfile: UserProfile) = {

    var listOfFavorites = userProfile.getFavorites.iterator()
    var listOfUserFavorMe = userProfileService.getUserWhoFavoritesUser(userProfile).toIterator
    var favorites = scala.collection.mutable.ListBuffer[models.viewmodels.FavoriteForm]()
    var favMe = scala.collection.mutable.ListBuffer[models.viewmodels.FavoriteForm]()

    // me favorites others
    while(listOfFavorites.hasNext) {
      var cur = listOfFavorites.next()
      var fav : UserProfile = cur.favoritesUserProfile

      val userImage: Option[String] = fav.getAvatarImage match {
        case null => None
        case image =>
          Some(routes.ImageController.userThumb(image.getStoreId).url)
      }

      var preAmble = fav.aboutMeHeadline
      if(preAmble.length > 50) {
        preAmble.substring(0, 50) + "..."
      }

      // add to return variable ....
      favorites += models.viewmodels.FavoriteForm(fav.profileLinkName, fav.getOwner.firstName(), preAmble, userImage, fav.objectId.toString, fav.getOwner.objectId.toString)
      println("ObjectId : " + fav.objectId + ", email : " + fav.email)
    }

    // favorites med
    while(listOfUserFavorMe.hasNext) {
      var cur = listOfUserFavorMe.next()

      val userImage: Option[String] = cur.getAvatarImage match {
        case null => None
        case image =>
          Some(routes.ImageController.userThumb(image.getStoreId).url)
      }

      var preAmble = cur.aboutMeHeadline
      if(preAmble.length > 50) {
        preAmble.substring(0, 50) + "..."
      }

      // add to return variable ....
      favMe += models.viewmodels.FavoriteForm(cur.profileLinkName, cur.getOwner.firstName(), preAmble, userImage, cur.objectId.toString, cur.getOwner.objectId.toString)
      println("ObjectId : " + cur.objectId + ", email : " + cur.email + ", LinkName: " + cur.profileLinkName)
    }

    // return partial view
    views.html.profile.showListOfFavorites.render(favorites.toList, favMe.toList)
  }


}
