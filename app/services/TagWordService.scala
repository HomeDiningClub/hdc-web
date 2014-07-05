package services

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
   def createTag(name: String, idName: String, order: String = ""): TagWord = {
    var newTag: TagWord = new TagWord
    var resultTag : TagWord = new TagWord
    newTag.tagName = name
    newTag.tagId = idName
    newTag.orderId = order

    resultTag = tagWordRepository.save(newTag)
    resultTag
  }

  @Transactional(readOnly = true)
  def listAll(): List[TagWord] = {
   // val listOfTags: List[TagWord] = IteratorUtil.asCollection(tagWordRepository.findAll()).asScala.toList
   val listOfTags: List[TagWord] = IteratorUtil.asCollection(tagWordRepository.findAll()).asScala.toList
    listOfTags
  }

}
