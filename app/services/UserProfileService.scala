package services

import models.UserProfile
import org.neo4j.graphdb._
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import repositories.UserProfileRepository
import securesocial.core.Identity
import scala.collection.JavaConverters._
import scala.List
import scala.language.implicitConversions
import org.springframework.transaction.annotation.Transactional
import org.neo4j.graphdb.index.Index


@Service
object UserProfileService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userProfileRepository: UserProfileRepository = _


 /*
  def findUserProfileByUserId(id: Identity) : Option[UserProfile] =
  {

    println("metod ...")

    var key : String = id.identityId.userId + "_" + id.identityId.providerId


    var up = userProfileRepository.findAll()

    var vv = up.iterator()

    while(vv.hasNext) {
     var up2 = Some(vv.next())
    println("provider Id: " + up2.getOrElse(new UserProfile).aboutMe)

    }


    None
  }

*/



}


@Service
class UserProfileService {


  // http://books.google.se/books?id=DeTO4xbC-eoC&pg=PT158&lpg=PT158&dq=:+Neo4jTemplate+%3D+_&source=bl&ots=kTYWRxqdkm&sig=nNZ8mMMFgx4CmCFw13zWeQdnzFA&hl=en&sa=X&ei=RK9eU7v-KI7jO5m9gegO&ved=0CCwQ6AEwATgK#v=onepage&q=%3A%20Neo4jTemplate%20%3D%20_&f=false

/*
  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userProfileRepository: UserProfileRepository = _
*/

  @Transactional(readOnly = false)
  def saveUserProfile(userProfile: models.UserProfile): models.UserProfile = {

    //println("ID: " + userProfile.objectId)
    var modUserProfile = UserProfileService.userProfileRepository.save(userProfile)

    modUserProfile
  }


  def findUserProfileByUserId(id: Identity) : Option[UserProfile] =
  {

    println("metod ...")

    var key : String = id.identityId.userId + "_" + id.identityId.providerId


    //var up = UserProfileService.userProfileRepository.findByKeyIdentity(key)
    var up = UserProfileService.userProfileRepository.findAll().iterator()


    while(up.hasNext) {
      var obj = up.next()

      if(key == obj.keyIdentity) {
          println("*key* : " + obj.keyIdentity +" --"+ key)
          return Some(obj)
      }
    }



    None
  }




/*
  @Transactional(readOnly = false)
  def saveUserProfile(userProfile: models.UserProfile): models.UserProfile = {

    println("ID: " + userProfile.objectId)
    var modUserProfile = userProfileRepository.save(userProfile)

    modUserProfile
  }
*/

  @Transactional(readOnly = true)
  def findByProfileLinkName(profileLinkName : String) : UserProfile = {
    var userProfile : models.UserProfile = UserProfileService.userProfileRepository.findByProfileLinkName(profileLinkName)
    userProfile
  }

  @Transactional(readOnly = true)
  def getAllUserProfiles: List[models.UserProfile] = {
    val listOfUserProfiles: List[models.UserProfile] = IteratorUtil.asCollection(UserProfileService.userProfileRepository.findAll()).asScala.toList
    listOfUserProfiles
  }




  @Transactional(readOnly = true)
  def getUserProfile(userName : String) = {


  print("getUserProfile : " + userName)

  if(UserProfileService.template == null){
    println("template is null ********************************")
  } else {
    println("template is not null*****************************")
  }


    var userProfileDataIndex: Index[Node] = UserProfileService.template.getIndex(classOf[models.UserProfile], "userName")
    //val node: Node = userProfileDataIndex.query("userName", userName).getSingle()

  if(userProfileDataIndex == null){
    println("userProfileDataIndex is null ********************************")
  } else {
    println("userProfileDataIndex is not null*****************************")
  }



    var hits = userProfileDataIndex.query("userName", userName)
    var nods = hits.iterator()
    var id : Long = 0

    println("...........................................")


    if(nods.hasNext) {
      id = nods.next().getId
      println("Id: " + id)
    }


    var currNode: models.UserProfile = new models.UserProfile

    // node != null
    if (id != 0) {
      currNode = UserProfileService.userProfileRepository.findOne(id)
    }

    currNode
  }


    @Transactional(readOnly = true)
  def getAllUserProfile: List[models.UserProfile] = {
      UserProfileService.userProfileRepository.findAll().asScala.toList
  }





}


