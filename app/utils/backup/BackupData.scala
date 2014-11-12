package utils.backup

import java.io.File

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

      var BACKUP_PATH             : Option[String] = api.Play.current.configuration.getString("backup.dir")
      var backupPath              : File    = new File(BACKUP_PATH.getOrElse(""))
      val DATABAS_SERVER_NAME_IP  : Option[String]  = api.Play.current.configuration.getString("backup.servernameorip")

      try
      {

      //  val backup = OnlineBackup.from( DATABAS_SERVER_NAME_IP.getOrElse("localhost") )

      //  backup.full( backupPath.getPath() )

      } catch {
        case e: Exception => {
          println(e.getMessage)
        }
      }

    }

  }

}
