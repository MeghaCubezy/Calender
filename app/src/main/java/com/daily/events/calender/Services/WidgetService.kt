package com.simplemobiletools.calendar.pro.services

import android.content.Intent
import android.widget.RemoteViewsService
import com.daily.events.calender.Adapter.EventListWidgetAdapter

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = EventListWidgetAdapter(applicationContext)
}
