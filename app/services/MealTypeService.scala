package services

import _root_.java.util.UUID
import javax.inject.{Named,Inject}
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import models.event.MealType
import repositories.MealTypeRepository
import scala.collection.JavaConverters._

//@Named
@Service
class MealTypeService @Inject() (val mealTypeRepository: MealTypeRepository) {

  /*
  @Autowired
  var template: Neo4jTemplate = _

  @Autowired
  var mealTypeRepository: MealTypeRepository = _
*/

  @Transactional(readOnly = false)
   def create(name: String, order: Int = 0): MealType = {
    mealTypeRepository.save(new MealType(name,order))
  }

  @Transactional(readOnly = true)
  def listAll(): Option[List[MealType]] = {
    IteratorUtil.asCollection(mealTypeRepository.findAll()).asScala.toList match {
      case null => None
      case tags => Some(tags)
    }
  }

  @Transactional(readOnly = true)
  def findById(objectId: UUID): Option[MealType] = {
    mealTypeRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  @Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = {
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        mealTypeRepository.delete(item)
        true
    }
  }

  @Transactional(readOnly = false)
  def deleteAll(): Boolean = {
    mealTypeRepository.deleteAll()
    true
  }

  @Transactional(readOnly = false)
  def save(item: MealType): MealType = {
    mealTypeRepository.save(item)
  }

}
