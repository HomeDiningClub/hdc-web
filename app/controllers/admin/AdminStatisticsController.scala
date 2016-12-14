package controllers.admin

import javax.inject.{Inject, Named}

import enums.{FileTypeEnums, RoleEnums}
import models.viewmodels.StatisticsData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Controller, RequestHeader}
import securesocial.core.SecureSocial
import services._
import customUtils.authorization.WithRole
import models.UserCredential
import customUtils.security.SecureSocialRuntimeEnvironment

class AdminStatisticsController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                           val recipeService: RecipeService,
                                           val ratingService: RatingService,
                                           val eventService: EventService,
                                           val userCredentialService: UserCredentialService,
                                           val fileService: ContentFileService,
                                           val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {


  def getStatBox: StatisticsData = {

    val currentStats = StatisticsData(
      imagesTotal = fileService.getCountOfAllType(FileTypeEnums.IMAGE.toString),
      recipesTotal = recipeService.getCountOfAll,
      membersTotal = userCredentialService.getCountOfAll,
      ratingsRecipeTotal = ratingService.getCountOfAllRecipesRatings,
      ratingsMemberTotal = ratingService.getCountOfAllMemberRatings,
      eventsTotal = eventService.getCountOfAllEvents,
      eventsBookingsTotal = eventService.getCountOfAllEventBookings
    )

    currentStats

    //views.html.admin.statistics.stat.render(Some(currentStats), request2Messages)
  }

}