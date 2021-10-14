package calendar.event.reminder.Extensions

import org.joda.time.DateTime

fun DateTime.seconds() = millis / 1000L
