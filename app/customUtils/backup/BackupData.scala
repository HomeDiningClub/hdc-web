package customUtils.backup

import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import org.neo4j.backup.OnlineBackup
import play.api
import play.api.Logger

object BackupData {

  var isRunning : Boolean = false

  def makeFullBackup()
  {

    if(!isRunning) {

      isRunning = true

      // backup bath
      // server ip or name

      val BACKUP_PATH: Option[String] = api.Play.current.configuration.getString("backup.dir")
      // var backupPath              : File    = new File(BACKUP_PATH.getOrElse(""))
      val backupPath: File = getFolder(BACKUP_PATH.getOrElse(""))
      val DATABASE_SERVER_NAME_IP: Option[String] = api.Play.current.configuration.getString("backup.servernameorip")
      var doNothing: Boolean = false


      if (BACKUP_PATH.isEmpty) {
        Logger.debug("Error BACKUP_PATH missing!")
        doNothing = true
      }

      if (DATABASE_SERVER_NAME_IP.isEmpty) {
        Logger.debug("Error DATABAS_SERVER_NAME_IP missing!")
        doNothing = true
      }

      // 20 Mbyte free space
      if(!freeSpace(BACKUP_PATH.getOrElse(""), 20)) {
        Logger.debug("Error No memory left to make a backup")
        doNothing = true
      }


      if (!doNothing) {
        try {
          val backup = OnlineBackup.from(DATABASE_SERVER_NAME_IP.getOrElse("localhost"))

          //backup.full(backupPath.getPath())
          //backup.backup(backupPath.getPath())
          backup.backup(new File(backupPath.getPath))

        } catch {
          case e: Exception => {
            println("Error : " + e.getMessage)
          }
        }
        finally {
          // your scala code here, such as to close a database connection
          isRunning = false
        }


      }
    }
  }



  def freeSpace(folder: String, megaByte : Integer) : Boolean = {

    val toMegaByte : Long =  1024 * 1024

    // Todo: Use parameter value
    val dir : File  = new File(folder)

    //val freeSpace   : Long = dir.getFreeSpace / toMegaByte
    val usableSpace : Long = dir.getUsableSpace / toMegaByte

    usableSpace > megaByte
  }




  def getFolder(backupPath : String) : File = {

    val toMegaByte : Long =  1024 * 1024

    // Todo: Use paramter value
    val dir : File  = new File(backupPath)

    //val freeSpace   : Long = dir.getFreeSpace / toMegaByte
    val usableSpace : Long = dir.getUsableSpace / toMegaByte

    val today = Calendar.getInstance().getTime()
    val dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS")
    val nowString = dateFormat.format(today)


    val dirDir : File  = new File(backupPath + "\\" + nowString)
    val successful : Boolean = dirDir.mkdir()

   dirDir
  }

}
