package com.daily.events.calender.Fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import com.daily.events.calender.Activity.MainActivity
import com.daily.events.calender.Adapter.MyDayPagerAdapter

import com.daily.events.calender.Extensions.*
import com.daily.events.calender.R
import com.daily.events.calender.helpers.Formatter
import com.daily.events.calender.helpers.WEEK_START_DATE_TIME
import com.daily.events.calender.interfaces.WeekFragmentListener
import com.daily.events.calender.views.MyScrollView
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.WEEK_SECONDS
import com.simplemobiletools.commons.views.MyViewPager
import kotlinx.android.synthetic.main.fragment_week_holder.view.*
import org.joda.time.DateTime

class DayFragmentsHolder : MyFragmentHolder(), WeekFragmentListener {
    private val PREFILLED_WEEKS = 151
    private val MAX_SEEKBAR_VALUE = 14

    private var viewPager: MyViewPager? = null
    private var weekHolder: ViewGroup? = null
    private var defaultWeeklyPage = 0
    private var thisWeekTS = 0L
    private var currentWeekTS = 0L
    private var isGoToTodayVisible = false
    private var weekScrollY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dateTimeString = arguments?.getString(WEEK_START_DATE_TIME) ?: return
        currentWeekTS = (DateTime.parse(dateTimeString) ?: DateTime()).seconds()
        thisWeekTS = currentWeekTS
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        weekHolder = inflater.inflate(R.layout.fragment_week_holder, container, false) as ViewGroup
        weekHolder!!.background = ColorDrawable(requireContext().resources.getColor(R.color.white))

        val itemHeight = requireContext().getWeeklyViewItemHeight().toInt()
        weekHolder!!.week_view_hours_holder.setPadding(0, 0, 0, itemHeight)

        viewPager = weekHolder!!.week_view_view_pager
        viewPager!!.id = (System.currentTimeMillis() % 100000).toInt()
        setupFragment()
        return weekHolder
    }

    override fun onResume() {
        super.onResume()
        context?.config?.allowCustomizeDayCount?.let { allow ->
            weekHolder!!.week_view_days_count.beVisibleIf(allow)
            weekHolder!!.week_view_seekbar.beVisibleIf(allow)
        }
        setupSeekbar()
    }

    private fun setupFragment() {
        addHours()
        setupWeeklyViewPager()

        weekHolder!!.week_view_hours_scrollview.setOnTouchListener { view, motionEvent -> true }

        weekHolder!!.week_view_seekbar.apply {
            progress = context?.config?.weeklyViewDays ?: 7
            max = MAX_SEEKBAR_VALUE

            onSeekBarChangeListener {
                if (it == 0) {
                    progress = 1
                }
                updateWeeklyViewDays(progress)
            }
        }
        updateActionBarTitle()
    }

    private fun setupWeeklyViewPager() {
        val weekTSs = getWeekTimestamps(currentWeekTS)
        val weeklyAdapter =
            MyDayPagerAdapter(requireActivity().supportFragmentManager, weekTSs, this)

        defaultWeeklyPage = weekTSs.size / 2

        viewPager!!.apply {
            adapter = weeklyAdapter
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    currentWeekTS = weekTSs[position]
                    val shouldGoToTodayBeVisible = shouldGoToTodayBeVisible()
                    if (isGoToTodayVisible != shouldGoToTodayBeVisible) {
                        (activity as? MainActivity)?.toggleGoToTodayVisibility(
                            shouldGoToTodayBeVisible
                        )
                        isGoToTodayVisible = shouldGoToTodayBeVisible
                    }

                    setupWeeklyActionbarTitle(weekTSs[position])
                }
            })
            currentItem = defaultWeeklyPage
        }

        weekHolder!!.week_view_hours_scrollview.setOnScrollviewListener(object :
            MyScrollView.ScrollViewListener {
            override fun onScrollChanged(
                scrollView: MyScrollView,
                x: Int,
                y: Int,
                oldx: Int,
                oldy: Int
            ) {
                weekScrollY = y
                weeklyAdapter.updateScrollY(viewPager!!.currentItem, y)
            }
        })
    }

    private fun addHours(textColor: Int = requireContext().config.textColor) {
        val itemHeight = requireContext().getWeeklyViewItemHeight().toInt()
        weekHolder!!.week_view_hours_holder.removeAllViews()
        val hourDateTime = DateTime().withDate(2000, 1, 1).withTime(0, 0, 0, 0)
        for (i in 1..23) {
            val formattedHours = Formatter.getHours(requireContext(), hourDateTime.withHourOfDay(i))
            (layoutInflater.inflate(
                R.layout.weekly_view_hour_textview,
                null,
                false
            ) as TextView).apply {
                text = formattedHours
                setTextColor(context.resources.getColor(R.color.black))
                height = itemHeight
                weekHolder!!.week_view_hours_holder.addView(this)
            }
        }
    }

    private fun getWeekTimestamps(targetSeconds: Long): List<Long> {
        val weekTSs = ArrayList<Long>(PREFILLED_WEEKS)
        val dateTime = Formatter.getDateTimeFromTS(targetSeconds)
        val shownWeekDays = requireContext().config.weeklyViewDays
        var currentWeek = dateTime.minusDays(PREFILLED_WEEKS / 2 * shownWeekDays)
        for (i in 0 until PREFILLED_WEEKS) {
            weekTSs.add(currentWeek.seconds())
            currentWeek = currentWeek.plusDays(shownWeekDays)
        }
        return weekTSs
    }

    private fun setupWeeklyActionbarTitle(timestamp: Long) {

        val startDateTime = Formatter.getDateTimeFromTS(timestamp)
        val endDateTime = Formatter.getDateTimeFromTS(timestamp + WEEK_SECONDS)
        val startMonthName = Formatter.getMonthName(requireContext(), startDateTime.monthOfYear)
        var newTitle = Formatter.getMonthName(requireContext(), startDateTime.monthOfYear)
        if (startDateTime.monthOfYear == endDateTime.monthOfYear) {
            newTitle = startMonthName
            if (startDateTime.year != DateTime().year) {
                newTitle += " - ${startDateTime.year}"
            }
        } else {
            val endMonthName = Formatter.getMonthName(requireContext(), endDateTime.monthOfYear)
            newTitle = "$startMonthName - $endMonthName"
        }
        val str = newTitle.plus(" ").plus(
            "\n${getString(R.string.week)} ${
                startDateTime.plusDays(3).weekOfWeekyear
            }"
        )
        MainActivity.mainBinding?.dateTitleTV?.text = str.toString()
    }

    override fun goToToday() {
        currentWeekTS = thisWeekTS
        setupFragment()
    }

    override fun showGoToDateDialog() {
        requireActivity().setTheme(requireContext().getDialogTheme())
        val view = layoutInflater.inflate(R.layout.date_picker, null)
        val datePicker = view.findViewById<DatePicker>(R.id.date_picker)

        val dateTime = Formatter.getDateTimeFromTS(currentWeekTS)
        datePicker.init(dateTime.year, dateTime.monthOfYear - 1, dateTime.dayOfMonth, null)

        AlertDialog.Builder(requireContext())
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok) { dialog, which -> dateSelected(dateTime, datePicker) }
            .create().apply {
                activity?.setupDialogStuff(view, this)
            }
    }

    private fun dateSelected(dateTime: DateTime, datePicker: DatePicker) {
        val isSundayFirst = false
        val month = datePicker.month + 1
        val year = datePicker.year
        val day = datePicker.dayOfMonth
        var newDateTime = dateTime.withDate(year, month, day)

        if (isSundayFirst) {
            newDateTime = newDateTime.plusDays(1)
        }

        var selectedWeek = newDateTime.withDayOfWeek(1).withTimeAtStartOfDay()
            .minusDays(if (isSundayFirst) 1 else 0)
        if (newDateTime.minusDays(7).seconds() > selectedWeek.seconds()) {
            selectedWeek = selectedWeek.plusDays(7)
        }

        currentWeekTS = selectedWeek.seconds()
        setupFragment()
    }

    private fun setupSeekbar() {
        if (context?.config?.allowCustomizeDayCount != true) {
            return
        }

        weekHolder!!.week_view_days_count.onGlobalLayout {
            if (weekHolder!!.week_view_seekbar.width != 0) {
                weekHolder!!.week_view_seekbar.layoutParams.width =
                    weekHolder!!.week_view_seekbar.width
            }
            (weekHolder!!.week_view_seekbar.layoutParams as RelativeLayout.LayoutParams).removeRule(
                RelativeLayout.START_OF
            )
        }

        updateDaysCount(context?.config?.weeklyViewDays ?: 7)
    }

    private fun updateWeeklyViewDays(days: Int) {
        requireContext().config.weeklyViewDays = days
        updateDaysCount(days)
        setupWeeklyViewPager()
    }

    private fun updateDaysCount(cnt: Int) {
        weekHolder!!.week_view_days_count.text =
            requireContext().resources.getQuantityString(R.plurals.days, cnt, cnt)
        (viewPager?.adapter as? MyDayPagerAdapter)?.updateVisibleDaysCount(
            viewPager!!.currentItem,
            cnt,
            currentWeekTS
        )
    }

    override fun refreshEvents() {
        (viewPager?.adapter as? MyDayPagerAdapter)?.updateCalendars(viewPager!!.currentItem)
    }

    override fun shouldGoToTodayBeVisible() = currentWeekTS != thisWeekTS

    override fun updateActionBarTitle() {
        setupWeeklyActionbarTitle(currentWeekTS)
    }

    override fun getNewEventDayCode(): String {
        val currentTS = System.currentTimeMillis() / 1000
        return if (currentTS > currentWeekTS && currentTS < currentWeekTS + WEEK_SECONDS) {
            Formatter.getTodayCode()
        } else {
            Formatter.getDayCodeFromTS(currentWeekTS)
        }
    }

    override fun scrollTo(y: Int) {
        weekHolder!!.week_view_hours_scrollview.scrollY = y
        weekScrollY = y
    }

    override fun updateHoursTopMargin(margin: Int) {
        weekHolder?.apply {
            week_view_hours_divider?.layoutParams?.height = margin
            week_view_hours_scrollview?.requestLayout()
            week_view_hours_scrollview?.onGlobalLayout {
                week_view_hours_scrollview.scrollY = weekScrollY
            }
        }
    }

    override fun getCurrScrollY() = weekScrollY

    override fun updateRowHeight(rowHeight: Int) {
        val childCnt = weekHolder!!.week_view_hours_holder.childCount
        for (i in 0..childCnt) {
            val textView =
                weekHolder!!.week_view_hours_holder.getChildAt(i) as? TextView ?: continue
            textView.layoutParams.height = rowHeight
        }

        weekHolder!!.week_view_hours_holder.setPadding(0, 0, 0, rowHeight)
        (viewPager!!.adapter as? MyDayPagerAdapter)?.updateNotVisibleScaleLevel(viewPager!!.currentItem)
    }

    override fun getFullFragmentHeight() =
        weekHolder!!.week_view_holder.height - weekHolder!!.week_view_seekbar.height - weekHolder!!.week_view_days_count_divider.height

    override fun printView() {
        weekHolder!!.apply {
            week_view_days_count_divider.beGone()
            week_view_seekbar.beGone()
            week_view_days_count.beGone()
            addHours(resources.getColor(R.color.theme_light_text_color))
            background = ColorDrawable(Color.WHITE)
            (viewPager?.adapter as? MyDayPagerAdapter)?.togglePrintMode(
                viewPager?.currentItem ?: 0
            )

            Handler().postDelayed({
                requireContext().printBitmap(weekHolder!!.week_view_holder.getViewBitmap())

                Handler().postDelayed({
                    week_view_days_count_divider.beVisible()
                    week_view_seekbar.beVisible()
                    week_view_days_count.beVisible()
                    addHours()
                    background = ColorDrawable(requireContext().config.backgroundColor)
                    (viewPager?.adapter as? MyDayPagerAdapter)?.togglePrintMode(
                        viewPager?.currentItem ?: 0
                    )
                }, 1000)
            }, 1000)
        }
    }
}
