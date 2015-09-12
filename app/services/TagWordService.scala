package services

import _root_.java.util.UUID
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.transaction.annotation.Transactional
import play.api.i18n.Messages
import play.api.cache.Cache
import play.api.Play.current

import securesocial.core._
import scala.Some

import models.profile.{TaggedUserProfile, TagWord}
import repositories.TagWordRepository
import scala.collection.JavaConverters._
import models.UserProfile

import scala.collection.mutable


@Service
class TagWordService {

  @Autowired
  var template: Neo4jTemplate = _

  @Autowired
  var tagWordRepository: TagWordRepository = _

  val tagWordCacheKey = "tagWord."

  @Transactional(readOnly = false)
   def createTag(name: String, idName: String, order: String = "", tagGroupName : String = "" ): TagWord = {
    var newTag: TagWord = new TagWord
    var resultTag : TagWord = new TagWord
    newTag.tagName = name
    newTag.tagId = idName
    newTag.orderId = order
    newTag.tagGroupName = tagGroupName

    resultTag = tagWordRepository.save(newTag)
    resultTag
  }

  @Transactional(readOnly = true)
  def listAll(): Option[List[TagWord]] = {
    val listOfTags: Option[List[TagWord]] = IteratorUtil.asCollection(tagWordRepository.findAll()).asScala.toList match {
      case null => None
      case tags => Some(tags)
    }
    listOfTags
  }

  @Transactional(readOnly = true)
  def findById(objectId: UUID): Option[TagWord] = {
    tagWordRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }


  @Transactional(readOnly = true)
  def listByGroupOption(groupName: String): Option[List[TagWord]] = {
    tagWordRepository.findByTagGroupName(groupName).asScala.toList match {
      case null | Nil  => None
      case tags => {

        val cachedTagWords = Cache.getOrElse[List[TagWord]](tagWordCacheKey + groupName) {
          addToCache(tagWordCacheKey + groupName, tags)
          tags
        }
        Some(cachedTagWords)
      }
    }
  }

  @Transactional(readOnly = true)
  def findByProfileAndGroup(profile: UserProfile, groupName: String): Option[List[TagWord]] = {
    profile.getTags.asScala.filter(tag => tag.tagWord.tagGroupName.equalsIgnoreCase(groupName)).toList match {
      case null | Nil => None
      case tags => Some(tags.map {
        tup: TaggedUserProfile =>
          tup.tagWord
      })
    }
  }

  @Transactional(readOnly = true)
  def getFoodAreas: Option[Seq[(String,String)]] = {
    val foodTags: Option[Seq[(String,String)]] = this.listByGroupOption("profile") match {
      case Some(listOfTags) =>
        var bufferList : mutable.Buffer[(String,String)] = mutable.Buffer[(String,String)]()

        // Prepend the fist selection
        bufferList += (("", Messages("filterform.foodarea")))

        // Map and add the rest
        listOfTags.sortBy(tw => tw.tagName).toBuffer.map {
          tag: TagWord =>
            bufferList += ((tag.objectId.toString, tag.tagName))
        }

        Some(bufferList.toSeq)
      case None =>
        None
    }

    foodTags
  }


  //  @Transactional(readOnly = true)
//  def listByGroup(groupName: String): Option[List[TagWord]] = {
//   listAll() match {
//     case null => None
//     case tags: List[TagWord] => Some(tags.filter(t: TagWord => t.tagGroupName.equalsIgnoreCase(groupName)))
//    }
//  }

  @Transactional(readOnly = true)
  def listByGroup2(groupName: String): List[TagWord] = {

    tagWordRepository.findAllBySchemaPropertyValue("searchGroup", groupName).asScala.toList
    // template.lookup("search","tagGroupName:profile").asScala.toList
    //tagWordRepository.findByGruoupName("profile").toList
  }

  @Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = {
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        tagWordRepository.delete(item)
        removeFromCache(item)
        true
    }
  }

  @Transactional(readOnly = false)
  def deleteAll: Boolean = {
    tagWordRepository.findAll.asScala.toList.foreach{ item =>
      removeFromCache(item)
    }
    tagWordRepository.deleteAll
    true
  }


  @Transactional(readOnly = false)
  def save(item: TagWord): TagWord = {
    val result = tagWordRepository.save(item)
    removeFromCache(item)
    result
  }

  def addToCache(key: String, objToCache: Any) = {
    Cache.set(key, objToCache)
  }

  def removeFromCache(item: TagWord) = {
    Cache.remove(tagWordCacheKey + item.tagGroupName)
  }
}
