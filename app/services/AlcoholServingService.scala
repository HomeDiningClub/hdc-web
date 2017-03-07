package services

import _root_.java.util.UUID
import javax.inject.Inject

import models.event.AlcoholServing
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.data.neo4j.support.Neo4jTemplate
import repositories.AlcoholServingRepository
import traits.TransactionSupport

import scala.collection.JavaConverters._

class AlcoholServingService @Inject() (val template: Neo4jTemplate,
                                 val alcoholServingRepository: AlcoholServingRepository) extends TransactionSupport {

   def create(name: String, order: Int = 0): AlcoholServing = withTransaction(template){
     alcoholServingRepository.save(new AlcoholServing(name,order))
  }

  def listAll(): Option[List[AlcoholServing]] = withTransaction(template){
    IteratorUtil.asCollection(alcoholServingRepository.findAll()).asScala.toList match {
      case null | Nil => None
      case items => Some(items.sortBy(x => x.order))
    }
  }

  def getAlcoholServingsAsSeq: Option[Seq[(String, String)]] ={
    this.listAll() match {
      case None => None
      case Some(list) =>
        Some(list.map { as =>
          (as.objectId.toString, as.name)
        })
    }
  }


  def findById(objectId: UUID): Option[AlcoholServing] = withTransaction(template){
    alcoholServingRepository.findByobjectId(objectId.toString) match {
      case null => None
      case item => Some(item)
    }
  }

  def deleteById(objectId: UUID): Boolean = withTransaction(template){
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        alcoholServingRepository.delete(item)
        true
    }
  }

  def deleteAll(): Boolean = withTransaction(template){
    alcoholServingRepository.deleteAll()
    true
  }

  def save(item: AlcoholServing): AlcoholServing = withTransaction(template){
    alcoholServingRepository.save(item)
  }

}
