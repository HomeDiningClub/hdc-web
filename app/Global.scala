import play.Application
import play.GlobalSettings
import play.Logger
import com.tinkerpop.blueprints.Vertex
import com.wingnest.play2.frames.GraphDB
import Global._
//remove if not needed
import scala.collection.JavaConversions._

// Java2Scala
// http://javatoscala.com/

object Global {

  private var init: Boolean = false
}

class Global extends GlobalSettings {

  override def  onStart(app: Application) {
    try {
      if (!init) {
        GraphDB.createKeyIndex("className", classOf[Vertex])
        init = true
      }
    } catch {
      case e: java.lang.UnsupportedOperationException => Logger.info(e.getMessage)
    }
  }
}