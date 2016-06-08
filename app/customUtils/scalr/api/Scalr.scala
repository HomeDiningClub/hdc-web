package customUtils.scalr.api

import play.api._
import java.io.{FileInputStream, OutputStream, File}
import org.apache.commons.io.FilenameUtils
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import util.Random
import customUtils.res.api.Res

import play.api.Play.current

object Scalr {

  lazy val configuration = Play.configuration.getConfig("scalr").getOrElse(Configuration.empty)

  /**
   * Creates and caches image in local cache directory
   * @param path The path to where the images are stored
   * @param file The filePath relative to the path variable (the same as the play Assets Controller)
   * @param width The width of the frame that we want the image to fit within
   * @param height The height of the frame that we want the image to fit within
   * @param mode Any of the Scale modes such as AUTOMATIC, FIT_TO_WIDTH, FIT_TO_HEIGHT
   * @param method Any of the Scalr Methods, The standard is the highest possible
   * @return a File if everything when well
   */
  def get(path: String, file: String, width: Int, height: Int, mode: Resizer.Mode = Resizer.Mode.AUTOMATIC, method: Resizer.Method = Resizer.Method.ULTRA_QUALITY): Option[File] = {

    val resourceName = Option(path + "/" + file).map(name => if (name.startsWith("/")) name else ("/" + name)).get
    val resourceFile = Play.getFile(resourceName)

    if (resourceFile.isDirectory) {
      None
    } else {
      val cachePath = configuration.getString("cachedir").getOrElse("tmp/scalrcache")
      val cachedImage = Res.fileWithMeta(
        filePath = FilenameUtils.concat(cachePath, file),
        meta = Seq(width.toString, height.toString, mode.toString)
      )

      cachedImage.map { cachedImage =>
        Some(cachedImage)
      }.getOrElse {
        val resizedImage = resize(resourceFile, width, height, mode, method)
        val resizedFilePath = Res.saveWithMeta(
          resizedImage,
          filePath = FilenameUtils.concat(cachePath, file),
          meta = Seq(width.toString, height.toString, mode.toString)
        )
        Play.getExistingFile(resizedFilePath)
      }
    }
  }

  /**
   *
   * @param fileuid The unique play-res file identifier
   * @param source The play-res source name
   * @param width The width of the frame that we want the image to fit within
   * @param height The height of the frame that we want the image to fit within
   * @param mode Any of the Scale modes such as AUTOMATIC, FIT_TO_WIDTH, FIT_TO_HEIGHT
   * @param method Any of the Scalr Methods, The standard is the highest possible
   * @return a File if everything when well
   */
  def getRes(fileuid: String, source: String = "default", width: Int, height: Int, mode: Resizer.Mode = Resizer.Mode.AUTOMATIC, method: Resizer.Method = Resizer.Method.ULTRA_QUALITY): Option[File] = {

    Res.get(fileuid, source).flatMap { res =>

      val cacheSource = configuration.getString("cache").getOrElse("scalrcache")

      val cachedImage = Res.get(
        fileuid = res.getName,
        meta = Seq(width.toString, height.toString, mode.toString),
        source = cacheSource
      )

      cachedImage.map { cachedImage =>
        Some(cachedImage)
      }.getOrElse {
        val resizedImage = resize(res, width, height, mode, method)
        val fileUID = Res.put(
          resizedImage,
          cacheSource,
          filename = Some(res.getName),
          meta = Seq(width.toString, height.toString, mode.toString)
        )
        Res.get(fileUID, cacheSource)
      }
    }
  }

  /**
   * Resizes an image and stores it in a temp file that we then can move into a cache
   * @param file The file to resize
   * @param width The width of the frame that we want the image to fit within
   * @param height The height of the frame that we want the image to fit within
   * @param mode Any of the Scale modes such as AUTOMATIC, FIT_TO_WIDTH, FIT_TO_HEIGHT
   * @param method Any of the Scalr Methods, The standard is the highest possible
   * @return A resized file
   */
  def resize(file: File, width: Int, height: Int, mode: Resizer.Mode = Resizer.Mode.AUTOMATIC, method: Resizer.Method = Resizer.Method.ULTRA_QUALITY): File = {
    val image = ImageIO.read(file)
    val resized = Resizer.resize(image, method, mode, width, height, Resizer.OP_ANTIALIAS)
    //val resized = Resizer.resize(image, method, mode, width, height)
    val ext = guessImageFormat(file)
    val tmp = File.createTempFile(Random.nextString(20), ext)
    ImageIO.write(resized, ext.toUpperCase, tmp)
    tmp
  }

  /**
   * Resizes an image and returns a stream that we can pipe directly to the browser
   * @param file The file to resize
   * @param width The width of the frame that we want the image to fit within
   * @param height The height of the frame that we want the image to fit within
   * @param mode Any of the Scale modes such as AUTOMATIC, FIT_TO_WIDTH, FIT_TO_HEIGHT
   * @param method Any of the Scalr Methods, The standard is the highest possible
   * @return A stream that we can pipe directly to the browser
   */
  def resizeToStream(file: File, width: Int, height: Int, mode: Resizer.Mode = Resizer.Mode.AUTOMATIC, method: Resizer.Method = Resizer.Method.ULTRA_QUALITY): OutputStream = {
    val image = ImageIO.read(file)
    val resized = Resizer.resize(image, method, mode, width, height, Resizer.OP_ANTIALIAS)
    //val resized = Resizer.resize(image, method, mode, width, height)
    val ext = guessImageFormat(file)
    val out: OutputStream = null
    ImageIO.write(resized, ext.toUpperCase, out)
    out
  }

  /**
   * Detect the image format "jpg" or "png"
   * Done by reading the first bytes of the file to check if it correspond to a file format "signature"
   * @see http://www.garykessler.net/library/file_sigs.html
   * @param file The File we want to analyse
   * @return A String representing the image format "jpg" or "png"
   */
  def guessImageFormat(file: File): String = {
    val is = new FileInputStream(file)
    val firstBytes = new Array[Byte](2)
    is.read(firstBytes)
    is.close
    // A JPEG file always start with bytes FF D8
    if (firstBytes(0) == 0xFF.asInstanceOf[Byte] && firstBytes(1) == 0xD8.asInstanceOf[Byte])
      "jpg"
    else
      "png"
  }
}