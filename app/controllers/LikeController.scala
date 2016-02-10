package controllers

import java.util.UUID
import javax.inject.{Named, Inject}

import constants.FlashMsgConstants
import enums.RoleEnums
import models.{Event, UserCredential, Recipe}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import play.api.mvc.{AnyContent, RequestHeader, Controller}
import play.twirl.api.Html
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.{LikeService, RecipeService, UserCredentialService}
import customUtils.authorization.WithRole
import scala.collection.JavaConverters._
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.LikeForm

class LikeController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                val userCredentialService: UserCredentialService,
                                val recipeService: RecipeService,
                                val likeService: LikeService,
                                val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  /*
  @Autowired
  private var userCredentialService : UserCredentialService = _

  @Autowired
  private var recipeService : RecipeService = _

  @Autowired
  private var likeService : LikeService = _
*/

  // Rating form mapping
  val likeForm = Form(
    mapping(
      "userLikesThisObjectId" -> text,
      "likeValue" -> boolean,
      "likeType" -> nonEmptyText()
    )(LikeForm.apply)(LikeForm.unapply)
  )

  def renderRecipeLikeForm(recipeToBeLiked: Recipe, currentUser: Option[UserCredential])(implicit request: RequestHeader): Html = {

    currentUser match {
      case None =>
        views.html.like.hdcLike.render(likeForm, None, recipeToBeLiked.getNrOfLikes, None, request2Messages)
      case Some(cu) =>
        val likeType = "recipe"

        // Set old values if user liked before, otherwise create form
        likeService.hasUserLikedThisBefore(cu, recipeToBeLiked) match {
          case None =>
            val formValues = LikeForm.apply(recipeToBeLiked.objectId.toString, likeValue = true, likeType)
            views.html.like.hdcLike.render(likeForm.fill(formValues), None, recipeToBeLiked.getNrOfLikes, Some(cu), request2Messages)
          case Some(relation) =>
            views.html.like.hdcLike.render(likeForm, Some(relation.getLastModifiedDate), recipeToBeLiked.getNrOfLikes, Some(cu), request2Messages)
        }
    }
  }

  def renderEventLikeForm(eventToBeLiked: Event, currentUser: Option[UserCredential])(implicit request: RequestHeader): Html = {

    currentUser match {
      case None =>
        views.html.like.hdcLike.render(likeForm, None, eventToBeLiked.getNrOfLikes, None, request2Messages)
      case Some(cu) =>
        val likeType = "event"

        // Set old values if user liked before, otherwise create form
        likeService.hasUserLikedThisBefore(cu, eventToBeLiked) match {
          case None =>
            val formValues = LikeForm.apply(eventToBeLiked.objectId.toString, likeValue = true, likeType)
            views.html.like.hdcLike.render(likeForm.fill(formValues), None, eventToBeLiked.getNrOfLikes, Some(cu), request2Messages)
          case Some(relation) =>
            views.html.like.hdcLike.render(likeForm, Some(relation.getLastModifiedDate), eventToBeLiked.getNrOfLikes, Some(cu), request2Messages)
        }
    }
  }

  def renderUserLikeForm(userToBeLiked: UserCredential, currentUser: Option[UserCredential])(implicit request: RequestHeader): Html = {

    currentUser match {
      case None =>
        views.html.like.hdcLike.render(likeForm, None, userToBeLiked.getNrOfLikes, None, request2Messages)
      case Some(cu) =>
        val likeType = "user"

        // Set old values if user liked before, otherwise create form
        likeService.hasUserLikedThisBefore(cu, userToBeLiked) match {
          case None =>
            val formValues = LikeForm.apply(userToBeLiked.objectId.toString, likeValue = true, likeType)
            views.html.like.hdcLike.render(likeForm.fill(formValues), None, userToBeLiked.getNrOfLikes, Some(cu), request2Messages)
          case Some(relation) =>
            views.html.like.hdcLike.render(likeForm, Some(relation.getLastModifiedDate), userToBeLiked.getNrOfLikes, Some(cu), request2Messages)
        }
    }
  }


  def likeSubmit = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val currentUser = request.user

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
