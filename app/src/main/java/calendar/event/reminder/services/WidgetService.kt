package calendar.event.reminder.services

import android.content.Intent
import android.widget.RemoteViewsService
import calendar.event.reminder.Adapter.EventListWidgetAdapter

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = EventListWidgetAdapter(applicationContext)
}
