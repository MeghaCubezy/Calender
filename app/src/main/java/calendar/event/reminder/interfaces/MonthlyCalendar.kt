package calendar.event.reminder.interfaces

import android.content.Context
import calendar.event.reminder.Model.DayMonthly
import org.joda.time.DateTime

interface MonthlyCalendar {
    fun updateMonthlyCalendar(
        context: Context,
        month: String,
        days: ArrayList<DayMonthly>,
        checkedEvents: Boolean,
        currTargetDate: DateTime
    )
}
