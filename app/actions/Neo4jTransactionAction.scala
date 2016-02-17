package actions

import play.api.mvc._
import scala.concurrent.Future

/*
object Neo4jTransactionAction extends ActionBuilder[Request]  {
  override def composeAction[A](action: Action[A]) = new Neo4jTransactionAction(action)

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    block(request)
  }
}

case class Neo4jTransactionAction[A](action: Action[A]) extends Action[A] {

  def apply(request: Request[A]): Future[Result] = {
    val tx = db.beginTx
    try {
      val result = action(request)
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

  lazy val parser = action.parser
}
*/