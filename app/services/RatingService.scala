package services

import java.util.UUID

import controllers.routes
import models.viewmodels.ReviewBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories._
import models.rating.{RatesRecipe, RatesUserCredential}
import models.{Recipe, UserCredential}
import models.modelconstants.RelationshipTypesScala
import utils.Helpers
import scala.collection.JavaConverters._
import scala.List

@Service
class RatingService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userCredentialRepository: UserCredentialRepository = _

  @Autowired
  private var ratingUserCredentialRepository: RatingUserCredentialRepository = _

  @Autowired
  private var ratingRecipeRepository: RatingRecipeRepository = _


  // RatingUserCredential
  @Transactional(readOnly = true)
  def findUserRatingById(objectId: java.util.UUID): RatesUserCredential = {
    ratingUserCredentialRepository.findByobjectId(objectId)
  }
  @Transactional(readOnly = true)
  def findUserRatingByUserWhoIsRating(user: UserCredential): Option[List[RatesUserCredential]] = {
    ratingUserCredentialRepository.findByuserWhoIsRating(user.objectId).iterator.asScala.toList.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse) match {
      case Nil =>
        None
      case listOfItems =>
        Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserRatingByRatingValue(ratingValue: Int, filterModifier: String): Option[List[RatesUserCredential]] = {
    ratingUserCredentialRepository.findByratingValue(ratingValue, filterModifier).iterator.asScala.toList.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse) match {
      case Nil =>
        None
      case listOfItems =>
        Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserRatingByWhoGotRated(user: UserCredential): Option[List[RatesUserCredential]] = {
    ratingUserCredentialRepository.findByuserRates(user.objectId).iterator.asScala.toList.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse) match {
      case Nil =>
        None
      case listOfItems =>
        Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserRatingByUserWhoIsRatingAndUserRates(user: UserCredential, userRated: UserCredential): Option[List[RatesUserCredential]] = {
    ratingUserCredentialRepository.findByuserWhoIsRatingAndUserRates(user.objectId.toString, userRated.objectId.toString).iterator.asScala.toList.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse) match {
      case Nil =>
        None
      case listOfItems =>
        Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserRatingAll(): Option[List[RatesUserCredential]] = {
    ratingUserCredentialRepository.findAllRatings.asScala.toList.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse) match {
      case Nil =>
        None
      case listOfItems =>
        Some(listOfItems)
    }
  }



  // RatingRecipe
  @Transactional(readOnly = true)
  def findRecipeRatingById(objectId: java.util.UUID): RatesRecipe = {
    ratingRecipeRepository.findByobjectId(objectId)
  }
  @Transactional(readOnly = true)
  def findRecipeRatingByUserWhoIsRating(user: UserCredential): Option[List[RatesRecipe]] = {
    ratingRecipeRepository.findByuserWhoIsRating(user.objectId).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findRecipeRatingByRatingValue(ratingValue: Int, filterModifier: String): Option[List[RatesRecipe]] = {
    ratingRecipeRepository.findByratingValue(ratingValue,filterModifier).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findRecipeRatingByWhatGotRated(recipe: Recipe): Option[List[RatesRecipe]] = {
    ratingRecipeRepository.findByuserRates(recipe.objectId).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findRecipeRatingsByRecipeOwner(user: UserCredential): Option[List[RatesRecipe]] = {
    ratingRecipeRepository.findRecipeRatingsByRecipeOwnerProfile(user.profiles.iterator().next().objectId).iterator.asScala.toList.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse) match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findRecipeRatingByUserWhoIsRatingAndUserRates(user: UserCredential, userRated: Recipe): Option[List[RatesRecipe]] = {
    ratingRecipeRepository.findByuserWhoIsRatingAndUserRates(user.objectId.toString, userRated.objectId.toString).iterator.asScala.toList.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse) match {
      case Nil =>
        None
      case listOfItems =>
        Some(listOfItems)
    }
  }



  // Recipe
  @Transactional(readOnly = true)
  def hasUserRatedThisBefore(currentUser: UserCredential, hasRatedThisRecipe: Recipe): Option[RatesRecipe] = {
    // By using graphId we don't need to load all the relationships
    currentUser.getHasRatedRecipes.asScala.find(rel => rel.userRates.graphId == hasRatedThisRecipe.graphId)
  }
  @Transactional(readOnly = true)
  def doesUserTryToRateHimself(currentUser: UserCredential, recipeToBeRated: Recipe): Boolean = {
    currentUser.objectId.toString.equalsIgnoreCase(recipeToBeRated.getOwnerProfile.getOwner.objectId.toString)
  }


  // UserCredential
  @Transactional(readOnly = true)
  def hasUserRatedThisBefore(currentUser: UserCredential, hasRatedThisUser: UserCredential): Option[RatesUserCredential] = {
    // By using graphId we don't need to load all the relationships
    currentUser.getHasRatedUsers.asScala.find(rel => rel.userRates.graphId == hasRatedThisUser.graphId)
  }
  @Transactional(readOnly = true)
  def doesUserTryToRateHimself(currentUser: UserCredential, userToBeRated: UserCredential): Boolean = {
    userToBeRated.objectId.toString.equalsIgnoreCase(currentUser.objectId.toString)
  }


  @Transactional(readOnly = true)
  def getUserReviewBoxesStartPage(takeTop: Int): Option[List[ReviewBox]] = {
    this.findUserRatingAll() match {
      case None =>
        None
      case Some(items) =>
        buildUserReviewBoxes(items.take(takeTop))
    }
  }

  @Transactional(readOnly = true)
  def getMyUserReviews(user: UserCredential): Option[List[ReviewBox]] = {
    this.findUserRatingByUserWhoIsRating(user) match {
      case None =>
        None
      case Some(items) =>
        buildUserReviewBoxes(items)
    }
  }

  @Transactional(readOnly = true)
  def getUserReviewsAboutMe(user: UserCredential): Option[List[ReviewBox]] = {
    this.findUserRatingByWhoGotRated(user) match {
      case None =>
        None
      case Some(items) =>
        buildUserReviewBoxes(items)
    }
  }

  @Transactional(readOnly = true)
  def getMyUserReviewsAboutFood(user: UserCredential): Option[List[ReviewBox]] = {
    this.findRecipeRatingByUserWhoIsRating(user) match {
      case None =>
        None
      case Some(items) =>
        buildRecipeReviewBoxes(items)
    }
  }

  @Transactional(readOnly = true)
  def getUserReviewsAboutMyFood(user: UserCredential): Option[List[ReviewBox]] = {
    this.findRecipeRatingsByRecipeOwner(user) match {
      case None =>
        None
      case Some(items) =>
        buildRecipeReviewBoxes(items)
    }
  }



  @Transactional(readOnly = true)
  def buildUserReviewBoxes(list: List[RatesUserCredential]): Option[List[ReviewBox]] = {
    Some{ list.filter(r => !r.getUserWhoIsRating.profiles.isEmpty && r.getUserWhoIsRating.profiles.asScala.head.profileLinkName.nonEmpty).map { ratingItem: RatesUserCredential =>
      val userWhoIsRatingProfile = ratingItem.getUserWhoIsRating.profiles.asScala.head
      val itemRatedProfile = ratingItem.getUserRates.profiles.asScala.head
        ReviewBox(
          objectId = Some(ratingItem.objectId),
          linkToProfile = controllers.routes.UserProfileController.viewProfileByName(userWhoIsRatingProfile.profileLinkName),
          firstName = ratingItem.getUserWhoIsRating.firstName,
          rankedName = itemRatedProfile.profileLinkName + "&nbsp;(" + ratingItem.getUserRates.firstName + ")",
          linkToRatedItem = controllers.routes.UserProfileController.viewProfileByName(itemRatedProfile.profileLinkName),
          reviewText = ratingItem.ratingComment match {
            case "" | null => None
            case content => Some(content)
          },
          ratedDate = ratingItem.getLastModifiedDate,
          userImage = userWhoIsRatingProfile.getAvatarImage match {
            case null => None
            case image => Some(image.getTransformByName("thumbnail").getUrl)
          },
          rating = ratingItem.ratingValue)
          }
        }
  }

  @Transactional(readOnly = true)
  def buildRecipeReviewBoxes(list: List[RatesRecipe]): Option[List[ReviewBox]] = {
    Some{ list.filter(r => !r.getUserWhoIsRating.profiles.isEmpty && r.getUserWhoIsRating.profiles.asScala.head.profileLinkName.nonEmpty).map { ratingItem: RatesRecipe =>
      val userWhoIsRatingProfile = ratingItem.getUserWhoIsRating.profiles.asScala.head
      val itemRatedProfile = ratingItem.getUserRates.getOwnerProfile
      ReviewBox(
        objectId = Some(ratingItem.objectId),
        linkToProfile = controllers.routes.UserProfileController.viewProfileByName(userWhoIsRatingProfile.profileLinkName),
        firstName = ratingItem.getUserWhoIsRating.firstName,
        rankedName = ratingItem.getUserRates.getName,
        linkToRatedItem = controllers.routes.RecipePageController.viewRecipeByNameAndProfile(itemRatedProfile.profileLinkName,ratingItem.getUserRates.getLink),
        reviewText = ratingItem.ratingComment match {
          case "" | null => None
          case content => Some(content)
        },
        ratedDate = ratingItem.getLastModifiedDate,
        userImage = userWhoIsRatingProfile.getAvatarImage match {
          case null => None
          case image => Some(image.getTransformByName("thumbnail").getUrl)
        },
        rating = ratingItem.ratingValue)
    }
    }
  }


//  def hasUserRatedThisUser(currentUser: UUID, hasRatedThisUser: UUID): Option[RatesUserCredential] = {
//    this.hasUserRatedThisUser(userCredentialRepository.findByobjectId(currentUser), userCredentialRepository.findByobjectId(hasRatedThisUser))
//  }
//  def hasUserRatedThisUser(currentUser: UserCredential, hasRatedThisUser: UUID): Option[RatesUserCredential] = {
//    this.hasUserRatedThisUser(currentUser, userCredentialRepository.findByobjectId(hasRatedThisUser))
//  }
//  def hasUserRatedThisUser(currentUser: UUID, hasRatedThisUser: UserCredential): Option[RatesUserCredential] = {
//    this.hasUserRatedThisUser(userCredentialRepository.findByobjectId(currentUser), hasRatedThisUser)
//  }



  @Transactional(readOnly = false)
  def rateUser(userRating: UserCredential, userRates: UserCredential, ratingValue: Int , ratingComment: String , userRaterIP: String ): RatesUserCredential = {
    //val item: RatesUserCredential = template.createRelationshipBetween(userRating, userRates, classOf[RatesUserCredential], RelationshipTypesScala.RATED_USER.Constant, false)

    // Is there already a rating? Don't allow duplicates
    val item = this.findUserRatingByUserWhoIsRatingAndUserRates(userRating, userRates) match {
      case None =>
        new RatesUserCredential
      case Some(ratings) =>
        ratings.head
    }

    item.rate(userRating, userRates, ratingValue, ratingComment, userRaterIP)
    this.saveUserRate(item)
  }

  @Transactional(readOnly = false)
  def rateRecipe(userRating: UserCredential, userRates: Recipe, ratingValue: Int , ratingComment: String , userRaterIP: String ): RatesRecipe = {
    //val item: RatesRecipe = template.createRelationshipBetween(userRating, userRates, classOf[RatesRecipe], RelationshipTypesScala.RATED_RECIPE.Constant, false)

    // Is there already a rating? Don't allow duplicates
    val item = this.findRecipeRatingByUserWhoIsRatingAndUserRates(userRating, userRates) match {
      case None =>
        new RatesRecipe
      case Some(ratings) =>
        ratings.head
    }

    item.rate(userRating, userRates, ratingValue, ratingComment, userRaterIP)
    this.saveRecipeRate(item)
  }




  @Transactional(readOnly = false)
  def deleteUserRatingById(objectId: java.util.UUID): Boolean = {
    this.findUserRatingById(objectId) match {
      case null =>
        false
      case item =>
        ratingUserCredentialRepository.delete(item)
        true
    }
  }

  @Transactional(readOnly = false)
  def deleteRecipeRatingById(objectId: java.util.UUID): Boolean = {
    this.findRecipeRatingById(objectId) match {
      case null =>
        false
      case item =>
        ratingRecipeRepository.delete(item)
        true
    }
  }

  @Transactional(readOnly = false)
  def saveUserRate(newItem: RatesUserCredential): RatesUserCredential = {
    val newResult = ratingUserCredentialRepository.save(newItem)
    newResult
  }

  @Transactional(readOnly = false)
  def saveRecipeRate(newItem: RatesRecipe): RatesRecipe = {
    val newResult = ratingRecipeRepository.save(newItem)
    newResult
  }



  // UserRating
  @Transactional(readOnly = true)
  def findUserByRatingUser(user: UserCredential): Option[List[UserCredential]] = {
    userCredentialRepository.findByratingsUserRates(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserByRatingValue(ratingValue: Int): Option[List[UserCredential]] = {
    userCredentialRepository.findByratingsRatingValue(ratingValue).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserByUserWhoIsRating(user: UserCredential): Option[List[UserCredential]] = {
    userCredentialRepository.findByratingsUserWhoIsRating(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }



  // RecipeRating





}
