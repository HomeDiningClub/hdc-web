package services

import java.util.UUID

import models.files.ContentFile
import models.modelconstants.UserLevelScala
import models.{Recipe, UserCredential, UserProfile}
import org.neo4j.graphdb._
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Sort, Page, PageRequest, Pageable}
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import repositories.UserProfileRepository
import securesocial.core.Identity
import scala.collection.JavaConverters._
import scala.List
import scala.language.implicitConversions
import org.springframework.transaction.annotation.Transactional
import org.neo4j.graphdb.index.Index
import org.springframework.context.annotation.Lazy
import play.api.mvc.Results._
import models.location.County
import models.profile.{TaggedFavoritesToUserProfile, TaggedUserProfile, TagWord}
import scala.collection.JavaConverters._


@Service
object  UserProfileService {
  /*
  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userProfileRepository: UserProfileRepository = _
  */
}


@Service
class UserProfileService {

  // http://books.google.se/books?id=DeTO4xbC-eoC&pg=PT158&lpg=PT158&dq=:+Neo4jTemplate+%3D+_&source=bl&ots=kTYWRxqdkm&sig=nNZ8mMMFgx4CmCFw13zWeQdnzFA&hl=en&sa=X&ei=RK9eU7v-KI7jO5m9gegO&ved=0CCwQ6AEwATgK#v=onepage&q=%3A%20Neo4jTemplate%20%3D%20_&f=false

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userProfileRepository: UserProfileRepository = _


  @Transactional(readOnly = false)
  def saveUserProfile(userProfile: models.UserProfile): models.UserProfile = {

    println("save id: " + userProfile.userIdentity )
    println("save provider id  " + userProfile.providerIdentity)

    var modUserProfile = userProfileRepository.save(userProfile)

    modUserProfile
  }



  @Transactional(readOnly = false)
  def addFavorites(
                             theUser:  models.UserProfile,
                             friendsUserCredential:  models.UserCredential
                            ): models.UserProfile = {

    if(friendsUserCredential!=None && friendsUserCredential!= null) {
      var fUserProfile : UserProfile = friendsUserCredential.profiles.asScala.head
      theUser.addFavoriteUserProfile(fUserProfile)
    }

    theUser
  }



  @Transactional(readOnly = false)
  def removeFavorites(
                    theUser:  models.UserProfile,
                    friendsUserCredential:  models.UserCredential
                    ): models.UserProfile = {

    if(friendsUserCredential!=None && friendsUserCredential!= null) {
      var fUserProfile : UserProfile = friendsUserCredential.profiles.asScala.head

      var jmfFriend = friendsUserCredential.objectId
      var profileLink : Option[TaggedFavoritesToUserProfile] = None


      var itter = theUser.getFavorites.iterator()
      while(itter.hasNext) {
        var tagProfile = itter.next()
        if(tagProfile.favoritesUserProfile.getOwner.objectId == jmfFriend) {
          profileLink = Some(tagProfile)
        }
      }

      theUser.removeFavoriteUserProfile(profileLink.get)
    }

    theUser
  }




  @Transactional(readOnly = true)
  def isFavoritesToMe(
                       theUser:  models.UserProfile,
                       friendsUserCredential:  models.UserCredential
                       ): Boolean = {

    var isFriend      : Boolean = false
    var profileLink   : Option[TaggedFavoritesToUserProfile] = None

    if(friendsUserCredential!=None && friendsUserCredential!= null) {
      var fUserProfile : UserProfile = friendsUserCredential.profiles.asScala.head

      var jmfFriend = friendsUserCredential.objectId



      var itter = theUser.getFavorites.iterator()
      while(itter.hasNext) {
        var tagProfile = itter.next()
        if(tagProfile.favoritesUserProfile.getOwner.objectId.toString == jmfFriend.toString) {
          profileLink = Some(tagProfile)
        }
      }

    }
      var isFavoriteTo =   profileLink  match {
        case None         => false
        case null         => false
        case profileLink  => true
      }

    isFavoriteTo
  }





  @Transactional(readOnly = false)
  def updateUserProfileTags(
    theUser:  models.UserProfile,
    d:        Option[List[TagWord]],
    map:      Map[String, String] ): models.UserProfile = {

    theUser.removeAllTags()

    // Fetch all tags available to choose
    if (d.isDefined)
    {
      // Loop all available tags
      for (theTag <- d.get)
      {
        var value = map.getOrElse(theTag.tagName, "empty")

        if (!value.equals("empty")) {

          // If the the user have tagged the particial chooice tag it
          theUser.tag(theTag)
        }

      } // end loop

    }

    saveUserProfile(theUser)

  }



  @Transactional(readOnly = false)
  def addRecipeToProfile(user: UserCredential, recipeToAdd: Recipe): UserProfile = {
    val userProfile = user.profiles.iterator().next()
    var modUserProfile = this.addRecipeToProfile(userProfile,recipeToAdd)
    modUserProfile = userProfileRepository.save(userProfile)
    modUserProfile
  }

  @Transactional(readOnly = false)
  def addRecipeToProfile(userProfile: UserProfile, recipeToAdd: Recipe): UserProfile =
  {
    userProfile.addRecipe(recipeToAdd)
  }

  def findByprofileLinkName(profileName: String): Option[UserProfile] =
  {
    var returnObject: Option[UserProfile] = None
    if(profileName.nonEmpty)
    {
      returnObject = userProfileRepository.findByprofileLinkName(profileName) match {
        case null => None
        case profile =>
          // Lazy fetching
//          if(fetchAll){
//            template.fetch(profile.getRecipes)
//          }
          Some(profile)
      }
    }
    returnObject
  }


  def findByowner(userCred: UserCredential): Option[UserProfile] =
  {
    userProfileRepository.findByowner(userCred) match {
      case null => None
      case profile =>
        // Lazy fetching
//        if(fetchAll){
//          template.fetch(profile.getRecipes)
//        }
        Some(profile)
    }
  }

  def findUserProfileByUserId(id: Identity) : Option[UserProfile] =
  {
//    var key : String = id.identityId.userId + "_" + id.identityId.providerId
//    var up = UserProfileService.userProfileRepository.findByUserIdentityAndProviderIdentity(id.identityId.userId, id.identityId.providerId)

    var lista = userProfileRepository.findAll().iterator()

    while(lista.hasNext) {
      var v = lista.next()
      println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
      println("userId " + v.userIdentity + " provider id :" + v.providerIdentity)
      println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")

      if(v.userIdentity.equalsIgnoreCase(id.identityId.userId) && v.providerIdentity.equalsIgnoreCase(id.identityId.providerId)){
        println("OK")
        return Some(v)
      } else {
        println("No match")
      }
    }
    None
  }




/*
  @Transactional(readOnly = false)
  def saveUserProfile(userProfile: models.UserProfile): models.UserProfile = {

    println("ID: " + userProfile.objectId)
    var modUserProfile = UserProfileService.userProfileRepository.save(userProfile)

    modUserProfile
  }
*/
/*
  @Transactional(readOnly = true)
  def findByProfileLinkName(profileLinkName : String) : UserProfile = {
    val userProfile : models.UserProfile = userProfileRepository.findByProfileLinkName(profileLinkName)
    userProfile
  }
  */

  @Transactional(readOnly = true)
  def getAllUserProfiles: List[models.UserProfile] = {
    val listOfUserProfiles: List[models.UserProfile] = IteratorUtil.asCollection(userProfileRepository.findAll()).asScala.toList
    listOfUserProfiles
  }


  @Transactional(readOnly = false)
  def removeAllLocationTags(userProfile: UserProfile): UserProfile = {
    userProfile.removeLocation()
    userProfile
  }

  @Transactional(readOnly = false)
  def removeAllProfileTags(userProfile: UserProfile): UserProfile = {
    userProfile.removeAllTags()
    userProfile
  }

  @Transactional(readOnly = false)
  def addProfileTag(userProfile: UserProfile, tag: TagWord): UserProfile = {
    userProfile.tag(tag)
    userProfile
  }

  @Transactional(readOnly = false)
  def addLocation(userProfile: UserProfile, county: County): UserProfile = {
    userProfile.locate(county)
    userProfile
  }

  @Transactional(readOnly = false)
  def setAndRemoveMainImage(userProfile: UserProfile, newImage: ContentFile): UserProfile = {
    userProfile.setAndRemoveMainImage(newImage)
    userProfile
  }

  @Transactional(readOnly = false)
  def setAndRemoveAvatarImage(userProfile: UserProfile, newImage: ContentFile): UserProfile = {
    userProfile.setAndRemoveAvatarImage(newImage)
    userProfile
  }


  @Transactional(readOnly = true)
  def getUserProfile(userName : String) = {


  print("getUserProfile : " + userName)

  if(template == null){
    println("template is null ********************************")
  } else {
    println("template is not null*****************************")
  }


    var userProfileDataIndex: Index[Node] = template.getIndex(classOf[models.UserProfile], "userName")
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
      currNode = userProfileRepository.findOne(id)
    }

    currNode
  }



//  @Transactional(readOnly = false)
//  def updateUserProfile(userProfile: UserProfile, reqUserProfile: AnvandareForm): UserProfile = {
//    userProfileRepository.findAll().asScala.toList
//  }



  @Transactional(readOnly = true)
  def getAllUserProfile: List[models.UserProfile] = {
      userProfileRepository.findAll().asScala.toList
  }


  // Can return either:
  // Option[Page[UserProfile]]
  // Option[List[UserProfile]]
  @Transactional(readOnly = true)
  def getUserProfilesFiltered(filterTag: Option[TagWord], filterCounty: Option[County], filterIsHost: Boolean, pageNo: Option[Integer] = None, nrPerPage: Int = 9) = {

    var returnList: List[UserProfile] = Nil
    var returnPaged: Page[UserProfile] = null

    (filterTag, filterCounty, filterIsHost) match {
      case (Some(tw), Some(cnt), true) =>
        if(pageNo.isDefined){
          returnPaged = userProfileRepository.findByTagWordIdAndCountyIdAndIsHost(tw.objectId.toString, cnt.objectId.toString, UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        }else{
          returnList = userProfileRepository.findByTagWordIdAndCountyIdAndIsHost(tw.objectId.toString, cnt.objectId.toString, UserLevelScala.HOST.Constant).asScala.toList
        }
      case (Some(tw), Some(cnt), false) =>
        if(pageNo.isDefined){
          returnPaged = userProfileRepository.findByTagWordIdAndCountyId(tw.objectId.toString, cnt.objectId.toString, new PageRequest(pageNo.get, nrPerPage))
        }else{
          returnList = userProfileRepository.findByTagWordIdAndCountyId(tw.objectId.toString, cnt.objectId.toString).asScala.toList
        }
      case (Some(tw), None, true) =>
        if(pageNo.isDefined) {
          returnPaged = userProfileRepository.findByTagWordIdAndIsHost(tw.objectId.toString, UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        }else{
          returnList = userProfileRepository.findByTagWordIdAndIsHost(tw.objectId.toString, UserLevelScala.HOST.Constant).asScala.toList
        }
      case (Some(tw), None, false) =>
        if(pageNo.isDefined) {
          returnPaged = userProfileRepository.findByTagWordId(tw.objectId.toString, new PageRequest(pageNo.get, nrPerPage))
        }else{
          returnList = userProfileRepository.findByTagWordId(tw.objectId.toString).asScala.toList
        }
      case (None, Some(cnt), true) =>
        if(pageNo.isDefined) {
          returnPaged = userProfileRepository.findByCountyIdAndIsHost(cnt.objectId.toString, UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        }else{
          returnList = userProfileRepository.findByCountyIdAndIsHost(cnt.objectId.toString, UserLevelScala.HOST.Constant).asScala.toList
        }
      case (None, Some(cnt), false) =>
        if(pageNo.isDefined) {
          returnPaged = userProfileRepository.findByCountyId(cnt.objectId.toString, new PageRequest(pageNo.get, nrPerPage))
        }else{
          returnList = userProfileRepository.findByCountyId(cnt.objectId.toString).asScala.toList
        }
      case (None, None, true) =>
        if(pageNo.isDefined) {
          returnPaged = userProfileRepository.findByIsHost(UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        }else{
          returnList = userProfileRepository.findByIsHost(UserLevelScala.HOST.Constant).asScala.toList
        }
      case _ =>
        // Sorting, works but not with relations
        //val sorting = new Sort(new Sort.Order(Sort.Direction.ASC, "mainImage", Sort.NullHandling.NULLS_LAST))

        if(pageNo.isDefined) {
          returnPaged = userProfileRepository.findAllProfiles(new PageRequest(pageNo.get, nrPerPage))
        }else{
          returnList = userProfileRepository.findAllProfiles().asScala.toList
        }
    }

    // Return paged list, or normal list or just None
    if(returnList != Nil){
      Some(returnList)
    }else if(returnPaged != null){
      Some(returnPaged)
    }else{
      None
    }
  }


  @Transactional(readOnly = true)
  def getUserWhoFavoritesUser(userProfile: models.UserProfile): List[models.UserProfile] = {
    userProfileRepository.findFriendsToUser(userProfile.objectId).asScala.toList.filter(p=>p.profileLinkName != null && p.profileLinkName.nonEmpty)
  }



}


