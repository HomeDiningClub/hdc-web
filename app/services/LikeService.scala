package services

import models.like.{UserCredentialLikeRecipe, UserCredentialLikeUserCredential}
import models.modelconstants.RelationshipTypesScala
import models.rating.{RatesRecipe, RatesUserCredential}
import models.{Recipe, UserCredential}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories._

import scala.collection.JavaConverters._

@Service
class LikeService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userCredentialRepository: UserCredentialRepository = _

  @Autowired
  private var likeUserCredentialRepository: LikeUserCredentialRepository = _

  @Autowired
  private var likeRecipeRepository: LikeRecipeRepository = _


  // LikeUserCredential
  @Transactional(readOnly = true)
  def findUserCredentialLikeUserCredentialById(objectId: java.util.UUID): UserCredentialLikeUserCredential = {
    likeUserCredentialRepository.findByobjectId(objectId)
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeUserCredentialByUserWhoIsLiking(user: UserCredential): Option[List[UserCredentialLikeUserCredential]] = {
    likeUserCredentialRepository.findByuserWhoLikes(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeUserCredentialByValue(likeValue: Boolean): Option[List[UserCredentialLikeUserCredential]] = {
    likeUserCredentialRepository.findBylikes(likeValue).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeUserCredentialByWhoGotLiked(user: UserCredential): Option[List[UserCredentialLikeUserCredential]] = {
    likeUserCredentialRepository.findByuserLikes(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  // LikeRecipe
  @Transactional(readOnly = true)
  def findUserCredentialLikeRecipeById(objectId: java.util.UUID): UserCredentialLikeRecipe = {
    likeRecipeRepository.findByobjectId(objectId)
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeRecipeByUserWhoIsLiking(user: UserCredential): Option[List[UserCredentialLikeRecipe]] = {
    likeRecipeRepository.findByuserWhoLikes(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeRecipeByValue(likeValue: Boolean): Option[List[UserCredentialLikeRecipe]] = {
    likeRecipeRepository.findBylikes(likeValue).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeRecipeByWhoGotLiked(recipe: Recipe): Option[List[UserCredentialLikeRecipe]] = {
    likeRecipeRepository.findByuserLikes(recipe).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }



  // Recipe
  @Transactional(readOnly = true)
  def hasUserLikedThisBefore(currentUser: UserCredential, hasLikedThis: Recipe): Option[UserCredentialLikeRecipe] = {
    // By using graphId we don't need to load all the relationships
    currentUser.getHasLikedRecipes.asScala.find(rel => rel.userLikes.graphId == hasLikedThis.graphId)
  }

  // UserCredential
  @Transactional(readOnly = true)
  def hasUserLikedThisBefore(currentUser: UserCredential, hasLikedThis: UserCredential): Option[UserCredentialLikeUserCredential] = {
    // By using graphId we don't need to load all the relationships
    currentUser.getHasLikedUsers.asScala.find(rel => rel.userLikes.graphId == hasLikedThis.graphId)
  }


  @Transactional(readOnly = false)
  def likeUser(userLiking: UserCredential, userLikes: UserCredential, likeValue: Boolean, userLikeIP: String): UserCredentialLikeUserCredential = {
    val item: UserCredentialLikeUserCredential = template.createRelationshipBetween(userLiking, userLikes, classOf[UserCredentialLikeUserCredential], RelationshipTypesScala.LIKES_USER.Constant, false)
    item.like(likeValue, userLikeIP)
    this.saveUserLike(item)
  }

  @Transactional(readOnly = false)
  def likeRecipe(userLiking: UserCredential, userLike: Recipe, likeValue: Boolean, userLikeIP: String): UserCredentialLikeRecipe = {
    val item: UserCredentialLikeRecipe = template.createRelationshipBetween(userLiking, userLike, classOf[UserCredentialLikeRecipe], RelationshipTypesScala.LIKES_RECIPE.Constant, false)
    item.like(likeValue, userLikeIP)
    this.saveRecipeLike(item)
  }




  @Transactional(readOnly = false)
  def deleteUserCredentialLikeUserCredentialById(objectId: java.util.UUID): Boolean = {
    this.findUserCredentialLikeUserCredentialById(objectId) match {
      case null =>
        false
      case item =>
        likeUserCredentialRepository.delete(item)
        true
    }
  }

  @Transactional(readOnly = false)
  def deleteUserCredentialLikeRecipeById(objectId: java.util.UUID): Boolean = {
    this.findUserCredentialLikeRecipeById(objectId) match {
      case null =>
        false
      case item =>
        likeRecipeRepository.delete(item)
        true
    }
  }

  @Transactional(readOnly = false)
  def saveUserLike(newItem: UserCredentialLikeUserCredential): UserCredentialLikeUserCredential = {
    val newResult = likeUserCredentialRepository.save(newItem)
    newResult
  }

  @Transactional(readOnly = false)
  def saveRecipeLike(newItem: UserCredentialLikeRecipe): UserCredentialLikeRecipe = {
    val newResult = likeRecipeRepository.save(newItem)
    newResult
  }

}
