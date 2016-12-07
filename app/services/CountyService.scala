package services

import javax.inject.{Named, Inject}

import customUtils.security.SecureSocialRuntimeEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import play.api.i18n.{MessagesApi, I18nSupport, Messages}
import traits.TransactionSupport
import scala.collection.JavaConverters._
import scala.List
import org.springframework.transaction.annotation.{EnableTransactionManagement, Transactional}
import repositories._
import models.location.County
import java.util.UUID
import play.api.Play.current

import scala.collection.mutable
import play.api.cache.Cache

//@Service
class CountyService @Inject() (val template: Neo4jTemplate,
                               val countyRepository: CountyRepository,
                               val messagesApi: MessagesApi) extends I18nSupport with TransactionSupport {

  /*
  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var countyRepository: CountyRepository = _
*/

  val cacheListKey = "county.list"

  def findByName(name: String): County = {
    countyRepository.findBySchemaPropertyValue("countyName", name)
  }


  def findById(objectId: UUID): Option[County] = withTransaction(template){
    countyRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }


  def createCounty(name: String, order: Int = 0, persist: Boolean): County = withTransaction(template){
    val newCounty = new County(name, order)

    if (persist) {
     val savedCounty = add(newCounty)
      savedCounty
    }
    else
      newCounty
  }


  def getListOfAll: Option[List[County]] = withTransaction(template){

    val returnList: List[County] = Cache.getOrElse[List[County]](cacheListKey){
     countyRepository.findAll().asScala.toList match {
        case null | Nil  => Nil
        case items => {
          addToCache(cacheListKey, items)
          items
        }
      }
    }

    if(returnList.isEmpty)
      None
    else
      Some(returnList)

  }


  def getCounties: Option[Seq[(String,String)]] = withTransaction(template){
    val counties: Option[Seq[(String,String)]] = this.getListOfAll match {
      case Some(counties) =>
        var bufferList : mutable.Buffer[(String,String)] = mutable.Buffer[(String,String)]()

        // Prepend the first selection
        bufferList += (("", Messages("filterform.counties")))

        // Map and add the rest
        counties.sortBy(tw => tw.name).toBuffer.map {
          item: County =>
            bufferList += ((item.objectId.toString, item.name))
        }

        Some(bufferList.toSeq)
      case None =>
        None
    }

    counties
  }



  def deleteById(objectId: UUID): Boolean = withTransaction(template){
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        countyRepository.delete(item)
        removeFromCache(cacheListKey)
        true
    }
  }


  def deleteAll(): Boolean = withTransaction(template){
    removeFromCache(cacheListKey)
    countyRepository.deleteAll
    true
  }


  def add(newItem: County): County = withTransaction(template){
    removeFromCache(cacheListKey)
    countyRepository.save(newItem)
  }

  def addToCache(key: String, objToCache: Any) = {
    Cache.set(key, objToCache)
  }

  def removeFromCache(cacheIdent: String) = {
    Cache.remove(cacheIdent)
  }
}
