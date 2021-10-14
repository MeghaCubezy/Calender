package calendar.event.reminder.services

import android.content.Intent
import android.widget.RemoteViewsService
import calendar.event.reminder.Adapter.EventListWidgetAdapterEmpty

class WidgetServiceEmpty : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = EventListWidgetAdapterEmpty(applicationContext)
}
