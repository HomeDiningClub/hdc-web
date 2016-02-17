package services

import _root_.java.util.UUID
import javax.inject.{Named,Inject}
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import models.event.MealType
import repositories.MealTypeRepository
import traits.TransactionSupport
import scala.collection.JavaConverters._

//@Named
//@Service
class MealTypeService @Inject() (val template: Neo4jTemplate,
                                 val mealTypeRepository: MealTypeRepository) extends TransactionSupport {

  /*
  @Autowired
  var template: Neo4jTemplate = _

  @Autowired
  var mealTypeRepository: MealTypeRepository = _
*/

  //@Transactional(readOnly = false)
   def create(name: String, order: Int = 0): MealType = withTransaction(template){
    mealTypeRepository.save(new MealType(name,order))
  }

  //@Transactional(readOnly = true)
  def listAll(): Option[List[MealType]] = withTransaction(template){
    IteratorUtil.asCollection(mealTypeRepository.findAll()).asScala.toList match {
      case null => None
      case tags => Some(tags)
    }
  }

  //@Transactional(readOnly = true)
  def findById(objectId: UUID): Option[MealType] = withTransaction(template){
    mealTypeRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  //@Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = withTransaction(template){
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        mealTypeRepository.delete(item)
        true
    }
  }

  //@Transactional(readOnly = false)
  def deleteAll(): Boolean = withTransaction(template){
    mealTypeRepository.deleteAll()
    true
  }

  //@Transactional(readOnly = false)
  def save(item: MealType): MealType = withTransaction(template){
    mealTypeRepository.save(item)
  }

}
