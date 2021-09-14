package com.daily.events.calender.Interfaces

import com.daily.events.calender.Model.Event


interface WeeklyCalendar {
    fun updateWeeklyCalendar(events: ArrayList<Event>)
}
