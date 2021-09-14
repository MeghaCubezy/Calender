package com.daily.events.calender.Interfaces

import org.joda.time.DateTime

interface NavigationListener {
    fun goLeft()

    fun goRight()

    fun goToDateTime(dateTime: DateTime)
}
