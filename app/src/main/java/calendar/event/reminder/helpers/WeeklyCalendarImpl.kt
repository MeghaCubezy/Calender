package calendar.event.reminder.helpers

import android.content.Context
import calendar.event.reminder.Extensions.eventsHelper
import calendar.event.reminder.Model.Event
import calendar.event.reminder.interfaces.WeeklyCalendar
import com.simplemobiletools.commons.helpers.DAY_SECONDS
import com.simplemobiletools.commons.helpers.WEEK_SECONDS
import java.util.*

class WeeklyCalendarImpl(val callback: WeeklyCalendar, val context: Context) {
    var mEvents = ArrayList<Event>()

    fun updateWeeklyCalendar(weekStartTS: Long) {
        val endTS = weekStartTS + 2 * WEEK_SECONDS
        context.eventsHelper.getEvents(weekStartTS - DAY_SECONDS, endTS) {
            mEvents = it
            callback.updateWeeklyCalendar(it)
        }
    }
}
