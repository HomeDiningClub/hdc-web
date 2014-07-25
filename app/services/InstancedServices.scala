package services

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

// This service is only an created instance of the services needed, use this if you want static access to services without using @Autowired
// Add more services, but please only services, get the repositories using useing the services eg. servicename.servicesRepoObject.myRepoFunction()
// Example on real life call:
// InstancedServices.userCredentialService.userCredentialRepository.save(userCredential)
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
}

// Just here for default constructor, don't add code in this class!
class InstancedServices {

}
