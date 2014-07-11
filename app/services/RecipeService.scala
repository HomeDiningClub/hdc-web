package services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories._
import models.Recipe
import scala.collection.JavaConverters._
import scala.List
import java.util.UUID

@Service
class RecipeService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var recipeRepository: RecipeRepository = _

  def findPageByName(name: String): Recipe = {
    recipeRepository.findBySchemaPropertyValue("name", name)
  }

  @Transactional(readOnly = true)
  def findById(objectId: UUID): Recipe = {
    recipeRepository.findByobjectId(objectId)
    //recipeRepository.findOne(objectId)
  }

  @Transactional(readOnly = true)
  def getListOfAll(): List[Recipe] = {
    val list: List[Recipe] = template.findAll(classOf[Recipe]).iterator.asScala.toList
    list
  }

  @Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = {
    val recipe: Recipe = this.findById(objectId)
    if(recipe != null)
    {
      recipeRepository.delete(recipe)
      return true
    }
    false
  }

  @Transactional(readOnly = false)
  def deleteAll() {
    recipeRepository.deleteAll()
  }

  @Transactional(readOnly = false)
  def add(newContent: Recipe): Recipe = {
    val newContentResult = recipeRepository.save(newContent)
    newContentResult
  }


}
