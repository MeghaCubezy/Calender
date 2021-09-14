package com.daily.events.calender.Model

data class ListSection(
    val title: String,
    val code: String,
    val isToday: Boolean,
    val isPastSection: Boolean
) : ListItem()
