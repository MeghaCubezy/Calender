package calendar.event.reminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import calendar.event.reminder.Extensions.notifyRunningEvents
import calendar.event.reminder.Extensions.recheckCalDAVCalendars
import calendar.event.reminder.Extensions.scheduleAllEvents
import com.simplemobiletools.commons.helpers.ensureBackgroundThread

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ensureBackgroundThread {
            context.apply {
                scheduleAllEvents()
                notifyRunningEvents()
                recheckCalDAVCalendars {}
            }
        }
    }
}
