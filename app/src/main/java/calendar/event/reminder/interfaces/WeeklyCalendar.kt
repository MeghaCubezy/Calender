package calendar.event.reminder.interfaces

import calendar.event.reminder.Model.Event


interface WeeklyCalendar {
    fun updateWeeklyCalendar(events: ArrayList<Event>)
}
