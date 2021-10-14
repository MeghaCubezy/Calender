package calendar.event.reminder.models

import calendar.event.reminder.Model.ListItem

data class ListSection(
    val title: String,
    val code: String,
    val isToday: Boolean,
    val isPastSection: Boolean
) : ListItem()
