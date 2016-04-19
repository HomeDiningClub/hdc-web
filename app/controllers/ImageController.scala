package controllers

import _root_.java.util.UUID
import javax.inject.{Named, Inject}
import enums.{FileTypeEnums, RoleEnums}
import models.viewmodels.ImageData
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.libs.json.Writes._
import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import org.springframework.beans.factory.annotation.Autowired
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.{NodeEntityService, ContentFileService}
import customUtils.Helpers
import customUtils.authorization.WithRole
import customUtils.scalr.api.Resizer
import models.UserCredential
import customUtils.security.SecureSocialRuntimeEnvironment

class ImageController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                 val messagesApi: MessagesApi,
                                 implicit val nodeEntityService: NodeEntityService,
                                 val contentFileService: ContentFileService) extends Controller with SecureSocial with I18nSupport {
/*
  @Autowired
  private var contentFileService: ContentFileService = _
*/

  implicit val imageWrites = Json.writes[ImageData]

  private val faultyImageRequestAction: Action[AnyContent] = Action(Ok(""))

  // Returns JSON
  def listImages(selected: String = "") = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

    // Remove empty entry if string is empty
    val splitSelected = selected.split(',').toBuffer
    if(splitSelected.length == 1 && splitSelected.head.trim == ""){
      splitSelected.remove(0)
    }
    val splitSelectedArray = splitSelected.toArray

    val images = contentFileService.getImagesForUser(request.user.objectId).map(
      s => new ImageData(
        Some(s.objectId.toString),
        s.name,
        routes.ImageController.imgChooserThumb(s.getStoreId).url,
        if(splitSelectedArray.isEmpty){ false }else{ splitSelected.exists(item => UUID.fromString(item) == s.objectId) }
      )
    )
    Ok(Json.toJson(images.map(s => Json.toJson(s))))

  }

  // Returns JSON
  def previewImages(selected: String = "") = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>

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


  def uploadImageSubmit() = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.multipartFormData) { implicit request =>

    request.body.file("files").map {
      file =>
        // We have to do this here, since otherwise we have a lock on the file
        val fileName = file.filename
        val newFile = contentFileService.createTemporaryFile(fileName)
        val contentType = file.contentType
        file.ref.moveTo(newFile, true)

        contentFileService.uploadFile(newFile, fileName, contentType, request.user.objectId, FileTypeEnums.IMAGE) match {
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

  // Image resources
  // General functions
  // Mode: AUTOMATIC, FIT_EXACT, FIT_TO_WIDTH, FIT_TO_HEIGHT, CROP
  def at(fileUid: String): Action[AnyContent] = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.res.ResAssets.at(id)
    }
  }

  def auto(fileUid: String, width: Int, height: Int): Action[AnyContent] = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, width, height)
    }
  }

  def crop(fileUid: String, width: Int, height: Int) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, width, height, mode = Resizer.Mode.CROP.toString)
    }
  }

  // Image chooser
  def imgChooserThumb(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 100, 100, mode = Resizer.Mode.CROP.toString)
    }
  }

  // User avatar
  def userMini(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 30, 30, mode = Resizer.Mode.CROP.toString)
    }
  }
  def userThumb(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 100, 100, mode = Resizer.Mode.CROP.toString)
    }
  }

  // UserProfile
  def profileThumb(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 150, 100, mode = Resizer.Mode.CROP.toString)
    }
  }
  def profileBox(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 263, 160, mode = Resizer.Mode.FIT_TO_WIDTH.toString)
    }
  }
  def profileNormal(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 460, 305, mode = Resizer.Mode.FIT_TO_WIDTH.toString)
    }
  }
  def profileBig(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 800, 600, mode = Resizer.Mode.FIT_TO_WIDTH.toString)
    }
  }

  // Blog
  def blogNormal(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.res.ResAssets.at(id)
    }
  }


  // Recipe
  def recipeThumb(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 150, 100, mode = Resizer.Mode.CROP.toString)
    }
  }
  def recipeBox(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 275, 160, mode = Resizer.Mode.FIT_TO_WIDTH.toString)
    }
  }
  def recipeNormal(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 460, 345, mode = Resizer.Mode.FIT_TO_WIDTH.toString)
    }
  }

  // Event
  def eventThumb(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 150, 100, mode = Resizer.Mode.CROP.toString)
    }
  }
  def eventBox(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 275, 160, mode = Resizer.Mode.FIT_TO_WIDTH.toString)
    }
  }
  def eventNormal(fileUid: String) = {
    fileUid match {
      case "null" =>
        faultyImageRequestAction
      case id: String =>
        customUtils.scalr.ScalrResAssets.at(id, 460, 345, mode = Resizer.Mode.FIT_TO_WIDTH.toString)
    }
  }

}
