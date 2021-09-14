package com.daily.events.calender.services

import android.content.Intent
import android.widget.RemoteViewsService

class WidgetServiceEmpty : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = EventListWidgetAdapterEmpty(applicationContext)
}
