package customUtils

import java.net.URLEncoder
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

import play.api.mvc.{PathBindable, QueryStringBindable}
import java.util.UUID

object Binders {
  implicit def uuidPathBinder = new PathBindable[UUID] {
    override def bind(key: String, value: String): Either[String, UUID] = {
      Right(UUID.fromString(value))
    }
    override def unbind(key: String, id: UUID): String = {
      id.toString
    }
  }

  implicit def LocalDateQueryBinder = new QueryStringBindable[LocalDate] {

    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate] with Product with Serializable] = {
      params.get(key).flatMap(_.headOption).map { value =>
        try {
          val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
          Right(LocalDate.parse(value, formatter))
        } catch {
          case e: Exception => Left("Cannot parse parameter " + key + " as LocalDateTime: " + e.getMessage)
        }
      }
    }

    def unbind(key: String, value: LocalDate): String = {
      key + "=" + URLEncoder.encode(value.toString, "utf-8")
    }
  }

  implicit def LocalDateTimeQueryBinder = new QueryStringBindable[LocalDateTime] {

    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDateTime] with Product with Serializable] = {
      params.get(key).flatMap(_.headOption).map { value =>
        try {
          val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
          Right(LocalDateTime.parse(value, formatter))
        } catch {
          case e: Exception => Left("Cannot parse parameter " + key + " as LocalDateTime: " + e.getMessage)
        }
      }
    }

    def unbind(key: String, value: LocalDateTime): String = {
      key + "=" + URLEncoder.encode(value.toString, "utf-8")
    }
  }


}