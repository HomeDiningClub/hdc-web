package utils.backup

import java.io.File

object BackupData {


  def backup_data()
  {
    // backup bath
    // server ip or name

    val BACKUP_PATH             : String = "D:/neo4j-enterprise-1.9.4/data/backup/"
    var backupPath              : File    = new File(BACKUP_PATH)
    val DATABAS_SERVER_NAME_IP  : String  = "127.0.0.1"


   //   val backup = OnlineBackup.from( DATABAS_SERVER_NAME_IP )
   // backup.full( backupPath.getPath() );


  }

}
