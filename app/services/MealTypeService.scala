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

class MealTypeService @Inject() (val template: Neo4jTemplate,
                                 val mealTypeRepository: MealTypeRepository) extends TransactionSupport {

   def create(name: String, order: Int = 0): MealType = withTransaction(template){
    mealTypeRepository.save(new MealType(name,order))
  }

  def listAll(): Option[List[MealType]] = withTransaction(template){
    IteratorUtil.asCollection(mealTypeRepository.findAll()).asScala.toList match {
      case null => None
      case items => Some(items.sortBy(m => m.order))
    }
  }

  def getMealTypesAsSeq: Option[Seq[(String, String)]] ={
    this.listAll() match {
      case None => None
      case Some(list) =>
        Some(list.map {as =>
          (as.objectId.toString,as.name)
        }.toSeq)
    }
  }

  def findById(objectId: UUID): Option[MealType] = withTransaction(template){
    mealTypeRepository.findByobjectId(objectId.toString) match {
      case null => None
      case item => Some(item)
    }
  }

  def deleteById(objectId: UUID): Boolean = withTransaction(template){
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        mealTypeRepository.delete(item)
        true
    }
  }

  def deleteAll(): Boolean = withTransaction(template){
    mealTypeRepository.deleteAll()
    true
  }

  def save(item: MealType): MealType = withTransaction(template){
    mealTypeRepository.save(item)
  }

}
