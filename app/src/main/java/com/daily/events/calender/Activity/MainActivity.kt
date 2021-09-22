package com.daily.events.calender.Activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.provider.CalendarContract
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.daily.events.calender.Extensions.*
import com.daily.events.calender.Fragment.EventFragment
import com.daily.events.calender.Fragment.Home.HomeFragment
import com.daily.events.calender.Fragment.MonthFragmentsHolder
import com.daily.events.calender.Fragment.NotificationFragment
import com.daily.events.calender.Fragment.SettingFragment
import com.daily.events.calender.Model.EventType
import com.daily.events.calender.R
import com.daily.events.calender.databinding.ActivityMainBinding
import com.daily.events.calender.helpers.DAY_CODE
import com.daily.events.calender.helpers.Formatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.calendar_item_account.view.*
import kotlinx.android.synthetic.main.calendar_item_calendar.view.*
import kotlinx.android.synthetic.main.dialog_select_calendars.view.*
import org.joda.time.DateTime
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private var selectAccountReceiver: SelectAccountReceiver? = null

    companion object {
        val CALDAV_REFRESH_DELAY = 3000L
        val calDAVRefreshHandler = Handler()
        var calDAVRefreshCallback: (() -> Unit)? = null

        lateinit var selectAccountBehaviour: BottomSheetBehavior<LinearLayout>
        var mainBinding: ActivityMainBinding? = null

        lateinit var activity: Activity

        fun getSyncedCalDAVCalendars() =
            activity.calDAVHelper.getCalDAVCalendars(activity.config.caldavSyncedCalendarIds, false)


        fun syncCalDAVCalendars(callback: () -> Unit) {
            calDAVRefreshCallback = callback
            ensureBackgroundThread {
                val uri = CalendarContract.Calendars.CONTENT_URI
                activity.contentResolver.unregisterContentObserver(calDAVSyncObserver)
                activity.contentResolver.registerContentObserver(uri, false, calDAVSyncObserver)
                activity.refreshCalDAVCalendars(activity.config.caldavSyncedCalendarIds, true)
            }
        }

        // caldav refresh content observer triggers multiple times in a row at updating, so call the callback only a few seconds after the (hopefully) last one
        private val calDAVSyncObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                if (!selfChange) {
                    calDAVRefreshHandler.removeCallbacksAndMessages(null)
                    calDAVRefreshHandler.postDelayed({
                        ensureBackgroundThread {
                            unregisterObserver()
                            calDAVRefreshCallback?.invoke()
                            calDAVRefreshCallback = null
                        }
                    }, CALDAV_REFRESH_DELAY)
                }
            }
        }

        fun unregisterObserver() {
            activity.contentResolver.unregisterContentObserver(calDAVSyncObserver)
        }

        private fun updateDefaultEventTypeText() {
            if (activity.config.defaultEventTypeId == -1L) {

            } else {
                ensureBackgroundThread {
                    val eventType =
                        activity.eventTypesDB.getEventTypeWithId(activity.config.defaultEventTypeId)
                    if (eventType != null) {
                        activity.config.lastUsedCaldavCalendarId = eventType.caldavCalendarId
                    } else {
                        activity.config.defaultEventTypeId = -1
                        updateDefaultEventTypeText()
                    }
                }
            }
        }
    }

    private var showCalDAVRefreshToast = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding =
            DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        mainBinding?.bottomnavigationbar?.setOnNavigationItemSelectedListener(this)

        activity = this@MainActivity

        val isGranted = EasyPermissions.hasPermissions(this@MainActivity, *perms)
        if (isGranted) {
            config.caldavSync = false
        }

        if (config.caldavSync) {
            refreshCalDAVCalendars(false)
        }

        selectAccountBehaviour =
            BottomSheetBehavior.from(llBottom)

        selectAccountBehaviour.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mainBinding?.hideBack?.beGone()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })


        selectAccountReceiver = SelectAccountReceiver()

        LocalBroadcastManager.getInstance(this@MainActivity).registerReceiver(
            selectAccountReceiver!!,
            IntentFilter("OPEN_ACCOUNT_SYNC")
        )

        config.isSundayFirst = false
    }

    private fun refreshCalDAVCalendars(showRefreshToast: Boolean) {
        showCalDAVRefreshToast = showRefreshToast
        if (showRefreshToast) {
            toast(R.string.refreshing)
        }

        syncCalDAVCalendars {
            calDAVHelper.refreshCalendars(true) {
                calDAVChanged()
            }
        }
    }

    private fun calDAVChanged() {
        if (showCalDAVRefreshToast) {
            toast(R.string.refreshing_complete)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (selectAccountBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
            selectAccountBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            mainBinding?.hideBack?.beGone()
        }
    }

    override fun permissionGranted() {
        homeFragment = HomeFragment()
        eventFragment = EventFragment()
        notificationFragment = NotificationFragment()
        settingFragment = SettingFragment()

        homeFragment?.let {
            supportFragmentManager.beginTransaction().replace(R.id.container, it)
                .commit()
        }
    }

    var homeFragment: HomeFragment? = null
    var eventFragment: EventFragment? = null
    var notificationFragment: NotificationFragment? = null
    var settingFragment: SettingFragment? = null

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                homeFragment?.let {
                    supportFragmentManager.beginTransaction().replace(R.id.container, it)
                        .commit()
                }
                return true
            }
            R.id.event -> {
                eventFragment?.let {
                    supportFragmentManager.beginTransaction().replace(R.id.container, it)
                        .commit()
                }
                return true
            }
            R.id.notification -> {
                notificationFragment?.let {
                    supportFragmentManager.beginTransaction().replace(R.id.container, it)
                        .commit()
                }
                return true
            }

            R.id.setting -> {
                settingFragment?.let {
                    supportFragmentManager.beginTransaction().replace(R.id.container, it)
                        .commit()
                }
                return true
            }
        }
        return false
    }

    fun openMonthFromYearly(dateTime: DateTime) {
        val fragment = MonthFragmentsHolder()
        val bundle = Bundle()
        bundle.putString(DAY_CODE, Formatter.getDayCodeFromDateTime(dateTime))
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.container1, fragment).commitNow()
        homeFragment?.monthChanges()

    }

    fun openDayFromMonthly(dateTime: DateTime) {
//        val fragment = DayFragmentsHolder()
//        currentFragments.add(fragment)
//        val bundle = Bundle()
//        bundle.putString(DAY_CODE, Formatter.getDayCodeFromDateTime(dateTime))
//        fragment.arguments = bundle
//        try {
//            supportFragmentManager.beginTransaction().add(R.id.fragments_holder, fragment).commitNow()
//            supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        } catch (e: Exception) {
//        }
    }

    fun toggleGoToTodayVisibility(beVisible: Boolean) {
//        shouldGoToTodayBeVisible = beVisible
//        if (goToTodayButton?.isVisible != beVisible) {
//            invalidateOptionsMenu()
//        }
    }

    private class SelectAccountReceiver : BroadcastReceiver() {


        override fun onReceive(context: Context, intent: Intent) {

            var prevAccount = ""

            selectAccountBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
            mainBinding?.hideBack?.beVisible()

            val ids = context.config.getSyncedCalendarIdsAsList()
            val calendars = context.calDAVHelper.getCalDAVCalendars("", true)
            mainBinding?.llMain?.dialogSelectCalendarsPlaceholder?.beVisibleIf(calendars.isEmpty())
            mainBinding?.llMain?.dialogSelectCalendarsHolder?.beVisibleIf(calendars.isNotEmpty())

            mainBinding?.llMain?.dialogSelectCalendarsHolder?.removeAllViews()

            val sorted = calendars.sortedWith(compareBy({ it.accountName }, { it.displayName }))
            sorted.forEach {
                if (prevAccount != it.accountName) {
                    prevAccount = it.accountName
                    addCalendarItem(false, it.accountName)
                }

                addCalendarItem(true, it.displayName, it.id, ids.contains(it.id))
            }

            mainBinding?.llMain?.dialogSubmit?.setOnClickListener {
                confirmSelection()
            }

            mainBinding?.llMain?.dialogCancel?.setOnClickListener {
                selectAccountBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                mainBinding?.hideBack?.beGone()
            }
        }

        fun addCalendarItem(
            isEvent: Boolean,
            text: String,
            tag: Int = 0,
            shouldCheck: Boolean = false
        ) {
            val layout =
                if (isEvent) R.layout.calendar_item_calendar else R.layout.calendar_item_account
            val calendarItem = activity.layoutInflater.inflate(
                layout,
                mainBinding?.llMain?.dialogSelectCalendarsHolder,
                false
            )

            if (isEvent) {
                calendarItem.calendar_item_calendar_switch.apply {
                    this.tag = tag
                    this.text = text
                    isChecked = shouldCheck
                    calendarItem.setOnClickListener {
                        toggle()
                    }
                }
            } else {
                calendarItem.calendar_item_account.text = text
            }

            mainBinding?.llMain?.dialogSelectCalendarsHolder?.addView(calendarItem)

        }

        private fun confirmSelection() {
            val oldCalendarIds = activity.config.getSyncedCalendarIdsAsList()
            val calendarIds = ArrayList<Int>()
            val childCnt = mainBinding?.llMain?.dialogSelectCalendarsHolder?.childCount
            for (i in 0..childCnt!!) {
                val child = mainBinding?.llMain?.dialogSelectCalendarsHolder?.getChildAt(i)
                if (child is RelativeLayout) {
                    val check = child.getChildAt(0)
                    if (check is SwitchCompat && check.isChecked) {
                        calendarIds.add(check.tag as Int)
                    }
                }
            }

            activity.config.caldavSyncedCalendarIds = TextUtils.join(",", calendarIds)

            val newCalendarIds = activity.config.getSyncedCalendarIdsAsList()

            activity.config.caldavSync = newCalendarIds.isNotEmpty()
            if (newCalendarIds.isNotEmpty()) {
//                toast(activity.resources.getString(R.string.syncing))
            }

            ensureBackgroundThread {
                if (newCalendarIds.isNotEmpty()) {
                    val existingEventTypeNames = activity.eventsHelper.getEventTypesSync().map {
                        it.getDisplayTitle()
                            .lowercase(Locale.getDefault())
                    } as ArrayList<String>
                    getSyncedCalDAVCalendars().forEach {
                        val calendarTitle = it.getFullTitle()
                        if (!existingEventTypeNames.contains(calendarTitle.lowercase(Locale.getDefault()))) {
                            val eventType = EventType(
                                null,
                                it.displayName,
                                it.color,
                                it.id,
                                it.displayName,
                                it.accountName
                            )
                            existingEventTypeNames.add(calendarTitle.lowercase(Locale.getDefault()))
                            activity.eventsHelper.insertOrUpdateEventType(activity, eventType)
                        }
                    }

                    syncCalDAVCalendars {
                        activity.calDAVHelper.refreshCalendars(true) {
                            if (newCalendarIds.isNotEmpty()) {
//                                toast(activity.resources.getString(R.string.synchronization_completed))
                            }
                        }
                    }
                }

                val removedCalendarIds = oldCalendarIds.filter { !newCalendarIds.contains(it) }
                removedCalendarIds.forEach {
                    activity.calDAVHelper.deleteCalDAVCalendarEvents(it.toLong())
                    activity.eventsHelper.getEventTypeWithCalDAVCalendarId(it)?.apply {
                        activity.eventsHelper.deleteEventTypes(arrayListOf(this), true)
                    }
                }

                activity.eventTypesDB.deleteEventTypesWithCalendarId(removedCalendarIds)
                updateDefaultEventTypeText()
            }
            selectAccountBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            mainBinding?.hideBack?.beGone()
        }
    }
}