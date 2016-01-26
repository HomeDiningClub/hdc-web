package services

import javax.inject.{Singleton, Named, Inject}

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

// This service is only a created instance of the services needed, use this if you want static access to services without using @Autowired
// Add more services, but please only services, get the repositories using using the services eg. servicename.servicesRepoObject.myRepoFunction()
// Example on real life calls:
// InstancedServices.userCredentialService.userCredentialRepository.save(userCredential)
// InstancedServices.userCredentialService.findUserById(userId)
/*
object InstancedServices {

}

//@Named
@Singleton
@Service
class InstancedServices @Inject()(val userCredentialService: UserCredentialService,
                                  val recipeService: RecipeService,
                                  val mealTypeService: MealTypeService,
                                  val eventService: EventService,
                                  val userProfileService: UserProfileService,
                                  val tagWordService: TagWordService,
                                  val userRoleService: UserRoleService,
                                  val contentFileService: ContentFileService,
                                  val nodeEntityService: NodeEntityService,
                                  val messageService: MessageService) {
/*
  @Autowired
  var userCredentialService: UserCredentialService = _

  @Autowired
  var recipeService: RecipeService = _

  @Autowired
  var mealTypeService: MealTypeService = _

  @Autowired
  var eventService: EventService = _

  @Autowired
  var userProfileService: UserProfileService = _

  @Autowired
  var tagWordService: TagWordService = _

  @Autowired
  var userRoleService: UserRoleService = _

  @Autowired
  var contentFileService: ContentFileService = _

  @Autowired
  var nodeEntityService: NodeEntityService = _

  @Autowired
  var messageService: MessageService = _
  */
}
*/
