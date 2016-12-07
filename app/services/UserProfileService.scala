package services

import javax.inject.Inject

import models.files.ContentFile
import models.modelconstants.UserLevelScala
import models._
import org.neo4j.graphdb._
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import play.api.Logger
import repositories.{TagWordRepository, UserProfileRepository, ViewedByMemberRepository, ViewedByUnKnownRepository}
import traits.TransactionSupport

import scala.collection.JavaConverters._
import scala.language.implicitConversions
import org.springframework.transaction.annotation.Transactional
import org.neo4j.graphdb.index.Index
import models.location.County
import models.profile.{FavoriteData, TagWord, TaggedFavoritesToUserProfile}

import scala.collection.JavaConverters._
import customUtils.ViewedByMemberUtil
import models.formdata.UserProfileOptionsForm

class UserProfileService @Inject()(val template: Neo4jTemplate,
                                   val userProfileRepository: UserProfileRepository,
                                   val viewedByMemberRepository: ViewedByMemberRepository,
                                   val tagWordRepository: TagWordRepository,
                                   val viewedByUnKnownRepository: ViewedByUnKnownRepository) extends TransactionSupport {

  // save UnKnow user access to page
  def saveUnKnownAccess(view: models.ViewedByUnKnown): models.ViewedByUnKnown = withTransaction(template) {
    viewedByUnKnownRepository.save(view)
  }

  // save member access to page
  def saveMemberAccess(view: models.ViewedByMember): models.ViewedByMember = withTransaction(template) {
    viewedByMemberRepository.save(view)
  }


  // logged in user accessing profile page
  def logProfileViewByObjectId(viewdByMember: ViewedByMember, viewerObjectId: String, pageOwnerObjectId: String) = withTransaction(template) {
    val util = new ViewedByMemberUtil()
    viewdByMember.viewedBy(viewerObjectId, util.getNowString) //@todo
    saveMemberAccess(viewdByMember)
  }

  // UnKnow user access not logged in user
  def logUnKnownProfileViewByObjectId(viewedByUnKnown: ViewedByUnKnown, ipAddress: String) = withTransaction(template) {
    var util = new ViewedByMemberUtil()
    viewedByUnKnown.viewedBy(ipAddress, util.getNowString)
    saveUnKnownAccess(viewedByUnKnown)
  }


  // Fetch number of viewers
  def getViewedByMember(userP: UserProfile): Option[ViewedByMember] = withTransaction(template) {
    if(userP.getmemberVisited() != null){
      Some(template.fetch(userP.getmemberVisited()))
    }else {
      None
    }
  }

  // Fetch number of viewers
  def getViewedByUnKnown(userP: UserProfile): Option[ViewedByUnKnown] = withTransaction(template) {
    if(userP.getUnKnownVisited != null){
      Some(template.fetch(userP.getUnKnownVisited))
    }else {
      None
    }
  }

  def countViewsByUnknown(userP: UserProfile): Int = {
    viewedByUnKnownRepository.countViewsByUnknown(userP.objectId.toString)
  }

  def countViewsByMember(userP: UserProfile): Int = {
    viewedByMemberRepository.countViewsByMember(userP.objectId.toString)
  }

  def saveUserProfile(userProfile: models.UserProfile): models.UserProfile = withTransaction(template) {
    Logger.info("Saving user profile id: " + userProfile.userIdentity)
    Logger.info("Saving provider id: " + userProfile.providerIdentity)
    userProfileRepository.save(userProfile)
  }



  def addFavorites(theUser: models.UserProfile, friendsUserCredential: models.UserCredential): models.UserProfile = withTransaction(template) {
    if (friendsUserCredential != null) {
      val fUserProfile: UserProfile = friendsUserCredential.getUserProfile
      theUser.addFavoriteUserProfile(fUserProfile)
    }
    theUser
  }


  def removeFavorites(userProfile: UserProfile, friendsUserProfile: UserProfile): UserProfile = withTransaction(template) {

    if (userProfile != null && friendsUserProfile != null) {
      val favRelation = userProfileRepository.findFavRelationToMe(userProfile.objectId.toString, friendsUserProfile.objectId.toString) match {
        case null => None
        case relation => Some(relation)
      }
      if(favRelation.isDefined) {
        userProfile.removeFavoriteUserProfile(favRelation.get)
      }
    }

    userProfile
  }


  def isFavouriteToMe(theUser: UserProfile, friendsUserCredential: UserCredential): Boolean = withTransaction(template) {
    userProfileRepository.isFavouriteToMe(theUser.objectId.toString, friendsUserCredential.objectId.toString) match {
      case 0 => false
      case _ => true
    }
    /*
    var profileLink: Option[TaggedFavoritesToUserProfile] = None

    if (friendsUserCredential != null) {
      val jmfFriend = friendsUserCredential.objectId
      val itter = template.fetch(theUser.getFavorites).iterator()

      while (itter.hasNext) {
        val tagProfile = itter.next()
        if (tagProfile.favoritesUserProfile.getOwner.objectId.toString == jmfFriend.toString) {
          profileLink = Some(tagProfile)
        }
      }

    }

    val isFavoriteTo = profileLink match {
      case None => false
      case null => false
      case someValue => true
    }

    isFavoriteTo
    */
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

  def addPaymentOptionsToProfile(userProfile: UserProfile, paymentForm: UserProfileOptionsForm): UserProfile = withTransaction(template) {
    userProfile.payBankCard = paymentForm.payBankCard
    userProfile.payCash = paymentForm.payCash
    userProfile.payIZettle = paymentForm.payIZettle
    userProfile.paySwish = paymentForm.paySwish
    val modUserProfile = userProfileRepository.save(userProfile)
    modUserProfile
  }


  def addRecipeToProfile(user: UserCredential, recipeToAdd: Recipe): UserProfile = withTransaction(template) {
    val userProfile = user.profiles.iterator().next()
    var modUserProfile = this.addRecipeToProfile(userProfile, recipeToAdd)
    modUserProfile = userProfileRepository.save(userProfile)
    modUserProfile
  }


  def addRecipeToProfile(userProfile: UserProfile, recipeToAdd: Recipe): UserProfile = withTransaction(template) {
    userProfile.addRecipe(recipeToAdd)
  }


  def addBlogPostsToProfile(userProfile: UserProfile, blogPostsToAdd: BlogPost): UserProfile = withTransaction(template) {
    userProfile.addBlogPosts(blogPostsToAdd)
  }


  def addEventToProfile(userProfile: UserProfile, eventToAdd: Event): UserProfile = withTransaction(template) {
    val modUserProfile = userProfile.addEvent(eventToAdd)
    userProfileRepository.save(modUserProfile)
  }

  def addUserAsHostIfNotAlready(userProfile: UserProfile): UserProfile = {
    if (!userProfile.getRole.contains(UserLevelScala.HOST.Constant)) {
      userProfile.getRole.add(UserLevelScala.HOST.Constant)
    }
    userProfile
  }

  def removeUserAsHost(userProfile: UserProfile): UserProfile = {
    if (userProfile.getRole.contains(UserLevelScala.HOST.Constant)) {
      userProfile.getRole.remove(UserLevelScala.HOST.Constant)
    }
    userProfile
  }

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
          Some(profile)
      }
    }
    returnObject
  }


  def findByOwner(userCred: UserCredential): Option[UserProfile] = withTransaction(template) {
    userProfileRepository.findByowner(userCred) match {
      case null => None
      case profile =>
        Some(profile)
    }
  }

  def getAllUserProfiles: List[models.UserProfile] = withTransaction(template) {
    val listOfUserProfiles: List[models.UserProfile] = IteratorUtil.asCollection(userProfileRepository.findAll()).asScala.toList
    listOfUserProfiles
  }


  def removeAllLocationTags(userProfile: UserProfile): UserProfile = withTransaction(template) {
    userProfile.removeLocation()
    userProfile
  }


  def removeAllProfileTags(userProfile: UserProfile): UserProfile = withTransaction(template) {
    userProfile.removeAllTags()
    userProfile
  }


  def addProfileTag(userProfile: UserProfile, tag: TagWord): UserProfile = withTransaction(template) {
    userProfile.tag(tag)
    userProfile
  }


  def addLocation(userProfile: UserProfile, county: County): UserProfile = withTransaction(template) {
    userProfile.locate(county)
    userProfile
  }


  def setAndRemoveMainImage(userProfile: UserProfile, newImage: ContentFile): UserProfile = withTransaction(template) {
    userProfile.setAndRemoveMainImage(newImage)
    userProfile
  }


  def setAndRemoveAvatarImage(userProfile: UserProfile, newImage: ContentFile): UserProfile = withTransaction(template) {
    userProfile.setAndRemoveAvatarImage(newImage)
    userProfile
  }


  def setAndRemoveViewByMember(userProfile: UserProfile, viewedByMember: models.ViewedByMember): UserProfile = withTransaction(template) {
    userProfile.setViewedByMeber(viewedByMember)
    userProfile
  }


  def setAndRemoveViewByUnKnown(userProfile: UserProfile, viewedByUnKnown: models.ViewedByUnKnown): UserProfile = withTransaction(template) {
    userProfile.setViewedByUnKnown(viewedByUnKnown)
    userProfile
  }


  def getAllUserProfile: List[models.UserProfile] = withTransaction(template){
    userProfileRepository.findAll().asScala.toList
  }


  // Can return either:
  // Option[Page[UserProfile]]
  // Option[List[UserProfile]]
  def getUserProfilesFiltered(filterTag: Option[TagWord], filterCounty: Option[County], filterIsHost: Boolean, pageNo: Option[Integer] = None, nrPerPage: Int = 9): Either[Option[List[UserProfile]],Option[Page[UserProfile]]] = withTransaction(template){

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

    if (returnList != Nil) {
      Left(Some(returnList))
    } else {
      Right(Option(returnPaged))
    }

  }

  def getMyFavorites(userProfile: UserProfile): Option[List[FavoriteData]] = withTransaction(template){
    userProfileRepository.findMyFriends(userProfile.objectId.toString).asScala.toList match {
      case Nil => None
      case items => Some(items)
    }
  }

  def getUserWhoFavoritesUser(userProfile: UserProfile): Option[List[FavoriteData]] = withTransaction(template){
    userProfileRepository.findFriendsToUser(userProfile.objectId.toString).asScala.toList match {
      case Nil => None
      case items => Some(items)
    }
  }


}


