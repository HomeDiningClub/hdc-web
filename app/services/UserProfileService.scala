package services

import javax.inject.Inject

import models.files.ContentFile
import models.modelconstants.UserLevelScala
import models._
import org.neo4j.graphdb._
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.data.domain.{Sort, Page, PageRequest, Pageable}
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import play.api.Logger
import repositories.{TagWordRepository, ViewedByUnKnownRepository, UserProfileRepository, ViewedByMemberRepository}
import traits.TransactionSupport
import scala.collection.JavaConverters._
import scala.language.implicitConversions
import org.springframework.transaction.annotation.Transactional
import org.neo4j.graphdb.index.Index
import models.location.County
import models.profile.{TaggedFavoritesToUserProfile, TagWord}
import scala.collection.JavaConverters._
import customUtils.ViewedByMemberUtil

//@Service
class UserProfileService @Inject()(val template: Neo4jTemplate,
                                   val userProfileRepository: UserProfileRepository,
                                   val viewedByMemberRepository: ViewedByMemberRepository,
                                   val tagWordRepository: TagWordRepository,
                                   val viewedByUnKnownRepository: ViewedByUnKnownRepository) extends TransactionSupport {

  // save UnKnow user access to page
  //@Transactional(readOnly = false)
  def saveUnKnownAccess(view: models.ViewedByUnKnown): models.ViewedByUnKnown = withTransaction(template) {
    var newView = viewedByUnKnownRepository.save(view)
    newView
  }

  // save member access to page
  //@Transactional(readOnly = false)
  def saveMemberAccess(view: models.ViewedByMember): models.ViewedByMember = withTransaction(template) {
    var newView = viewedByMemberRepository.save(view)
    newView
  }


  // logged in user accessing profile page
  //@Transactional(readOnly = false)
  def logProfileViewByObjectId(viewdByMember: ViewedByMember, viewerObjectId: String, pageOwnerObjectId: String) = withTransaction(template) {
    var util = new ViewedByMemberUtil()
    viewdByMember.viewedBy(viewerObjectId, util.getNowString) //@todo
    saveMemberAccess(viewdByMember)
  }

  // UnKnow user access not logged in user
  //@Transactional(readOnly = false)
  def logUnKnownProfileViewByObjectId(viewedByUnKnown: ViewedByUnKnown, ipAddress: String) = withTransaction(template) {
    var util = new ViewedByMemberUtil()
    viewedByUnKnown.viewedBy(ipAddress, util.getNowString)
    saveUnKnownAccess(viewedByUnKnown)
  }


  // fetch number of viewers

  // LoggedInUsers

  // UnKnownUsers


  //@todo not working, must implement a new metod on repository
  //@Transactional(readOnly = true)
  def findByViewByMemberAccess(objectId: String): Option[ViewedByMember] = withTransaction(template) {

    var ob: Option[ViewedByMember] = None

    var list = viewedByMemberRepository.findAll()


    // @todo
    /*
    for(v: models.ViewdByMember <- list) {
      if(v.objectId == objectId.toString) {
       ob = Some(v)
      }

    }
    */
    ob
  }


  //@Transactional(readOnly = false)
  def saveUserProfile(userProfile: models.UserProfile): models.UserProfile = withTransaction(template) {
    Logger.info("Saving user profile id: " + userProfile.userIdentity)
    Logger.info("Saving provider id: " + userProfile.providerIdentity)
    userProfileRepository.save(userProfile)
  }


  //@Transactional(readOnly = false)
  def addFavorites(theUser: models.UserProfile, friendsUserCredential: models.UserCredential): models.UserProfile = withTransaction(template) {

    if (friendsUserCredential != None && friendsUserCredential != null) {
      var fUserProfile: UserProfile = friendsUserCredential.profiles.asScala.head
      theUser.addFavoriteUserProfile(fUserProfile)
    }

    theUser
  }

  //@Transactional(readOnly = false)
  def viewedBy(theUser: models.UserProfile, name: String): models.UserProfile = withTransaction(template) {

    var memberAccess = theUser.getmemberVisited(): models.ViewedByMember
    var util = new ViewedByMemberUtil()
    memberAccess.viewedBy(name, util.getNowString)
    theUser.setViewedByMeber(memberAccess)

    theUser
  }

  //@Transactional(readOnly = false)
  def removeFavorites(theUser: models.UserProfile, friendsUserCredential: models.UserCredential): models.UserProfile = withTransaction(template) {

    if (friendsUserCredential != None && friendsUserCredential != null) {
      var fUserProfile: UserProfile = friendsUserCredential.profiles.asScala.head

      var jmfFriend = friendsUserCredential.objectId
      var profileLink: Option[TaggedFavoritesToUserProfile] = None


      var itter = theUser.getFavorites.iterator()
      while (itter.hasNext) {
        var tagProfile = itter.next()
        if (tagProfile.favoritesUserProfile.getOwner.objectId == jmfFriend) {
          profileLink = Some(tagProfile)
        }
      }

      theUser.removeFavoriteUserProfile(profileLink.get)
    }

    theUser
  }


  //@Transactional(readOnly = true)
  def isFavoritesToMe(theUser: models.UserProfile, friendsUserCredential: models.UserCredential): Boolean = withTransaction(template) {

    var isFriend: Boolean = false
    var profileLink: Option[TaggedFavoritesToUserProfile] = None

    if (friendsUserCredential != None && friendsUserCredential != null) {
      var fUserProfile: UserProfile = friendsUserCredential.profiles.asScala.head

      var jmfFriend = friendsUserCredential.objectId



      var itter = theUser.getFavorites.iterator()
      while (itter.hasNext) {
        var tagProfile = itter.next()
        if (tagProfile.favoritesUserProfile.getOwner.objectId.toString == jmfFriend.toString) {
          profileLink = Some(tagProfile)
        }
      }

    }
    var isFavoriteTo = profileLink match {
      case None => false
      case null => false
      case profileLink => true
    }

    isFavoriteTo
  }

  def updateUserProfileTags(theUser: UserProfile, tagsToAdd: List[TagWord]): UserProfile = withTransaction(template) {

    // Fetch all tags available to choose from
    val allTagWords: List[TagWord] = tagWordRepository.findByTagGroupName("profile").asScala.toList

    if (allTagWords != Nil) {
      theUser.removeAllTags()

      for (tag <- allTagWords) {
        // If the the user have tagged the choice, tag it!
        if(tagsToAdd.exists(t => t.objectId == tag.objectId)){
          theUser.tag(tag)
        }
      }

    }

    saveUserProfile(theUser)
  }

  //@Transactional(readOnly = false)
  def addRecipeToProfile(user: UserCredential, recipeToAdd: Recipe): UserProfile = withTransaction(template) {
    val userProfile = user.profiles.iterator().next()
    var modUserProfile = this.addRecipeToProfile(userProfile, recipeToAdd)
    modUserProfile = userProfileRepository.save(userProfile)
    modUserProfile
  }

  //@Transactional(readOnly = false)
  def addRecipeToProfile(userProfile: UserProfile, recipeToAdd: Recipe): UserProfile = withTransaction(template) {
    userProfile.addRecipe(recipeToAdd)
  }

  //@Transactional(readOnly = false)
  def addBlogPostsToProfile(userProfile: UserProfile, blogPostsToAdd: BlogPost): UserProfile = withTransaction(template) {
    userProfile.addBlogPosts(blogPostsToAdd)
  }

  //@Transactional(readOnly = false)
  def addEventToProfile(userProfile: UserProfile, eventToAdd: Event): UserProfile = withTransaction(template) {
    userProfile.addEvent(eventToAdd)
  }

  //@Transactional(readOnly = false)
  def addBlogPostsToProfile(user: UserCredential, blogPostsToAdd: BlogPost): UserProfile = withTransaction(template) {
    val userProfile = user.profiles.iterator().next()
    var modUserProfile = this.addBlogPostsToProfile(userProfile, blogPostsToAdd)
    modUserProfile = userProfileRepository.save(userProfile)
    modUserProfile
  }


  def findByprofileLinkName(profileName: String): Option[UserProfile] = withTransaction(template) {
    var returnObject: Option[UserProfile] = None
    if (profileName.nonEmpty) {
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


  def findByowner(userCred: UserCredential): Option[UserProfile] = withTransaction(template) {
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

  def findUserProfileByUserId(id: UserCredential): Option[UserProfile] = withTransaction(template) {
    //    var key : String = id.identityId.userId + "_" + id.identityId.providerId
    //    var up = UserProfileService.userProfileRepository.findByUserIdentityAndProviderIdentity(id.identityId.userId, id.identityId.providerId)

    var lista = userProfileRepository.findAll().iterator()

    while (lista.hasNext) {
      var v = lista.next()
      println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
      println("userId " + v.userIdentity + " provider id :" + v.providerIdentity)
      println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")

      if (v.userIdentity.equalsIgnoreCase(id.userId) && v.providerIdentity.equalsIgnoreCase(id.providerId)) {
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

  //@Transactional(readOnly = true)
  def getAllUserProfiles: List[models.UserProfile] = withTransaction(template) {
    val listOfUserProfiles: List[models.UserProfile] = IteratorUtil.asCollection(userProfileRepository.findAll()).asScala.toList
    listOfUserProfiles
  }

  //@Transactional(readOnly = false)
  def removeAllLocationTags(userProfile: UserProfile): UserProfile = withTransaction(template) {
    userProfile.removeLocation()
    userProfile
  }

  //@Transactional(readOnly = false)
  def removeAllProfileTags(userProfile: UserProfile): UserProfile = withTransaction(template) {
    userProfile.removeAllTags()
    userProfile
  }

  //@Transactional(readOnly = false)
  def addProfileTag(userProfile: UserProfile, tag: TagWord): UserProfile = withTransaction(template) {
    userProfile.tag(tag)
    userProfile
  }

  //@Transactional(readOnly = false)
  def addLocation(userProfile: UserProfile, county: County): UserProfile = withTransaction(template) {
    userProfile.locate(county)
    userProfile
  }

  //@Transactional(readOnly = false)
  def setAndRemoveMainImage(userProfile: UserProfile, newImage: ContentFile): UserProfile = withTransaction(template) {
    userProfile.setAndRemoveMainImage(newImage)
    userProfile
  }

  //@Transactional(readOnly = false)
  def setAndRemoveAvatarImage(userProfile: UserProfile, newImage: ContentFile): UserProfile = withTransaction(template) {
    userProfile.setAndRemoveAvatarImage(newImage)
    userProfile
  }

  //@Transactional(readOnly = false)
  def setAndRemoveViewByMember(userProfile: UserProfile, viewedByMember: models.ViewedByMember): UserProfile = withTransaction(template) {
    userProfile.setViewedByMeber(viewedByMember)
    userProfile
  }

  //@Transactional(readOnly = false)
  def setAndRemoveViewByUnKnown(userProfile: UserProfile, viewedByUnKnown: models.ViewedByUnKnown): UserProfile = withTransaction(template) {
    userProfile.setViewedByUnKnown(viewedByUnKnown)
    userProfile
  }

  //@Transactional(readOnly = true)
  def getUserProfile(userName: String) = withTransaction(template) {


    print("getUserProfile : " + userName)

    if (template == null) {
      println("template is null ********************************")
    } else {
      println("template is not null*****************************")
    }


    var userProfileDataIndex: Index[Node] = template.getIndex(classOf[models.UserProfile], "userName")
    //val node: Node = userProfileDataIndex.query("userName", userName).getSingle()

    if (userProfileDataIndex == null) {
      println("userProfileDataIndex is null ********************************")
    } else {
      println("userProfileDataIndex is not null*****************************")
    }



    var hits = userProfileDataIndex.query("userName", userName)
    var nods = hits.iterator()
    var id: Long = 0

    println("...........................................")


    if (nods.hasNext) {
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


  //@Transactional(readOnly = true)
  def getAllUserProfile: List[models.UserProfile] = withTransaction(template){
    userProfileRepository.findAll().asScala.toList
  }


  // Can return either:
  // Option[Page[UserProfile]]
  // Option[List[UserProfile]]
  def getUserProfilesFiltered(filterTag: Option[TagWord], filterCounty: Option[County], filterIsHost: Boolean, pageNo: Option[Integer] = None, nrPerPage: Int = 9) = withTransaction(template){

    var returnList: List[UserProfile] = Nil
    var returnPaged: Page[UserProfile] = null

    (filterTag, filterCounty, filterIsHost) match {
      case (Some(tw), Some(cnt), true) =>
        if (pageNo.isDefined) {
          returnPaged = userProfileRepository.findByTagWordIdAndCountyIdAndIsHost(tw.objectId.toString, cnt.objectId.toString, UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = userProfileRepository.findByTagWordIdAndCountyIdAndIsHost(tw.objectId.toString, cnt.objectId.toString, UserLevelScala.HOST.Constant).asScala.toList
        }
      case (Some(tw), Some(cnt), false) =>
        if (pageNo.isDefined) {
          returnPaged = userProfileRepository.findByTagWordIdAndCountyId(tw.objectId.toString, cnt.objectId.toString, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = userProfileRepository.findByTagWordIdAndCountyId(tw.objectId.toString, cnt.objectId.toString).asScala.toList
        }
      case (Some(tw), None, true) =>
        if (pageNo.isDefined) {
          returnPaged = userProfileRepository.findByTagWordIdAndIsHost(tw.objectId.toString, UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = userProfileRepository.findByTagWordIdAndIsHost(tw.objectId.toString, UserLevelScala.HOST.Constant).asScala.toList
        }
      case (Some(tw), None, false) =>
        if (pageNo.isDefined) {
          returnPaged = userProfileRepository.findByTagWordId(tw.objectId.toString, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = userProfileRepository.findByTagWordId(tw.objectId.toString).asScala.toList
        }
      case (None, Some(cnt), true) =>
        if (pageNo.isDefined) {
          returnPaged = userProfileRepository.findByCountyIdAndIsHost(cnt.objectId.toString, UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = userProfileRepository.findByCountyIdAndIsHost(cnt.objectId.toString, UserLevelScala.HOST.Constant).asScala.toList
        }
      case (None, Some(cnt), false) =>
        if (pageNo.isDefined) {
          returnPaged = userProfileRepository.findByCountyId(cnt.objectId.toString, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = userProfileRepository.findByCountyId(cnt.objectId.toString).asScala.toList
        }
      case (None, None, true) =>
        if (pageNo.isDefined) {
          returnPaged = userProfileRepository.findByIsHost(UserLevelScala.HOST.Constant, new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = userProfileRepository.findByIsHost(UserLevelScala.HOST.Constant).asScala.toList
        }
      case _ =>
        // Sorting, works but not with relations
        //val sorting = new Sort(new Sort.Order(Sort.Direction.ASC, "mainImage", Sort.NullHandling.NULLS_LAST))

        if (pageNo.isDefined) {
          returnPaged = userProfileRepository.findAllProfiles(new PageRequest(pageNo.get, nrPerPage))
        } else {
          returnList = userProfileRepository.findAllProfiles().asScala.toList
        }
    }

    // Return paged list, or normal list or just None
    if (returnList != Nil) {
      Some(returnList)
    } else if (returnPaged != null) {
      Some(returnPaged)
    } else {
      None
    }
  }


  //@Transactional(readOnly = true)
  def getUserWhoFavoritesUser(userProfile: models.UserProfile): List[models.UserProfile] = withTransaction(template){
    userProfileRepository.findFriendsToUser(userProfile.objectId).asScala.toList.filter(p => p.profileLinkName != null && p.profileLinkName.nonEmpty)
  }


}


