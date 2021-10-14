package calendar.event.reminder.interfaces

import android.util.SparseArray
import calendar.event.reminder.Model.DayYearly
import java.util.*

interface YearlyCalendar {
    fun updateYearlyCalendar(events: SparseArray<ArrayList<DayYearly>>, hashCode: Int)
}
