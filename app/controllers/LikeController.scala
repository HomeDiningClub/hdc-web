package controllers

import java.util.UUID

import constants.FlashMsgConstants
import enums.RoleEnums
import models.{UserCredential, Recipe}
import models.viewmodels.{LikeForm}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.{RequestHeader, Controller}
import securesocial.core.SecureSocial
import services.{LikeService, RecipeService, UserCredentialService}
import utils.authorization.WithRole
import scala.collection.JavaConverters._

// Object just needs a default constructor
class LikeController extends Controller { }

@SpringController
object LikeController extends Controller with SecureSocial {

  @Autowired
  private var userCredentialService : UserCredentialService = _

  @Autowired
  private var recipeService : RecipeService = _

  @Autowired
  private var likeService : LikeService = _

  // Rating form mapping
  val likeForm = Form(
    mapping(
      "userLikesThisObjectId" -> text,
      "likeValue" -> boolean,
      "likeType" -> nonEmptyText()
    )(LikeForm.apply)(LikeForm.unapply)
  )

  def renderRecipeLikeForm(recipeToBeLiked: Recipe) = { implicit request: RequestHeader =>

    utils.Helpers.getUserFromRequest match {
      case None =>
        views.html.like.hdcLike.render(likeForm, None, recipeToBeLiked.getNrOfLikes, request)
      case Some(currentUser) =>
        val likeType = "recipe"

        // Set old values if user liked before, otherwise create form
        likeService.hasUserLikedThisBefore(currentUser, recipeToBeLiked) match {
          case None =>
            val formValues = LikeForm.apply(recipeToBeLiked.objectId.toString, true, likeType)
            views.html.like.hdcLike.render(likeForm.fill(formValues), None, recipeToBeLiked.getNrOfLikes, request)
          case Some(relation) =>
            views.html.like.hdcLike.render(likeForm, Some(relation.getLastModifiedDate), recipeToBeLiked.getNrOfLikes, request)
        }
    }
  }


  def renderUserLikeForm(userToBeLiked: UserCredential) = { implicit request: RequestHeader =>

    utils.Helpers.getUserFromRequest match {
      case None =>
        views.html.like.hdcLike.render(likeForm, None, userToBeLiked.getNrOfLikes, request)
      case Some(currentUser) =>
        val likeType = "user"

        // Set old values if user liked before, otherwise create form
        likeService.hasUserLikedThisBefore(currentUser, userToBeLiked) match {
          case None =>
            val formValues = LikeForm.apply(userToBeLiked.objectId.toString, true, likeType)
            views.html.like.hdcLike.render(likeForm.fill(formValues), None, userToBeLiked.getNrOfLikes, request)
          case Some(relation) =>
            views.html.like.hdcLike.render(likeForm, Some(relation.getLastModifiedDate), userToBeLiked.getNrOfLikes, request)
        }
    }
  }


  def likeSubmit = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request =>
    val currentUser = utils.Helpers.getUserFromRequest.get

    likeForm.bindFromRequest.fold(
      errors => {
        request.headers.get("referrer") match {
          case Some(referrerUrl) =>
            Redirect(referrerUrl, 302).flashing(FlashMsgConstants.Error -> Messages("like.add.error"))
          case None =>
            Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName)).flashing(FlashMsgConstants.Error -> Messages("rating.add.error"))
        }
      },
      formContent => {

        formContent.likeType match {
          case "user" =>
            userCredentialService.findById(UUID.fromString(formContent.userLikesThisObjectId)) match {
              case None =>
                BadRequest(views.html.like.likeErrorMsg.render(Messages("like.add.error"), "error"))
              case Some(userToBeLiked) => {
                likeService.likeUser(
                  currentUser,
                  userToBeLiked,
                  formContent.likeValue,
                  request.remoteAddress)
              }

                Redirect(routes.UserProfileController.viewProfileByName(userToBeLiked.profiles.asScala.head.profileLinkName))
            }
          case "recipe" =>
            recipeService.findById(UUID.fromString(formContent.userLikesThisObjectId)) match {
              case None =>
                BadRequest(views.html.like.likeErrorMsg.render(Messages("rating.add.error"), "error"))
              case Some(recipeToBeLiked) => {
                likeService.likeRecipe(
                  currentUser,
                  recipeToBeLiked,
                  formContent.likeValue,
                  request.remoteAddress)
              }
                Redirect(routes.RecipePageController.viewRecipeByNameAndProfile(recipeToBeLiked.getOwnerProfile.profileLinkName, recipeToBeLiked.getLink))
            }
          case _ =>
            Logger.error("Cannot accept post without proper Type")
            sys.error("Error in request")

        }
      }
    )
  }
}
