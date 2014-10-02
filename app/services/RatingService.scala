package services

import java.util.UUID

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories._
import models.rating.{RatesRecipe, RatesUserCredential}
import models.{Recipe, UserCredential}
import models.modelconstants.RelationshipTypesScala
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
    ratingUserCredentialRepository.findByuserWhoIsRating(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserRatingByRatingValue(ratingValue: Int): Option[List[RatesUserCredential]] = {
    ratingUserCredentialRepository.findByratingValue(ratingValue).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserRatingByWhoGotRated(user: UserCredential): Option[List[RatesUserCredential]] = {
    ratingUserCredentialRepository.findByuserRates(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  // RatingRecipe
  @Transactional(readOnly = true)
  def findRecipeRatingById(objectId: java.util.UUID): RatesRecipe = {
    ratingRecipeRepository.findByobjectId(objectId)
  }
  @Transactional(readOnly = true)
  def findRecipeRatingByUserWhoIsRating(user: UserCredential): Option[List[RatesRecipe]] = {
    ratingRecipeRepository.findByuserWhoIsRating(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findRecipeRatingByRatingValue(ratingValue: Int): Option[List[RatesRecipe]] = {
    ratingRecipeRepository.findByratingValue(ratingValue).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findRecipeRatingByWhatGotRated(recipe: Recipe): Option[List[RatesRecipe]] = {
    ratingRecipeRepository.findByuserRates(recipe).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
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
    val item: RatesUserCredential = template.createRelationshipBetween(userRating, userRates, classOf[RatesUserCredential], RelationshipTypesScala.RATED_USER.Constant, false)
    item.rate(ratingValue, ratingComment, userRaterIP)
    this.saveUserRate(item)
  }

  @Transactional(readOnly = false)
  def rateRecipe(userRating: UserCredential, userRates: Recipe, ratingValue: Int , ratingComment: String , userRaterIP: String ): RatesRecipe = {
    val item: RatesRecipe = template.createRelationshipBetween(userRating, userRates, classOf[RatesRecipe], RelationshipTypesScala.RATED_RECIPE.Constant, false)
    item.rate(ratingValue, ratingComment, userRaterIP)
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
