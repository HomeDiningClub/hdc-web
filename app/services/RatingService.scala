package services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories._
import models.rating.RatingUserCredential
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
  def findRatingById(objectId: java.util.UUID): RatingUserCredential = {
    ratingUserCredentialRepository.findByobjectId(objectId)
  }
  @Transactional(readOnly = true)
  def findRatingByUserWhoIsRated(user: UserCredential): Option[List[RatingUserCredential]] = {
    ratingUserCredentialRepository.findByuserWhoIsRating(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findRatingByRatingValue(ratingValue: Int): Option[List[RatingUserCredential]] = {
    ratingUserCredentialRepository.findByratingValue(ratingValue).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }
  @Transactional(readOnly = true)
  def findRatingByUserWhoIsRating(user: UserCredential): Option[List[RatingUserCredential]] = {
    ratingUserCredentialRepository.findByuserRates(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

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
  def save(newItem: RatingUserCredential): RatingUserCredential = {
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

  @Transactional(readOnly = false)
  def rateUser(userCredential: UserCredential, ratingValue: Int, userIP: String, comment: String): RatingUserCredential = {
    val rating: RatingUserCredential = template.createRelationshipBetween(this, userCredential, classOf[RatingUserCredential], RelationshipTypesScala.RATED.Constant, false)
    rating.rate(ratingValue, comment, userIP)
    this.save(rating)
  }


  // ProfileRating






}
