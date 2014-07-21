package services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import scala.collection.JavaConverters._
import scala.List
import org.springframework.transaction.annotation.Transactional
import repositories._
import models.location.County

@Service
class CountyService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var countyRepository: CountyRepository = _

  def findByName(name: String): County = {
    countyRepository.findBySchemaPropertyValue("countyName", name)
  }

  @Transactional(readOnly = true)
  def findById(objectId: java.util.UUID): County = {
    countyRepository.findByobjectId(objectId)
  }

  @Transactional(readOnly = true)
  def getListOfAll: Option[List[County]] = {
    val listOfAll: List[County] = template.findAll(classOf[County]).iterator.asScala.toList

    if(listOfAll.isEmpty)
      None
    else
      Some(listOfAll)
  }

  @Transactional(readOnly = false)
  def deleteById(objectId: java.util.UUID): Boolean = {
    val item = this.findById(objectId)
    if(item != null)
    {
      countyRepository.delete(item)
      return true
    }
    false
  }

  @Transactional(readOnly = false)
  def deleteAllContentPages() {
    countyRepository.deleteAll()
  }

  @Transactional(readOnly = false)
  def add(newItem: County): County = {
    val newResult = countyRepository.save(newItem)
    newResult
  }


}
