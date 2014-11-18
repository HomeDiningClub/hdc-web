package utils.backup

import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar


/*
import akka.actor._
import scala.concurrent.duration._

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
*/
object BackupRobot {

  var v : Int = 0


  def main(args: Array[String]) {
    println("Hello, world!")

    val toMegaByte : Long =  (1024 * 1024)

    // todo use paramter value
    var dir : File  = new File("C:\\UTV\\KATALOG")
    var freeSpace   : Long = dir.getFreeSpace / toMegaByte
    var usableSpace : Long = dir.getUsableSpace / toMegaByte

    val today = Calendar.getInstance().getTime()
    val dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS")
    val nowString = dateFormat.format(today)
    println(nowString)
    println("FREE_SPACE : " + freeSpace)
    var dirDir : File  = new File("C:\\UTV\\KATALOG\\" + nowString)
    var successful : Boolean = dirDir.mkdir()
    if(successful) {
      println("OK")
    } else {
      println("FEL")
    }

  }

  /*
  Akka.system.scheduler.scheduleOnce(10 seconds) {
    println("Test .... :  " + v.toString())
    v = v + 1
  }
  */

}
