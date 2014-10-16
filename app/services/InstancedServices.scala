package services

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

// This service is only a created instance of the services needed, use this if you want static access to services without using @Autowired
// Add more services, but please only services, get the repositories using using the services eg. servicename.servicesRepoObject.myRepoFunction()
// Example on real life calls:
// InstancedServices.userCredentialService.userCredentialRepository.save(userCredential)
// InstancedServices.userCredentialService.findUserById(userId)
@Service
object InstancedServices {

  @Autowired
  var userCredentialService: UserCredentialService = _

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
}

// Just here for default constructor, don't add code in this class!
class InstancedServices {

}
