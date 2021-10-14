package calendar.event.reminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import calendar.event.reminder.Extensions.config
import calendar.event.reminder.Extensions.recheckCalDAVCalendars
import calendar.event.reminder.Extensions.refreshCalDAVCalendars
import calendar.event.reminder.Extensions.updateWidgets

class CalDAVSyncReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (context.config.caldavSync) {
            context.refreshCalDAVCalendars(context.config.caldavSyncedCalendarIds, false)
        }

        context.recheckCalDAVCalendars {
            context.updateWidgets()
        }
    }
}
