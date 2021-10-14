package calendar.event.reminder.services

import android.app.IntentService
import android.content.Intent
import calendar.event.reminder.Extensions.config
import calendar.event.reminder.Extensions.eventsDB
import calendar.event.reminder.Extensions.rescheduleReminder
import calendar.event.reminder.helpers.EVENT_ID

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val eventId = intent.getLongExtra(EVENT_ID, 0L)
            val event = eventsDB.getEventWithId(eventId)
            rescheduleReminder(event, config.snoozeTime)
        }
    }
}
