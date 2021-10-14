package calendar.event.reminder.Extensions

import calendar.event.reminder.Model.Event
import calendar.event.reminder.helpers.Formatter

fun Long.isTsOnProperDay(event: Event): Boolean {
    val dateTime = Formatter.getDateTimeFromTS(this)
    val power = Math.pow(2.0, (dateTime.dayOfWeek - 1).toDouble()).toInt()
    return event.repeatRule and power != 0
}
