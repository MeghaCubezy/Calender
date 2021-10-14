package calendar.event.reminder.Extensions

import calendar.event.reminder.helpers.MONTH
import calendar.event.reminder.helpers.WEEK
import calendar.event.reminder.helpers.YEAR

fun Int.isXWeeklyRepetition() = this != 0 && this % WEEK == 0

fun Int.isXMonthlyRepetition() = this != 0 && this % MONTH == 0

fun Int.isXYearlyRepetition() = this != 0 && this % YEAR == 0
