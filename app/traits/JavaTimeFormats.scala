package traits

import play.api.data._
import play.api.data.validation._
import play.api.data.Forms._
import play.api.data.format.{Formats => PlayFormats}
import play.api.data.format.Formatter
import play.api.data.format.Formats.stringFormat

trait JavaTimeFormats {

  import java.time.{LocalDate, LocalDateTime, ZoneId}
  import java.time.format.DateTimeFormatter

  protected def parsing[T](parse: String => T, errMsg: String, errArgs: Seq[Any])(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
    PlayFormats.stringFormat.bind(key, data).right.flatMap { s =>
      scala.util.control.Exception.allCatch[T]
        .either(parse(s))
        .left.map(e => Seq(FormError(key, errMsg, errArgs)))
    }
  }

  /**
   * Formatter for the `java.time.LocalDate` type.
   *
   * @param pattern a date pattern as specified in `java.time.format.DateTimeFormatter`.
   */
  def localDateFormat(formatter: DateTimeFormatter): Formatter[LocalDate] = new Formatter[LocalDate] {
    def jodaLocalDateParse(data: String) = LocalDate.parse(data, formatter)
    override val format = Some(("format.date", Seq(formatter.toString)))
    def bind(key: String, data: Map[String, String]) = parsing(jodaLocalDateParse, "error.date", Nil)(key, data)
    def unbind(key: String, value: LocalDate) = Map(key -> value.format(formatter))
  }

  /**
   * Default formatter for `java.time.LocalDate` type with pattern `yyyy-MM-dd` as `java.time.format.DateTimeFormatter.ISO_LOCAL_DATE`.
   */
  implicit val localDateFormat: Formatter[LocalDate] = localDateFormat(DateTimeFormatter.ISO_LOCAL_DATE)


  /**
   * Formatter for the `java.time.LocalDateTime` type.
   *
   * @param pattern a date pattern as specified in `java.time.format.DateTimeFormatter`.
   */
  def localDateTimeFormat(formatter: DateTimeFormatter): Formatter[LocalDateTime] = new Formatter[LocalDateTime] {
    def jodaLocalDateParse(data: String) = LocalDateTime.parse(data, formatter)
    override val format = Some(("format.date", Seq(formatter.toString)))
    def bind(key: String, data: Map[String, String]) = parsing(jodaLocalDateParse, "error.date", Nil)(key, data)
    def unbind(key: String, value: LocalDateTime) = Map(key -> value.format(formatter))
  }

  /**
   * Default formatter for `java.time.LocalDateTime` type with pattern `yyyy-MM-ddTHH:mm:ss` as `java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME`.
   */
  implicit val localDateTimeFormat: Formatter[LocalDateTime] = localDateTimeFormat(DateTimeFormatter.ISO_LOCAL_DATE_TIME)


  /**
   * Formatter for the `java.time.ZoneId` type.
   */
  implicit val zoneIdFormat: Formatter[ZoneId] = new Formatter[ZoneId] {
    def parse(data: String) = ZoneId.of(data)
    override val format = Some(("format.zoneId", Nil))
    def bind(key: String, data: Map[String, String]) = parsing(parse, "error.zoneId", Nil)(key, data)
    def unbind(key: String, value: ZoneId) = Map(key -> value.toString)
  }

}
