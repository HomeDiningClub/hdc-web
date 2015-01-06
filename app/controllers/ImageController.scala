package controllers

import _root_.java.io.File
import _root_.java.util.UUID

import enums.{FileTypeEnums, RoleEnums}
import models.viewmodels.ImageData
import securesocial.core._
import play.api.libs.json._
import play.api.libs.json.Writes._
import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import org.springframework.beans.factory.annotation.Autowired
import services.ContentFileService
import utils.Helpers
import utils.authorization.WithRole
import se.digiplant.scalr._
import se.digiplant.res._

@SpringController
class ImageController extends Controller with SecureSocial {

  @Autowired
  private var contentFileService: ContentFileService = _

  implicit val imageWrites = Json.writes[ImageData]

  private val faultyImageRequestAction: Action[AnyContent] = Action(Ok(""))

  // Returns JSON
  def listImages(selected: String = "") = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: RequestHeader =>

    // Remove empty entry if string is empty
    val splitSelected = selected.split(',').toBuffer
    if(splitSelected.length == 1 && splitSelected.head.trim == ""){
      splitSelected.remove(0)
    }
    val splitSelectedArray = splitSelected.toArray

    utils.Helpers.getUserFromRequest match {
      case Some(user) => {
        val images = contentFileService.getImagesForUser(user.objectId).map(
          s => new ImageData(
            Some(s.objectId.toString),
            s.name,
            routes.ImageController.imgChooserThumb(s.getStoreId).url,
            if(splitSelectedArray.isEmpty){ false }else{ splitSelected.exists(item => UUID.fromString(item) == s.objectId) }
          )
        )
        Ok(Json.toJson(images.map(s => Json.toJson(s))))
      }
      case None => BadRequest(Json.toJson("error"))
    }

  }

  // Returns JSON
  def previewImages(selected: String = "") = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: RequestHeader =>

    // Remove empty entry if string is empty
    val splitSelected = selected.split(',').toBuffer
    if(splitSelected.length == 1 && splitSelected.head.trim == ""){
      splitSelected.remove(0)
    }
    val splitSelectedArray = splitSelected.toArray

    if(splitSelectedArray.isEmpty)
      Ok(Json.toJson("No images"))

    // Convert to UUID to check for validity
    val listSelected = splitSelectedArray.map { item =>
        UUID.fromString(item)
    }

    utils.Helpers.getUserFromRequest match {
      case Some(user) => {
        val images = contentFileService.getByListOfobjectIds(listSelected).map(
          s => new ImageData(
            Some(s.objectId.toString),
            s.name,
            routes.ImageController.imgChooserThumb(s.getStoreId).url,
            true
          )
        )
        Ok(Json.toJson(images.map(s => Json.toJson(s))))
      }
      case None => BadRequest(Json.toJson("Error no user"))
    }
  }


  def uploadImageSubmit() = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    utils.Helpers.getUserFromRequest match {
      case Some(user) => {

        request.body.file("files").map {
          file =>
            // We have to do this here, since otherwise we have a lock on the file
            val fileName = file.filename
            val newFile = contentFileService.createTemporaryFile(fileName)
            val contentType = file.contentType
            file.ref.moveTo(newFile, true)

            contentFileService.uploadFile(newFile, fileName, contentType, Helpers.getUserFromRequest.get.objectId, FileTypeEnums.IMAGE) match {
              case Some(uploadedImage) => {
                val image: ImageData = new ImageData(Some(uploadedImage.objectId.toString), uploadedImage.name, routes.ImageController.imgChooserThumb(uploadedImage.getStoreId).url, false, "delete")
                val returnJSon = Json.obj("files" -> Json.arr(Json.toJson(image)))

                /* Manual JSON, works great, but using objects instead
                  val returnJSon2 = Json.obj(
                  "files" -> Json.arr(
                    Json.obj(
                      "objectid" -> uploadedImage.objectId.toString,
                      "url" -> routes.ImageController.imgChooserThumb(uploadedImage.getStoreId).url,
                      "name" -> uploadedImage.name,
                      "action" -> "delete"
                    )
                  )
                )
                */

                Ok(returnJSon)
              }
              case None =>
                BadRequest(Json.toJson("Error: Could not upload image"))
            }

        }.getOrElse {
          BadRequest(Json.toJson("Error: No picture selected"))
        }
      }
      case None =>
        BadRequest(Json.toJson("Error: No user"))
    }
  }

  // Image resources
  // General functions
  // Mode: AUTOMATIC, FIT_EXACT, FIT_TO_WIDTH, FIT_TO_HEIGHT, CROP
  def at(fileUid: String): Action[AnyContent] = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.res.ResAssets.at(id)
    }
  }

  def auto(fileUid: String, width: Int, height: Int): Action[AnyContent] = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, width, height)
    }
  }

  def crop(fileUid: String, width: Int, height: Int) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, width, height, mode = "CROP")
    }
  }

  // Image chooser
  def imgChooserThumb(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, 100, 100, mode = "CROP")
    }
  }

  // User avatar
  def userMini(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, 30, 30, mode = "CROP")
    }
  }
  def userThumb(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, 100, 100, mode = "CROP")
    }
  }

  // UserProfile
  def profileThumb(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, 150, 100, mode = "CROP")
    }
  }
  def profileBox(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, 263, 160, mode = "FIT_TO_WIDTH")
    }
  }
  def profileNormal(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, 460, 305, mode = "FIT_TO_WIDTH")
    }
  }
  def profileBig(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, 800, 600, mode = "FIT_TO_WIDTH")
    }
  }

  // Recipe
  def recipeThumb(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, 150, 100, mode = "CROP")
    }
  }
  def recipeBox(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, 275, 160, mode = "FIT_TO_WIDTH")
    }
  }
  def recipeNormal(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        utils.scalr.ScalrResAssets.at(id, 460, 345, mode = "FIT_TO_WIDTH")
    }
  }


}
