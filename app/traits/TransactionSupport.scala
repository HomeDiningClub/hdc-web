package traits

import org.neo4j.graphdb.GraphDatabaseService
import org.springframework.data.neo4j.support.Neo4jTemplate

trait TransactionSupport {

  val template: Neo4jTemplate

  protected def withTransaction[A <: Any](template: Neo4jTemplate)(dbOp: => A): A = {
    val db = template.getGraphDatabase
    val tx = db.beginTx
    try {
      val result = dbOp
      tx.success()
      result
    }
    catch {
      case t: Throwable => {
        tx.failure()
        throw t
      }
    } finally {
      tx.close()
    }
  }

}
