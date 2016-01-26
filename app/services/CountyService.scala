package services

import javax.inject.{Named, Inject}

import customUtils.security.SecureSocialRuntimeEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import play.api.i18n.{MessagesApi, I18nSupport, Messages}
import scala.collection.JavaConverters._
import scala.List
import org.springframework.transaction.annotation.Transactional
import repositories._
import models.location.County
import java.util.UUID
import play.api.Play.current

import scala.collection.mutable
import play.api.cache.Cache

//@Named
@Service
class CountyService @Inject() (val template: Neo4jTemplate, val countyRepository: CountyRepository, val messagesApi: MessagesApi) extends I18nSupport  {

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

  @Transactional(readOnly = true)
  def findById(objectId: UUID): Option[County] = {
    countyRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  @Transactional(readOnly = true)
  def createCounty(name: String, order: Int = 0, persist: Boolean): County = {
    val newCounty = new County(name, order)

    if (persist) {
     val savedCounty = add(newCounty)
      savedCounty
    }
    else
      newCounty
  }

  @Transactional(readOnly = true)
  def getListOfAll: Option[List[County]] = {

    countyRepository.findAll().asScala.toList match {
      case null | Nil  => None
      case items => {

        val cachedItems = Cache.getOrElse[List[County]](cacheListKey) {
          addToCache(cacheListKey, items)
          items
        }
        Some(cachedItems)
      }
    }
  }

  @Transactional(readOnly = true)
  def getCounties: Option[Seq[(String,String)]] = {
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


  @Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = {
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        countyRepository.delete(item)
        removeFromCache(cacheListKey)
        true
    }
  }

  @Transactional(readOnly = false)
  def deleteAll(): Boolean = {
    removeFromCache(cacheListKey)
    countyRepository.deleteAll
    true
  }

  @Transactional(readOnly = false)
  def add(newItem: County): County = {
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
