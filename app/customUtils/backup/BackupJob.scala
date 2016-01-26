package customUtils.backup

import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.Logger
import java.util.Calendar
import java.text.SimpleDateFormat
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object BackupJob {

  def startJob() = {
    // Every 12 hours store data from the register to file
    // Only in production
    if(play.api.Play.isProd(play.api.Play.current)) {
      Akka.system.scheduler.schedule(12.hour, 12.hour) {
        Logger.info("Backup started ... ")
        customUtils.backup.BackupData.makeFullBackup()
        val today = Calendar.getInstance().getTime()
        val dateFormat = new SimpleDateFormat("HH:mm:ss.SS")
        Logger.info("Backup done : " + dateFormat.format(today))
      }
    }
  }

}
