package services

import _root_.java.util.UUID
import javax.inject.Inject
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import play.api.cache.Cache
import play.api.Play.current
import models.profile.{TaggedUserProfile, TagWord}
import repositories.TagWordRepository
import traits.TransactionSupport
import scala.collection.JavaConverters._
import models.UserProfile

import scala.collection.mutable

//@Service
class TagWordService @Inject() (val template: Neo4jTemplate,
                                val tagWordRepository: TagWordRepository,
                                val messagesApi: MessagesApi) extends I18nSupport with TransactionSupport {

  val tagWordCacheKey = "tagWord."

  //@Transactional(readOnly = false)
  def createTag(name: String, idName: String, order: String = "", tagGroupName : String = "" ): TagWord = withTransaction(template){
    var newTag: TagWord = new TagWord
    var resultTag : TagWord = new TagWord
    newTag.tagName = name
    newTag.tagId = idName
    newTag.orderId = order
    newTag.tagGroupName = tagGroupName

    resultTag = tagWordRepository.save(newTag)
    resultTag
  }

  //@Transactional(readOnly = true)
  def listAll(): Option[List[TagWord]] = withTransaction(template){
    val listOfTags: Option[List[TagWord]] = IteratorUtil.asCollection(tagWordRepository.findAll()).asScala.toList match {
      case null => None
      case tags => Some(tags)
    }
    listOfTags
  }

  //@Transactional(readOnly = true)
  def findById(objectId: UUID): Option[TagWord] = withTransaction(template){
    tagWordRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  def findByListOfId(list: List[UUID]): Option[List[TagWord]] = withTransaction(template){
    val arrOfStrings = list.map(c => c.toString).toArray
    tagWordRepository.findByListOfobjectId(arrOfStrings).asScala.toList match {
      case null | Nil => None
      case items => Some(items)
    }
  }

  //@Transactional(readOnly = true)
  def listByGroupOption(groupName: String): Option[List[TagWord]] = withTransaction(template){

    val returnList: List[TagWord] = Cache.getOrElse[List[TagWord]](tagWordCacheKey + groupName){
      tagWordRepository.findByTagGroupName(groupName).asScala.toList match {
        case null | Nil  => Nil
        case tags => {
          addToCache(tagWordCacheKey + groupName, tags)
          tags
        }
      }
    }

    if(returnList.isEmpty)
      None
    else
      Some(returnList)
  }

  //@Transactional(readOnly = true)
  def findByProfileAndGroup(profile: UserProfile, groupName: String): Option[List[TagWord]] = withTransaction(template){
    profile.getTags.asScala.filter(tag => tag.tagWord.tagGroupName.equalsIgnoreCase(groupName)).toList match {
      case null | Nil => None
      case tags => Some(tags.map {
        tup: TaggedUserProfile =>
          tup.tagWord
      })
    }
  }

  //@Transactional(readOnly = true)
  def getFoodAreas: Option[Seq[(String,String)]] = withTransaction(template){
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

  //@Transactional(readOnly = true)
  def listByGroup2(groupName: String): List[TagWord] = withTransaction(template){

    tagWordRepository.findAllBySchemaPropertyValue("searchGroup", groupName).asScala.toList
    // template.lookup("search","tagGroupName:profile").asScala.toList
    //tagWordRepository.findByGruoupName("profile").toList
  }

  //@Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = withTransaction(template) {
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        tagWordRepository.delete(item)
        removeFromCache(item)
        true
    }
  }

  //@Transactional(readOnly = false)
  def deleteAll(): Boolean = withTransaction(template){
    tagWordRepository.findAll.asScala.toList.foreach{ item =>
      removeFromCache(item)
    }
    tagWordRepository.deleteAll()
    true
  }

  //@Transactional(readOnly = false)
  def save(item: TagWord): TagWord = withTransaction(template){
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
