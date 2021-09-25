package com.daily.events.calender.Activity

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.*
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.loader.content.CursorLoader
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.daily.events.calender.Extensions.*
import com.daily.events.calender.Fragment.EventFragment
import com.daily.events.calender.Fragment.Home.HomeFragment
import com.daily.events.calender.Fragment.MonthFragmentsHolder
import com.daily.events.calender.Fragment.NotificationFragment
import com.daily.events.calender.Fragment.SettingFragment
import com.daily.events.calender.Model.Event
import com.daily.events.calender.Model.EventType
import com.daily.events.calender.R
import com.daily.events.calender.databinding.ActivityMainBinding
import com.daily.events.calender.dialogs.SetRemindersDialog
import com.daily.events.calender.helpers.*
import com.daily.events.calender.helpers.Formatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.MyContactsContentProvider
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.getDateFormats
import com.simplemobiletools.commons.helpers.getDateFormatsWithYear
import com.simplemobiletools.commons.models.SimpleContact
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.calendar_item_account.view.*
import kotlinx.android.synthetic.main.calendar_item_calendar.view.*
import kotlinx.android.synthetic.main.dialog_select_calendars.view.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
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

        fun getMyContactsCursor(favoritesOnly: Boolean, withPhoneNumbersOnly: Boolean) = try {
            val getFavoritesOnly = if (favoritesOnly) "1" else "0"
            val getWithPhoneNumbersOnly = if (withPhoneNumbersOnly) "1" else "0"
            val args = arrayOf(getFavoritesOnly, getWithPhoneNumbersOnly)
            CursorLoader(
                activity,
                MyContactsContentProvider.CONTACTS_CONTENT_URI,
                null,
                null,
                args,
                null
            )
        } catch (e: Exception) {
            null
        }

        fun updateWidgets() {
            val widgetIDs = AppWidgetManager.getInstance(activity)?.getAppWidgetIds(
                ComponentName(
                    activity, MyWidgetMonthlyProvider::class.java
                )
            )
                ?: return
            if (widgetIDs.isNotEmpty()) {
                Intent(activity, MyWidgetMonthlyProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs)
                    activity.sendBroadcast(this)
                }
            }

            updateListWidget()
            updateDateWidget()
        }

        fun updateListWidget() {
            val widgetIDs = AppWidgetManager.getInstance(activity)
                ?.getAppWidgetIds(ComponentName(activity, MyWidgetListProvider::class.java))
                ?: return
            if (widgetIDs.isNotEmpty()) {
                Intent(activity, MyWidgetListProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs)
                    activity.sendBroadcast(this)
                }
            }
        }

        fun updateDateWidget() {
            val widgetIDs = AppWidgetManager.getInstance(activity)
                ?.getAppWidgetIds(ComponentName(activity, MyWidgetDateProvider::class.java))
                ?: return
            if (widgetIDs.isNotEmpty()) {
                Intent(activity, MyWidgetDateProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs)
                    activity.sendBroadcast(this)
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

                mainBinding?.llMain?.calendarItemBirthdaySwitch?.isSelected?.let { it1 ->
                    confirmSelection(
                        it1
                    )
                }
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

            mainBinding?.llMain?.calendarItemBirthdaySwitch?.apply {
                this.text = "Add Contact Birthday"
                isChecked = shouldCheck
                mainBinding?.llMain?.calendarItemBirthdaySwitch?.setOnClickListener {
                    toggle()
                }
            }

            mainBinding?.llMain?.calendarItemAnniversarySwitch?.apply {
                this.text = "Add Contact Anniversary"
                isChecked = shouldCheck
                mainBinding?.llMain?.calendarItemBirthdaySwitch?.setOnClickListener {
                    toggle()
                }
            }

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

        private fun confirmSelection(isAdded: Boolean) {
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
            tryAddBirthdays(isAdded)
            activity.config.caldavSyncedCalendarIds = TextUtils.join(",", calendarIds)

            val newCalendarIds = activity.config.getSyncedCalendarIdsAsList()

            activity.config.caldavSync = newCalendarIds.isNotEmpty()
            if (newCalendarIds.isNotEmpty()) {
                Toast.makeText(
                    activity,
                    activity.resources.getString(R.string.syncing),
                    Toast.LENGTH_SHORT
                ).show()
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
//                                Toast.makeText(
//                                    activity,
//                                    activity.resources.getString(R.string.synchronization_completed),
//                                    Toast.LENGTH_SHORT
//                                ).show()
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

        private fun tryAddBirthdays(isAdded: Boolean) {
            val isGranted = EasyPermissions.hasPermissions(activity, *perms)
            if (isGranted) {
                SetRemindersDialog(activity) {
                    val reminders = it
                    val privateCursor = getMyContactsCursor(false, false)?.loadInBackground()

                    ensureBackgroundThread {
                        val privateContacts =
                            MyContactsContentProvider.getSimpleContacts(activity, privateCursor)
                        addPrivateEvents(
                            true,
                            privateContacts,
                            reminders
                        ) { eventsFound, eventsAdded ->
                            addContactEvents(true, reminders, eventsFound, eventsAdded) {
                                when {
                                    it > 0 -> {
                                        Toast.makeText(
                                            activity,
                                            R.string.birthdays_added,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        setupQuickFilter(isAdded)
                                    }
                                    it == -1 -> Toast.makeText(
                                        activity,
                                        R.string.no_new_birthdays,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    else -> Toast.makeText(
                                        activity,
                                        R.string.no_birthdays,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                    activity,
                    R.string.no_contacts_permission,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun addContactEvents(
            birthdays: Boolean,
            reminders: ArrayList<Int>,
            initEventsFound: Int,
            initEventsAdded: Int,
            callback: (Int) -> Unit
        ) {
            var eventsFound = initEventsFound
            var eventsAdded = initEventsAdded
            val uri = ContactsContract.Data.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                ContactsContract.CommonDataKinds.Event.CONTACT_LAST_UPDATED_TIMESTAMP,
                ContactsContract.CommonDataKinds.Event.START_DATE
            )

            val selection =
                "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ?"
            val type =
                if (birthdays) ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY else ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY
            val selectionArgs =
                arrayOf(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, type.toString())

            val dateFormats = getDateFormats()
            val yearDateFormats = getDateFormatsWithYear()
            val existingEvents =
                if (birthdays) activity.eventsDB.getBirthdays() else activity.eventsDB.getAnniversaries()
            val importIDs = HashMap<String, Long>()
            existingEvents.forEach {
                importIDs[it.importId] = it.startTS
            }

            val eventTypeId =
                if (birthdays) activity.eventsHelper.getBirthdaysEventTypeId() else activity.eventsHelper.getAnniversariesEventTypeId()
            val source = if (birthdays) SOURCE_CONTACT_BIRTHDAY else SOURCE_CONTACT_ANNIVERSARY

            activity.queryCursor(
                uri,
                projection,
                selection,
                selectionArgs,
                showErrors = true
            ) { cursor ->
                val contactId =
                    cursor.getIntValue(ContactsContract.CommonDataKinds.Event.CONTACT_ID).toString()
                val name = cursor.getStringValue(ContactsContract.Contacts.DISPLAY_NAME)
                val startDate =
                    cursor.getStringValue(ContactsContract.CommonDataKinds.Event.START_DATE)

                for (format in dateFormats) {
                    try {
                        val formatter = SimpleDateFormat(format, Locale.getDefault())
                        val date = formatter.parse(startDate)
                        val flags = if (format in yearDateFormats) {
                            FLAG_ALL_DAY
                        } else {
                            FLAG_ALL_DAY or FLAG_MISSING_YEAR
                        }

                        val timestamp = date.time / 1000L
                        val lastUpdated =
                            cursor.getLongValue(ContactsContract.CommonDataKinds.Event.CONTACT_LAST_UPDATED_TIMESTAMP)
                        val event = Event(
                            null,
                            timestamp,
                            timestamp,
                            name,
                            reminder1Minutes = reminders[0],
                            reminder2Minutes = reminders[1],
                            reminder3Minutes = reminders[2],
                            importId = contactId,
                            timeZone = DateTimeZone.getDefault().id,
                            flags = flags,
                            repeatInterval = YEAR,
                            repeatRule = REPEAT_SAME_DAY,
                            eventType = eventTypeId,
                            source = source,
                            lastUpdated = lastUpdated
                        )

                        val importIDsToDelete = ArrayList<String>()
                        for ((key, value) in importIDs) {
                            if (key == contactId && value != timestamp) {
                                val deleted =
                                    activity.eventsDB.deleteBirthdayAnniversary(source, key)
                                if (deleted == 1) {
                                    importIDsToDelete.add(key)
                                }
                            }
                        }

                        importIDsToDelete.forEach {
                            importIDs.remove(it)
                        }

                        eventsFound++
                        if (!importIDs.containsKey(contactId)) {
                            activity.eventsHelper.insertEvent(event, false, false) {
                                eventsAdded++
                            }
                        }
                        break
                    } catch (e: Exception) {
                    }
                }
            }

            activity.runOnUiThread {
                callback(if (eventsAdded == 0 && eventsFound > 0) -1 else eventsAdded)
            }
        }

        private fun addPrivateEvents(
            birthdays: Boolean,
            contacts: ArrayList<SimpleContact>,
            reminders: ArrayList<Int>,
            callback: (eventsFound: Int, eventsAdded: Int) -> Unit
        ) {
            var eventsAdded = 0
            var eventsFound = 0
            if (contacts.isEmpty()) {
                callback(0, 0)
                return
            }

            try {
                val eventTypeId =
                    if (birthdays) activity.eventsHelper.getBirthdaysEventTypeId() else activity.eventsHelper.getAnniversariesEventTypeId()
                val source = if (birthdays) SOURCE_CONTACT_BIRTHDAY else SOURCE_CONTACT_ANNIVERSARY

                val existingEvents =
                    if (birthdays) activity.eventsDB.getBirthdays() else activity.eventsDB.getAnniversaries()
                val importIDs = HashMap<String, Long>()
                existingEvents.forEach {
                    importIDs[it.importId] = it.startTS
                }

                contacts.forEach { contact ->
                    val events = if (birthdays) contact.birthdays else contact.anniversaries
                    events.forEach { birthdayAnniversary ->
                        // private contacts are created in Simple Contacts Pro, so we can guarantee that they exist only in these 2 formats
                        val format = if (birthdayAnniversary.startsWith("--")) {
                            "--MM-dd"
                        } else {
                            "yyyy-MM-dd"
                        }

                        val formatter = SimpleDateFormat(format, Locale.getDefault())
                        val date = formatter.parse(birthdayAnniversary)
                        if (date.year < 70) {
                            date.year = 70
                        }

                        val timestamp = date.time / 1000L
                        val lastUpdated = System.currentTimeMillis()
                        val event = Event(
                            null,
                            timestamp,
                            timestamp,
                            contact.name,
                            reminder1Minutes = reminders[0],
                            reminder2Minutes = reminders[1],
                            reminder3Minutes = reminders[2],
                            importId = contact.contactId.toString(),
                            timeZone = DateTimeZone.getDefault().id,
                            flags = FLAG_ALL_DAY,
                            repeatInterval = YEAR,
                            repeatRule = REPEAT_SAME_DAY,
                            eventType = eventTypeId,
                            source = source,
                            lastUpdated = lastUpdated
                        )

                        val importIDsToDelete = ArrayList<String>()
                        for ((key, value) in importIDs) {
                            if (key == contact.contactId.toString() && value != timestamp) {
                                val deleted =
                                    activity.eventsDB.deleteBirthdayAnniversary(source, key)
                                if (deleted == 1) {
                                    importIDsToDelete.add(key)
                                }
                            }
                        }

                        importIDsToDelete.forEach {
                            importIDs.remove(it)
                        }

                        eventsFound++
                        if (!importIDs.containsKey(contact.contactId.toString())) {
                            activity.eventsHelper.insertEvent(event, false, false) {
                                eventsAdded++
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LLL_Error: ", "Add Contact ${e.localizedMessage}")
            }

            callback(eventsFound, eventsAdded)
        }

        private fun setupQuickFilter(isAdded: Boolean) {
            activity.eventsHelper.getEventTypes(activity, false) {
                if (isAdded) {
                    activity.config.displayEventTypes.plus(
                        activity.eventsHelper.getBirthdaysEventTypeId().toString()
                    )
                } else {
                    activity.config.displayEventTypes.minus(
                        activity.eventsHelper.getBirthdaysEventTypeId().toString()
                    )
                }
                updateWidgets()
            }
        }

    }
}