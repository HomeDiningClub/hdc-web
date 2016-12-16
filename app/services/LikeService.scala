package services

import javax.inject.{Named,Inject}

import models.like.{UserCredentialLikeEvent, UserCredentialLikeRecipe, UserCredentialLikeUserCredential}
import models.modelconstants.RelationshipTypesScala
import models.rating.{RatesRecipe, RatesUserCredential}
import models.{Event, Recipe, UserCredential}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories._
import traits.TransactionSupport

import scala.collection.JavaConverters._

class LikeService @Inject()(val template: Neo4jTemplate,
                            val userCredentialRepository: UserCredentialRepository,
                            val likeUserCredentialRepository: LikeUserCredentialRepository,
                            val likeRecipeRepository: LikeRecipeRepository,
                            val likeEventRepository: LikeEventRepository
                           ) extends TransactionSupport {

  // LikeUserCredential
  def findUserCredentialLikeUserCredentialById(objectId: java.util.UUID): UserCredentialLikeUserCredential = withTransaction(template){
    likeUserCredentialRepository.findByobjectId(objectId)
  }

  def findUserCredentialLikeUserCredentialByUserWhoIsLiking(user: UserCredential): Option[List[UserCredentialLikeUserCredential]] = withTransaction(template){
    likeUserCredentialRepository.findByuserWhoLikes(user.objectId).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }

  def findUserCredentialLikeUserCredentialByValue(likeValue: Boolean): Option[List[UserCredentialLikeUserCredential]] = withTransaction(template){
    likeUserCredentialRepository.findBylikes(likeValue, "=").iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }

  def findUserCredentialLikeUserCredentialByWhoGotLiked(user: UserCredential): Option[List[UserCredentialLikeUserCredential]] = withTransaction(template){
    likeUserCredentialRepository.findByuserLikes(user.objectId).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }

  def findUserCredentialLikeUserCredentialByUserWhoIsLikesAndUserLikes(user: UserCredential, userLikes: UserCredential): Option[List[UserCredentialLikeUserCredential]] = withTransaction(template){
    likeUserCredentialRepository.findByuserWhoLikesAndUserLikes(user.objectId.toString, userLikes.objectId.toString).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }


  // LikeRecipe

  def findUserCredentialLikeRecipeById(objectId: java.util.UUID): UserCredentialLikeRecipe = withTransaction(template){
    likeRecipeRepository.findByobjectId(objectId)
  }

  def findUserCredentialLikeRecipeByUserWhoIsLiking(user: UserCredential): Option[List[UserCredentialLikeRecipe]] = withTransaction(template){
    likeRecipeRepository.findByuserWhoLikes(user.objectId).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }

  def findUserCredentialLikeRecipeByValue(likeValue: Boolean): Option[List[UserCredentialLikeRecipe]] = withTransaction(template){
    likeRecipeRepository.findBylikes(likeValue,"=").iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }

  def findUserCredentialLikeRecipeByWhoGotLiked(recipe: Recipe): Option[List[UserCredentialLikeRecipe]] = withTransaction(template){
    likeRecipeRepository.findByuserLikes(recipe.objectId).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }

  def findUserCredentialLikeRecipeByUserWhoIsLikesAndUserLikes(user: UserCredential, recipe: Recipe): Option[List[UserCredentialLikeRecipe]] = withTransaction(template){
    likeRecipeRepository.findByuserWhoLikesAndUserLikes(user.objectId.toString, recipe.objectId.toString).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }

  def findUserCredentialLikeEventByUserWhoIsLikesAndUserLikes(user: UserCredential, event: Event): Option[List[UserCredentialLikeEvent]] = withTransaction(template){
    likeEventRepository.findByuserWhoLikesAndUserLikes(user.objectId.toString, event.objectId.toString).iterator.asScala.toList match {
      case Nil => None
      case listOfItems => Some(listOfItems)
    }
  }



  // Recipe
  def hasUserLikedThisBefore(currentUser: UserCredential, hasLikedThis: Recipe): Option[UserCredentialLikeRecipe] = withTransaction(template){
    likeRecipeRepository.findByuserWhoLikesAndUserLikes(currentUser.objectId.toString, hasLikedThis.objectId.toString).asScala.toList.headOption
  }

  // Event
  def hasUserLikedThisBefore(currentUser: UserCredential, hasLikedThis: Event): Option[UserCredentialLikeEvent] = withTransaction(template){
    likeEventRepository.findByuserWhoLikesAndUserLikes(currentUser.objectId.toString, hasLikedThis.objectId.toString).asScala.toList.headOption
  }

  // UserCredential
  def hasUserLikedThisBefore(currentUser: UserCredential, hasLikedThis: UserCredential): Option[UserCredentialLikeUserCredential] = withTransaction(template){
    likeUserCredentialRepository.findByuserWhoLikesAndUserLikes(currentUser.objectId.toString, hasLikedThis.objectId.toString).asScala.toList.headOption
  }



  def likeUser(userLiking: UserCredential, userLikes: UserCredential, likeValue: Boolean, userLikeIP: String): UserCredentialLikeUserCredential = withTransaction(template){

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


  def likeRecipe(userLiking: UserCredential, userLikes: Recipe, likeValue: Boolean, userLikeIP: String): UserCredentialLikeRecipe = withTransaction(template){

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

  def likeEvent(userLiking: UserCredential, userLikes: Event, likeValue: Boolean, userLikeIP: String): UserCredentialLikeEvent = withTransaction(template){

    // Is there already a rating? Don't allow duplicates
    val item = this.findUserCredentialLikeEventByUserWhoIsLikesAndUserLikes(userLiking, userLikes) match {
      case None =>
        new UserCredentialLikeEvent
      case Some(likes) =>
        likes.head
    }
    item.like(userLiking, userLikes, likeValue, userLikeIP)
    this.saveEventLike(item)
  }




  def deleteUserCredentialLikeUserCredentialById(objectId: java.util.UUID): Boolean = withTransaction(template){
    this.findUserCredentialLikeUserCredentialById(objectId) match {
      case null =>
        false
      case item =>
        likeUserCredentialRepository.delete(item)
        true
    }
  }


  def deleteUserCredentialLikeRecipeById(objectId: java.util.UUID): Boolean = withTransaction(template){
    this.findUserCredentialLikeRecipeById(objectId) match {
      case null =>
        false
      case item =>
        likeRecipeRepository.delete(item)
        true
    }
  }


  def saveUserLike(newItem: UserCredentialLikeUserCredential): UserCredentialLikeUserCredential = withTransaction(template){
    val newResult = likeUserCredentialRepository.save(newItem)
    newResult
  }


  def saveRecipeLike(newItem: UserCredentialLikeRecipe): UserCredentialLikeRecipe = withTransaction(template){
    val newResult = likeRecipeRepository.save(newItem)
    newResult
  }

  def saveEventLike(newItem: UserCredentialLikeEvent): UserCredentialLikeEvent = withTransaction(template){
    val newResult = likeEventRepository.save(newItem)
    newResult
  }

}
