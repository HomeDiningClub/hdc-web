package services

import javax.inject.{Named,Inject}

import models.files.ContentFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable, PageRequest}
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traits.TransactionSupport
import scala.language.existentials
import repositories._
import models.{UserProfile, UserCredential, Recipe}
import scala.collection.JavaConverters._
import scala.List
import java.util.UUID
import models.viewmodels.RecipeBox
import controllers.routes
import customUtils.Helpers

import scala.collection.mutable.ListBuffer

//@Named
//@Service
class RecipeService @Inject()(val template: Neo4jTemplate,
                              val recipeRepository: RecipeRepository) extends TransactionSupport {

  /*
  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var recipeRepository: RecipeRepository = _
*/
//  def findPageByName(name: String): Recipe = {
//    recipeRepository.findBySchemaPropertyValue("name", name)
//  }


  //@Transactional(readOnly = true)
  def findByownerProfileProfileLinkNameAndRecipeLinkName(profileLinkNane: String, recipeLinkName: String): Option[Recipe] = withTransaction(template){
    recipeRepository.findByownerProfileProfileLinkNameAndRecipeLinkName(profileLinkNane, recipeLinkName) match {
      case null => None
      case profile =>
        Some(profile)
    }
  }

  //@Transactional(readOnly = true)
  def findByrecipeLinkName(recipeLinkName: String): Option[Recipe] = withTransaction(template){

    var returnObject: Option[Recipe] = None
    if(recipeLinkName.nonEmpty)
    {
      returnObject = recipeRepository.findByrecipeLinkName(recipeLinkName) match {
        case null => None
        case profile =>
          // Lazy fetching, this is crazy slow
//          if(fetchAll){
//            template.fetch(profile.getOwnerProfile)
//          }
          Some(profile)
      }
    }
    returnObject
  }

  //@Transactional(readOnly = true)
  def findById(objectId: UUID): Option[Recipe] = withTransaction(template){
    recipeRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  //@Transactional(readOnly = true)
  def getCountOfAll: Int = withTransaction(template){
    recipeRepository.getCountOfAll()
  }


  //@Transactional(readOnly = true)
  def getListOfAll: List[Recipe] = withTransaction(template){
    recipeRepository.findAll.iterator.asScala.toList match {
      case null => null
      case recipes =>

        // Lazy fetching
//        if(fetchAll){
//          val fetchedList = recipes.par.foreach { p =>
//            if(p.getOwnerProfile != null)
//              template.fetch(p.getOwnerProfile)
//          }
//          fetchedList
//        }
        recipes
    }
  }

  // Get sorted images
  def getSortedRecipeImages(recipe: Recipe): Option[List[ContentFile]] = withTransaction(template){
    recipe.getRecipeImages.asScala match {
      case Nil => None
      case images =>
        Some(images.toList.sortBy(file => file.graphId))
    }
  }

  def convertToCommaSepStringOfObjectIds(listOfFiles: Option[List[ContentFile]]): Option[String] = {
    listOfFiles match {
      case None => None
      case Some(items) =>
        Some(items.map(_.objectId).mkString(","))
    }
  }

  def parseDouble(s: String) = try { Some(s.toDouble) } catch { case _ : Throwable => None }

  //@Transactional(readOnly = true)
  def getRecipeBoxes(user: UserCredential): Option[List[RecipeBox]] = withTransaction(template){
    // Without paging
    this.getRecipeBoxesPage(user, 0)
  }

  //@Transactional(readOnly = true)
  def getRecipeBoxesPage(user: UserCredential, pageNo: Integer): Option[List[RecipeBox]] = withTransaction(template){

    // With paging
    // 0 current page, 6 number of recipes for each page

    val list = recipeRepository.findRecipesOnPage(user.objectId.toString, new PageRequest(pageNo, 6))
    val iterator = list.iterator()
    var recipeList : ListBuffer[RecipeBox] = new ListBuffer[RecipeBox]

    while(iterator.hasNext()) {

      val obj = iterator.next()

      // Rating
      val v = obj.getRating() match {
        case null => "0.0"
        case _ => obj.getRating()
      }

      // Convert string to double, round to Int and convert to Int
      val ratingValue : Int = v.toDouble.round.toInt

      // Link
      val linkToRecipe = (obj.getprofileLinkName, obj.getLinkName) match {
        case (null|null) | ("","") => "#"
        case (profLink,recLink) => routes.RecipePageController.viewRecipeByNameAndProfile(profLink, recLink).url
      }

      // Image
      var mainImage = Some("/assets/images/profile/recipe-box-default-bw.png")
      if(obj.getMainImage().iterator().hasNext()){
        mainImage = Some(routes.ImageController.recipeBox(obj.getMainImage().iterator().next()).url)
      }

      // Build return-list
      var recipe = RecipeBox(
        Some(UUID.fromString(obj.getobjectId)),
        linkToRecipe,
        obj.getName,
        obj.getpreAmble match {
        case "" | null =>
          var retBody = Helpers.removeHtmlTags(obj.getMainBody)

          if (retBody.length > 125)
            retBody = retBody.substring(0, 125) + "..."

          Some(retBody)
        case content => Some(content)
        },
        mainImage,
        ratingValue,
        list.getTotalElements,
        list.hasNext,
        list.hasPrevious,
        list.getTotalPages
      )
      recipeList += recipe
    }

    val startPageBoxes: List[RecipeBox] = recipeList.toList

    if(startPageBoxes.isEmpty)
      None
    else
      Some(startPageBoxes)
  }




  //@Transactional(readOnly = true)
  def getListOwnedBy(user: UserCredential): Option[List[Recipe]] = withTransaction(template){
    recipeRepository.findByownerProfileOwner(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  //@Transactional(readOnly = true)
  def getListOwnedBy(userProfile: UserProfile): Option[List[Recipe]] = withTransaction(template){
    recipeRepository.findByownerProfile(userProfile).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  //@Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = withTransaction(template){
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        item.deleteMainImage()
        item.deleteRecipeImages()
        item.deleteRatings()
        item.deleteLikes()
        recipeRepository.delete(item)
        true
    }
  }

  // Fetching
  //@Transactional(readOnly = true)
  def fetchRecipe(recipe: Recipe): Recipe = withTransaction(template){
    template.fetch(recipe)
  }


  //@Transactional(readOnly = false)
  private def deleteAll() = withTransaction(template){
    recipeRepository.deleteAll()
  }

  //@Transactional(readOnly = false)
  def add(newContent: Recipe): Recipe = withTransaction(template){
    val newContentResult = recipeRepository.save(newContent)
    newContentResult
  }


}
