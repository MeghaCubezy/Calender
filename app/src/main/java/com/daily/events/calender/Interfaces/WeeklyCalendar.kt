package com.daily.events.calender.interfaces

import com.daily.events.calender.models.Event

interface WeeklyCalendar {
    fun updateWeeklyCalendar(events: ArrayList<Event>)
}
