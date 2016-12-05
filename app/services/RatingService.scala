package services

import java.util.UUID
import javax.inject.Inject

import controllers.routes
import models.viewmodels.ReviewBox
import org.springframework.data.neo4j.support.Neo4jTemplate
import repositories._
import models.rating.{RatesRecipe, RatesUserCredential, ReviewData}
import models.{Recipe, UserCredential}
import org.springframework.data.domain.PageRequest
import traits.TransactionSupport

import scala.collection.JavaConverters._


class RatingService @Inject()(val template: Neo4jTemplate,
                              val userCredentialRepository: UserCredentialRepository,
                              val ratingUserCredentialRepository: RatingUserCredentialRepository,
                              val ratingRecipeRepository: RatingRecipeRepository) extends TransactionSupport {


  // RatingUserCredential
  def findUserRatingById(objectId: java.util.UUID): RatesUserCredential = withTransaction(template){
    ratingUserCredentialRepository.findByobjectId(objectId)
  }
  
  def findUserRatingByUserWhoIsRating(user: UserCredential): Option[List[ReviewData]] = withTransaction(template){
    ratingUserCredentialRepository.findByuserWhoIsRatingData(user.objectId).asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)//Some(listOfItems.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse))
    }
  }
  
  def findUserRatingByRatingValue(ratingValue: Int, filterModifier: String): Option[List[RatesUserCredential]] = withTransaction(template){
    ratingUserCredentialRepository.findByratingValue(ratingValue, filterModifier).asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse))
    }
  }
  
  def findUserRatingByWhoGotRated(user: UserCredential): Option[List[ReviewData]] = withTransaction(template){
    ratingUserCredentialRepository.findByuserRatesData(user.objectId).asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems) //Some(listOfItems.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse))
    }
  }
  
  def findUserRatingByUserWhoIsRatingAndUserRates(user: UserCredential, userRated: UserCredential): Option[List[RatesUserCredential]] = withTransaction(template){
    ratingUserCredentialRepository.findByuserWhoIsRatingAndUserRates(user.objectId.toString, userRated.objectId.toString).asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse))
    }
  }
  
  def findUserRatingAll(): Option[List[ReviewData]] = withTransaction(template){
    ratingUserCredentialRepository.findAllRatingsData().asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems) //Some(listOfItems.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse))
    }
  }

  def findUserRatingAllPaged(perPage: Integer, pageNo: Integer): Option[List[ReviewData]] = withTransaction(template){
    ratingUserCredentialRepository.findAllRatingsDataPaged(new PageRequest(pageNo, perPage)).asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }

  
  def getCountOfAll: Int = withTransaction(template){
    getCountOfAllMemberRatings + getCountOfAllRecipesRatings
  }

  
  def getCountOfAllRecipesRatings: Int = withTransaction(template){
    ratingRecipeRepository.getCountOfAll()
  }

  
  def getCountOfAllMemberRatings: Int = withTransaction(template){
    ratingUserCredentialRepository.getCountOfAll()
  }


  // RatingRecipe
  def findRecipeRatingById(objectId: java.util.UUID): RatesRecipe = withTransaction(template){
    ratingRecipeRepository.findByobjectId(objectId)
  }
  
  def findRecipeRatingByUserWhoIsRating(user: UserCredential): Option[List[ReviewData]] = withTransaction(template){
    ratingRecipeRepository.findByuserWhoIsRatingData(user.objectId).asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  
  def findRecipeRatingByRatingValue(ratingValue: Int, filterModifier: String): Option[List[RatesRecipe]] = withTransaction(template){
    ratingRecipeRepository.findByratingValue(ratingValue,filterModifier).asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  
  def findRecipeRatingByWhatGotRated(recipe: Recipe): Option[List[RatesRecipe]] = withTransaction(template){
    ratingRecipeRepository.findByuserRates(recipe.objectId).asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  
  def findRecipeRatingsByRecipeOwner(user: UserCredential): Option[List[ReviewData]] = withTransaction(template){
    ratingRecipeRepository.findRecipeRatingsByRecipeOwnerProfileData(user.profiles.iterator().next().objectId).asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems) //Some(listOfItems.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse))
    }
  }
  
  def findRecipeRatingByUserWhoIsRatingAndUserRates(user: UserCredential, userRated: Recipe): Option[List[RatesRecipe]] = withTransaction(template){
    ratingRecipeRepository.findByuserWhoIsRatingAndUserRates(user.objectId.toString, userRated.objectId.toString).asScala.toList match {
      case Nil =>
        None
      case listOfItems =>
        Some(listOfItems.sortBy(rating => rating.getLastModifiedDate)(Ordering[java.util.Date].reverse))
    }
  }



  // Recipe
  def hasUserRatedThisBefore(currentUser: UserCredential, hasRatedThisRecipe: Recipe): Option[RatesRecipe] = withTransaction(template){
    ratingRecipeRepository.hasRatedThisBefore(currentUserObjectId = currentUser.objectId.toString, hasRatedObjectId = hasRatedThisRecipe.objectId.toString) match {
      case null => None
      case rating => Some(rating)
    }
  }
  
  def doesUserTryToRateHimself(currentUser: UserCredential, recipeToBeRated: Recipe): Boolean = withTransaction(template){
    currentUser.objectId.toString.equalsIgnoreCase(recipeToBeRated.getOwnerProfile.getOwner.objectId.toString)
  }


  // UserCredential
  def hasUserRatedThisBefore(currentUser: UserCredential, hasRatedThisUser: UserCredential): Option[RatesUserCredential] = withTransaction(template){
    ratingUserCredentialRepository.hasRatedThisBefore(currentUserObjectId = currentUser.objectId.toString, hasRatedObjectId = hasRatedThisUser.objectId.toString) match {
      case null => None
      case rating => Some(rating)
    }
  }
  
  def doesUserTryToRateHimself(currentUser: UserCredential, userToBeRated: UserCredential): Boolean = withTransaction(template){
    userToBeRated.objectId.toString.equalsIgnoreCase(currentUser.objectId.toString)
  }


  def getUserReviewBoxesStartPage(takeTop: Int): Option[List[ReviewBox]] = withTransaction(template){
    this.findUserRatingAllPaged(takeTop,0) match {
      case None => None
      case Some(items) => buildUserReviewBoxes(items)
    }
  }

  
  def getMyUserReviews(user: UserCredential): Option[List[ReviewBox]] = withTransaction(template){
    this.findUserRatingByUserWhoIsRating(user) match {
      case None => None
      case Some(items) => buildUserReviewBoxes(items)
    }
  }

  
  def getUserReviewsAboutMe(user: UserCredential): Option[List[ReviewBox]] = withTransaction(template){
    this.findUserRatingByWhoGotRated(user) match {
      case None => None
      case Some(items) => buildUserReviewBoxes(items)
    }
  }

  
  def getMyUserReviewsAboutFood(user: UserCredential): Option[List[ReviewBox]] = withTransaction(template){
    this.findRecipeRatingByUserWhoIsRating(user) match {
      case None => None
      case Some(items) => buildRecipeReviewBoxes(items)
    }
  }

  
  def getUserReviewsAboutMyFood(user: UserCredential): Option[List[ReviewBox]] = withTransaction(template){
    this.findRecipeRatingsByRecipeOwner(user) match {
      case None => None
      case Some(items) => buildRecipeReviewBoxes(items)
    }
  }


  def buildUserReviewBoxes(list: List[ReviewData]): Option[List[ReviewBox]] = {
    Some{
      list.map { ratingItem =>
        ReviewBox(
          objectId = Some(UUID.fromString(ratingItem.getObjectId())),
          linkToProfile = controllers.routes.UserProfileController.viewProfileByName(ratingItem.getUserWhoIsRatingProfileLinkName()),
          firstName =  ratingItem.getUserWhoIsRatingProfileLinkName(),
          rankedName =  ratingItem.getRatedProfileLinkName(),
          linkToRatedItem = controllers.routes.UserProfileController.viewProfileByName(ratingItem.getLinkToRatedItem()),
          reviewText = ratingItem.getReviewText() match {
            case "" | null => None
            case content => Some(content)
          },
          ratedDate = ratingItem.getLastModifiedDate(),
          userImage = ratingItem.getUserWhoIsRatingAvatarImage().asScala.toList match {
            case Nil | null => None
            case images => Some(routes.ImageController.userThumb(images.head).url)
          },
          rating = ratingItem.getRatingValue())
          }
        }
  }


  def buildRecipeReviewBoxes(list: List[ReviewData]): Option[List[ReviewBox]] = {
    Some{
      list.map { ratingItem =>
        ReviewBox(
          objectId = Some(UUID.fromString(ratingItem.getObjectId())),
          linkToProfile = controllers.routes.UserProfileController.viewProfileByName(ratingItem.getUserWhoIsRatingProfileLinkName()),
          firstName =  ratingItem.getUserWhoIsRatingProfileLinkName(),
          rankedName =  ratingItem.getNameOfRatedItem(),
          linkToRatedItem = controllers.routes.RecipePageController.viewRecipeByNameAndProfile(ratingItem.getRatedProfileLinkName(),ratingItem.getLinkToRatedItem()),
          reviewText = ratingItem.getReviewText() match {
            case "" | null => None
            case content => Some(content)
          },
          ratedDate = ratingItem.getLastModifiedDate(),
          userImage = ratingItem.getUserWhoIsRatingAvatarImage().asScala.toList match {
            case Nil | null => None
            case images => Some(routes.ImageController.userThumb(images.head).url)
          },
          rating = ratingItem.getRatingValue())
      }
    }
  }

  /*

  def buildRecipeReviewBoxes(list: List[RatesRecipe]): Option[List[ReviewBox]] = withTransaction(template){
    Some{ list.filter(r => !r.getUserWhoIsRating.profiles.isEmpty && r.getUserWhoIsRating.profiles.asScala.head.profileLinkName.nonEmpty).map { ratingItem: RatesRecipe =>
      val userWhoIsRatingProfile = ratingItem.getUserWhoIsRating.profiles.asScala.head
      val itemRatedProfile = ratingItem.getUserRates.getOwnerProfile
      ReviewBox(
        objectId = Some(ratingItem.objectId),
        linkToProfile = controllers.routes.UserProfileController.viewProfileByName(userWhoIsRatingProfile.profileLinkName),
        firstName = ratingItem.getUserWhoIsRating.profiles.iterator().next().profileLinkName,
        //firstName = ratingItem.getUserWhoIsRating.firstName,
        //rankedName = ratingItem.getUserRates.getOwnerProfile.profileLinkName,
        rankedName = ratingItem.getUserRates.getName,
        linkToRatedItem = controllers.routes.RecipePageController.viewRecipeByNameAndProfile(itemRatedProfile.profileLinkName,ratingItem.getUserRates.getLink),
        reviewText = ratingItem.ratingComment match {
          case "" | null => None
          case content => Some(content)
        },
        ratedDate = ratingItem.getLastModifiedDate,
        userImage = userWhoIsRatingProfile.getAvatarImage match {
          case null => None
          case image => Some(routes.ImageController.userThumb(image.getStoreId).url)
        },
        rating = ratingItem.ratingValue)
    }
    }
  }
*/

//  def hasUserRatedThisUser(currentUser: UUID, hasRatedThisUser: UUID): Option[RatesUserCredential] = {
//    this.hasUserRatedThisUser(userCredentialRepository.findByobjectId(currentUser), userCredentialRepository.findByobjectId(hasRatedThisUser))
//  }
//  def hasUserRatedThisUser(currentUser: UserCredential, hasRatedThisUser: UUID): Option[RatesUserCredential] = {
//    this.hasUserRatedThisUser(currentUser, userCredentialRepository.findByobjectId(hasRatedThisUser))
//  }
//  def hasUserRatedThisUser(currentUser: UUID, hasRatedThisUser: UserCredential): Option[RatesUserCredential] = {
//    this.hasUserRatedThisUser(userCredentialRepository.findByobjectId(currentUser), hasRatedThisUser)
//  }




  def rateUser(userRating: UserCredential, userRates: UserCredential, ratingValue: Int , ratingComment: String , userRaterIP: String ): RatesUserCredential = withTransaction(template){
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


  def rateRecipe(userRating: UserCredential, userRates: Recipe, ratingValue: Int , ratingComment: String , userRaterIP: String ): RatesRecipe = withTransaction(template){
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





  def deleteUserRatingById(objectId: java.util.UUID): Boolean = withTransaction(template){
    this.findUserRatingById(objectId) match {
      case null =>
        false
      case item =>
        ratingUserCredentialRepository.delete(item)
        true
    }
  }


  def deleteRecipeRatingById(objectId: java.util.UUID): Boolean = withTransaction(template){
    this.findRecipeRatingById(objectId) match {
      case null =>
        false
      case item =>
        ratingRecipeRepository.delete(item)
        true
    }
  }


  def saveUserRate(newItem: RatesUserCredential): RatesUserCredential = withTransaction(template){
    val newResult = ratingUserCredentialRepository.save(newItem)
    newResult
  }


  def saveRecipeRate(newItem: RatesRecipe): RatesRecipe = withTransaction(template){
    val newResult = ratingRecipeRepository.save(newItem)
    newResult
  }



  // UserRating
  
  def findUserByRatingUser(user: UserCredential): Option[List[UserCredential]] = withTransaction(template){
    userCredentialRepository.findByratingsUserRates(user).asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  
  def findUserByRatingValue(ratingValue: Int): Option[List[UserCredential]] = withTransaction(template){
    userCredentialRepository.findByratingsRatingValue(ratingValue).asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  
  def findUserByUserWhoIsRating(user: UserCredential): Option[List[UserCredential]] = withTransaction(template){
    userCredentialRepository.findByratingsUserWhoIsRating(user).asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }



}
