package utils.backup

import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

import org.neo4j.backup.OnlineBackup
import play.api


object BackupData {

  var isRunning : Boolean = false


  def makeFullBackup()
  {

    if(isRunning == false) {

      isRunning = true

      // backup bath
      // server ip or name

      var BACKUP_PATH: Option[String] = api.Play.current.configuration.getString("backup.dir")
      // var backupPath              : File    = new File(BACKUP_PATH.getOrElse(""))
      var backupPath: File = getFolder(BACKUP_PATH.getOrElse(""))
      val DATABAS_SERVER_NAME_IP: Option[String] = api.Play.current.configuration.getString("backup.servernameorip")
      var doNothing: Boolean = false


      if (BACKUP_PATH.equals("")) {
        println("Error BACUP_PATH missing!")
        doNothing = true
      }

      if (DATABAS_SERVER_NAME_IP.equals("")) {
        println("Error DATABAS_SERVER_NAME_IP missing!")
        doNothing = true
      }

      // 20 Mbyte free space
      if(!freeSpace(BACKUP_PATH.getOrElse(""), 20)) {
        println("Error No memory left to make a backup")
        doNothing = true
      }


      if (!doNothing) {
        try {

          val backup = OnlineBackup.from(DATABAS_SERVER_NAME_IP.getOrElse("localhost"))

          backup.full(backupPath.getPath())


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

    val toMegaByte : Long =  (1024 * 1024)

    // todo use paramter value
    var dir : File  = new File(folder)

    var freeSpace   : Long = dir.getFreeSpace / toMegaByte
    var usableSpace : Long = dir.getUsableSpace / toMegaByte

    usableSpace > megaByte
  }




  def getFolder(backupPath : String) : File = {

    val toMegaByte : Long =  (1024 * 1024)

    // todo use paramter value
    var dir : File  = new File(backupPath)

    var freeSpace   : Long = dir.getFreeSpace / toMegaByte
    var usableSpace : Long = dir.getUsableSpace / toMegaByte

    val today = Calendar.getInstance().getTime()
    val dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS")
    val nowString = dateFormat.format(today)


    var dirDir : File  = new File(backupPath + "\\" + nowString)
    var successful : Boolean = dirDir.mkdir()

   dirDir
  }

}
