package services

import models.files.ContentFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import repositories._
import models.{UserProfile, UserCredential, Recipe}
import scala.collection.JavaConverters._
import scala.List
import java.util.UUID
import models.viewmodels.RecipeBox
import controllers.routes
import utils.Helpers

import scala.collection.mutable.ListBuffer

@Service
class RecipeService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var recipeRepository: RecipeRepository = _

//  def findPageByName(name: String): Recipe = {
//    recipeRepository.findBySchemaPropertyValue("name", name)
//  }


  @Transactional(readOnly = true)
  def findByownerProfileProfileLinkNameAndRecipeLinkName(profileLinkNane: String, recipeLinkName: String): Option[Recipe] = {
    recipeRepository.findByownerProfileProfileLinkNameAndRecipeLinkName(profileLinkNane, recipeLinkName) match {
      case null => None
      case profile =>
        Some(profile)
    }
  }

  @Transactional(readOnly = true)
  def findByrecipeLinkName(recipeLinkName: String): Option[Recipe] = {

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

  @Transactional(readOnly = true)
  def findById(objectId: UUID): Option[Recipe] = {
    recipeRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  @Transactional(readOnly = true)
  def getListOfAll: List[Recipe] = {
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

  // This code is ugly as hell, but replaces an earlier image
  // Remodel to JSON-delete etc in the future
  def getSortedRecipeImages(recipe: Recipe): Option[List[ContentFile]] = {
    recipe.getRecipeImages.asScala match {
      case Nil => None
      case images =>
        Some(images.toList.sortBy(file => file.graphId))
    }
  }

  def parseDouble(s: String) = try { Some(s.toDouble) } catch { case _ => None }

  @Transactional(readOnly = true)
  def getRecipeBoxes(user: UserCredential): Option[List[RecipeBox]] = {

    println("user : " + user.userId)


    println("before .....")

    var list = recipeRepository.findReceipies(user.emailAddress)
    var itter = list.iterator()
    var recipeList : ListBuffer[RecipeBox] = new ListBuffer[RecipeBox]


    println("efter .....")

    while(itter.hasNext()) {

      var obj = itter.next()

      if (obj.getUserId() == user.userId) {

        println("##################################################")
        println("LinkName : " + obj.getLinkName())
        println("Name : " + obj.getName())
        println("ObjectId : " + obj.getobjectId())
        println("PreAmble : " + obj.getpreAmble())
        println("profileLinkname : " + obj.getprofileLinkName())
        println("userId : " + obj.getUserId())

        var v = obj.getRating() match {
          case null => "0.0"
          case _ => obj.getRating()
        }
        println("Rating : " + v)

        var dDot = v.indexOf(".")
        var str: String = v.substring(0, dDot)

        println("Rating (Int) : " + str.toInt)

        println("##################################################")
        var linkToRecipe = obj.getprofileLinkName() match {
          case null | "" => "#"
          case link => routes.RecipePageController.viewRecipeByNameAndProfile(obj.getprofileLinkName(), obj.getName()).url
        }

        var recipe = RecipeBox(None, obj.getLinkName(), obj.getName(), Some(obj.getpreAmble()), None, str.toInt)
        recipeList += recipe
      } else {
        println("Not to view " + obj.getUserId())
      }
    }
    println("ok .....")

    val startPageBoxes: List[RecipeBox] = recipeList.toList

    // recipeLinkName
    // profileLinkName

/*
    val startPageBoxes: List[RecipeBox] = this.getListOwnedBy(user) match {
      case None => List.empty
      case Some(items) => items.map {
        recipeItem: Recipe =>
          RecipeBox(
            objectId = Some(recipeItem.objectId),
            linkToRecipe = recipeItem.getLink match {
              case null | "" => "#"
              case link => routes.RecipePageController.viewRecipeByNameAndProfile(user.profiles.iterator.next.profileLinkName,link).url
            },
            name = recipeItem.getName,
            preAmble = recipeItem.getPreAmble match {
              case "" | null =>
                var retBody = Helpers.removeHtmlTags(recipeItem.getMainBody)

                if (retBody.length > 125)
                  retBody = retBody.substring(0, 125) + "..."

                Some(retBody)
              case content => Some(content)
            },
            mainImage = recipeItem.getMainImage match {
              case null => None
              case item => Some(item.getTransformByName("box").getUrl)
            },
            recipeRating = 0)
      }
    }
*/
    if(startPageBoxes.isEmpty)
      None
    else
      Some(startPageBoxes)
  }

  @Transactional(readOnly = true)
  def getListOwnedBy(user: UserCredential): Option[List[Recipe]] = {
    recipeRepository.findByownerProfileOwner(user).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  @Transactional(readOnly = true)
  def getListOwnedBy(userProfile: UserProfile): Option[List[Recipe]] = {
    recipeRepository.findByownerProfile(userProfile).iterator.asScala.toList match {
      case null => None
      case listOfItems => Some(listOfItems)
    }
  }

  @Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = {
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        item.deleteMainImage()
        item.deleteRecipeImages()
        recipeRepository.delete(item)
        true
    }
  }

  // Fetching
  @Transactional(readOnly = true)
  def fetchRecipe(recipe: Recipe): Recipe = {
    template.fetch(recipe)
  }


  @Transactional(readOnly = false)
  def deleteAll {
    recipeRepository.deleteAll()
  }

  @Transactional(readOnly = false)
  def add(newContent: Recipe): Recipe = {
    val newContentResult = recipeRepository.save(newContent)
    newContentResult
  }


}
