package com.daily.events.calender.Model

import org.joda.time.DateTime

fun DateTime.seconds() = millis / 1000L
