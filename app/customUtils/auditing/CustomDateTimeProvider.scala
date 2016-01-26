package customUtils.auditing

import java.util.{Calendar, UUID}
import org.springframework.data.auditing.{DateTimeProvider, CurrentDateTimeProvider}

class CustomDateTimeProvider extends DateTimeProvider {

  def getNow: Calendar = {
    CurrentDateTimeProvider.INSTANCE.getNow
  }
}
