package customUtils.formhelpers

import java.time.{LocalTime, LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import play.api.data.format.Formats.stringFormat
import play.api.data.format.Formatter
import play.api.data.FormError

object Formats extends play.data.format.Formats {

  /**
   * Formatter for the `org.joda.time.LocalTime` type.
   * @param pattern a date pattern as specified in `org.joda.time.format.DateTimeFormat`.
   */
  def jodaLocalTimeFormat(pattern: String): Formatter[org.joda.time.LocalTime] = new Formatter[org.joda.time.LocalTime] {
    import org.joda.time.LocalTime
    override val format = Some(("format.date", Seq(pattern)))

    def bind(key: String, data: Map[String, String]) = {
      stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[LocalTime]
          .either(LocalTime.parse(s, org.joda.time.format.DateTimeFormat.forPattern(pattern)))
          .left.map(e => Seq(FormError(key, "error.time", Nil)))
      }
    }

    def unbind(key: String, value: LocalTime) = Map(key -> value.toString(pattern))
  }

  /**
   * Default formatter for `org.joda.time.LocalTime` type with pattern `HH:mm:ss`.
   */
  implicit val jodaLocalTimeFormat: Formatter[org.joda.time.LocalTime] = jodaLocalTimeFormat("HH:mm:ss")




  /**
   * Formatter for the `java.time.LocalDateTime` type.
   *
   * @param formatter a date pattern as specified in `java.time.format.DateTimeFormatter`.
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
   * Formatter for the `java.time.LocalDate` type.
   *
   * @param formatter a date pattern as specified in `java.time.format.DateTimeFormatter`.
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
   * Formatter for the `java.time.LocalTime` type.
   *
   * @param formatter a time pattern as specified in `java.time.format.LocalTime`.
   */
  def localTimeFormat(formatter: DateTimeFormatter): Formatter[LocalTime] = new Formatter[LocalTime] {
    def localTimeParse(data: String) = LocalTime.parse(data, formatter)
    override val format = Some(("format.time", Seq(formatter.toString)))
    def bind(key: String, data: Map[String, String]) = parsing(localTimeParse, "error.time", Nil)(key, data)
    def unbind(key: String, value: LocalTime) = Map(key -> value.format(formatter))
  }

  /**
   * Default formatter for `java.time.LocalDateTime` type with pattern `HH:mm:ss` as `java.time.format.TimeFormatter.ISO_LOCAL_TIME`.
   */
  implicit val localTimeFormat: Formatter[LocalTime] = localTimeFormat(DateTimeFormatter.ISO_LOCAL_TIME)





  /**
   * Helper for formatters binders
   * @param parse Function parsing a String value into a T value, throwing an exception in case of failure
   * @param errMsg Error to set in case of parsing failure
   * @param key Key name of the field to parse
   * @param data Field data
   */
  protected def parsing[T](parse: String => T, errMsg: String, errArgs: Seq[Any])(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
    play.api.data.format.Formats.stringFormat.bind(key, data).right.flatMap { s =>
      scala.util.control.Exception.allCatch[T]
        .either(parse(s))
        .left.map(e => Seq(FormError(key, errMsg, errArgs)))
    }
  }

}