package services

import _root_.java.util.UUID
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.transaction.annotation.Transactional

import securesocial.core._
import scala.Some

import models.profile.TagWord
import repositories.TagWordRepository
import scala.collection.JavaConverters._


@Service
class TagWordService {

  @Autowired
  var template: Neo4jTemplate = _

  @Autowired
  var tagWordRepository: TagWordRepository = _

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
      case null => None
      case tags => Some(tags)
    }
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
        true
    }
  }

  @Transactional(readOnly = false)
  def save(item: TagWord): TagWord = {
    val result = tagWordRepository.save(item)
    result
  }

}
