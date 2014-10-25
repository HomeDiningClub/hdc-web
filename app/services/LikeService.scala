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
    likeUserCredentialRepository.findByuserWhoLikes(user.objectId).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeUserCredentialByValue(likeValue: Boolean): Option[List[UserCredentialLikeUserCredential]] = {
    likeUserCredentialRepository.findBylikes(likeValue, "=").iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeUserCredentialByWhoGotLiked(user: UserCredential): Option[List[UserCredentialLikeUserCredential]] = {
    likeUserCredentialRepository.findByuserLikes(user.objectId).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeUserCredentialByUserWhoIsLikesAndUserLikes(user: UserCredential, userLikes: UserCredential): Option[List[UserCredentialLikeUserCredential]] = {
    likeUserCredentialRepository.findByuserWhoLikesAndUserLikes(user.objectId.toString, userLikes.objectId.toString).iterator.asScala.toList match {
      case Nil => None
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
    likeRecipeRepository.findByuserWhoLikes(user.objectId).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeRecipeByValue(likeValue: Boolean): Option[List[UserCredentialLikeRecipe]] = {
    likeRecipeRepository.findBylikes(likeValue,"=").iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeRecipeByWhoGotLiked(recipe: Recipe): Option[List[UserCredentialLikeRecipe]] = {
    likeRecipeRepository.findByuserLikes(recipe.objectId).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findUserCredentialLikeRecipeByUserWhoIsLikesAndUserLikes(user: UserCredential, recipe: Recipe): Option[List[UserCredentialLikeRecipe]] = {
    likeRecipeRepository.findByuserWhoLikesAndUserLikes(user.objectId.toString, recipe.objectId.toString).iterator.asScala.toList match {
      case Nil => None
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
    //val item: UserCredentialLikeUserCredential = template.createRelationshipBetween(userLiking, userLikes, classOf[UserCredentialLikeUserCredential], RelationshipTypesScala.LIKES_USER.Constant, false)

    // Is there already a rating? Don't allow duplicates
    val item = this.findUserCredentialLikeUserCredentialByUserWhoIsLikesAndUserLikes(userLiking, userLikes) match {
      case None =>
        new UserCredentialLikeUserCredential
      case Some(likes) =>
        likes.head
    }
    item.like(userLiking, userLikes, likeValue, userLikeIP)
    this.saveUserLike(item)
  }

  @Transactional(readOnly = false)
  def likeRecipe(userLiking: UserCredential, userLikes: Recipe, likeValue: Boolean, userLikeIP: String): UserCredentialLikeRecipe = {
    //val item: UserCredentialLikeRecipe = template.createRelationshipBetween(userLiking, userLikes, classOf[UserCredentialLikeRecipe], RelationshipTypesScala.LIKES_RECIPE.Constant, false)

    // Is there already a rating? Don't allow duplicates
    val item = this.findUserCredentialLikeRecipeByUserWhoIsLikesAndUserLikes(userLiking, userLikes) match {
      case None =>
        new UserCredentialLikeRecipe
      case Some(likes) =>
        likes.head
    }
    item.like(userLiking, userLikes, likeValue, userLikeIP)
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
