package services

import java.util.UUID

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories._
import models.rating.RatesUserCredential
import models.UserCredential
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


  // RatingUserCredential
  @Transactional(readOnly = true)
  def findRatingById(objectId: java.util.UUID): RatesUserCredential = {
    ratingUserCredentialRepository.findByobjectId(objectId)
  }
  @Transactional(readOnly = true)
  def findRatingByUserWhoIsRated(user: UserCredential): Option[List[RatesUserCredential]] = {
    ratingUserCredentialRepository.findByuserWhoIsRating(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findRatingByRatingValue(ratingValue: Int): Option[List[RatesUserCredential]] = {
    ratingUserCredentialRepository.findByratingValue(ratingValue).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findRatingByUserWhoIsRating(user: UserCredential): Option[List[RatesUserCredential]] = {
    ratingUserCredentialRepository.findByuserRates(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  @Transactional(readOnly = true)
  def hasUserRatedThisUser(currentUser: UserCredential, hasRatedThisUser: UserCredential): Option[RatesUserCredential] = {
    // By using graphId we don't need to load all the relationships
    currentUser.getHasRated.asScala.find(rel => rel.userRates.graphId == hasRatedThisUser.graphId)
  }
  def hasUserRatedThisUser(currentUser: UUID, hasRatedThisUser: UUID): Option[RatesUserCredential] = {
    this.hasUserRatedThisUser(userCredentialRepository.findByobjectId(currentUser), userCredentialRepository.findByobjectId(hasRatedThisUser))
  }
  def hasUserRatedThisUser(currentUser: UserCredential, hasRatedThisUser: UUID): Option[RatesUserCredential] = {
    this.hasUserRatedThisUser(currentUser, userCredentialRepository.findByobjectId(hasRatedThisUser))
  }
  def hasUserRatedThisUser(currentUser: UUID, hasRatedThisUser: UserCredential): Option[RatesUserCredential] = {
    this.hasUserRatedThisUser(userCredentialRepository.findByobjectId(currentUser), hasRatedThisUser)
  }

  @Transactional(readOnly = false)
  def rateUser(userRating: UserCredential, userRates: UserCredential, ratingValue: Int , ratingComment: String , userRaterIP: String ): RatesUserCredential = {
    val rating: RatesUserCredential = template.createRelationshipBetween(userRating, userRates, classOf[RatesUserCredential], RelationshipTypesScala.RATED.Constant, false)
    rating.rate(ratingValue, ratingComment, userRaterIP)
    this.save(rating)
  }

//
//  @Transactional(readOnly = false)
//  def rateUser(userCredential: UserCredential, ratingValue: Int, userIP: String, comment: String): RatesUserCredential = {
//    val rating: RatesUserCredential = template.createRelationshipBetween(this, userCredential, classOf[RatesUserCredential], RelationshipTypesScala.RATED.Constant, false)
//    rating.rate(ratingValue, comment, userIP)
//    this.save(rating)
//  }


  @Transactional(readOnly = false)
  def deleteRatingById(objectId: java.util.UUID): Boolean = {
    this.findRatingById(objectId) match {
      case null =>
        false
      case item =>
        ratingUserCredentialRepository.delete(item)
        true
    }
  }
  @Transactional(readOnly = false)
  def save(newItem: RatesUserCredential): RatesUserCredential = {
    val newResult = ratingUserCredentialRepository.save(newItem)
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



  // ProfileRating






}
