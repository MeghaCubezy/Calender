package com.daily.events.calender.Fragment.Home

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.daily.events.calender.Activity.MainActivity
import com.daily.events.calender.Extensions.config
import com.daily.events.calender.Extensions.seconds
import com.daily.events.calender.Fragment.DayFragmentsHolder
import com.daily.events.calender.Fragment.MonthFragmentsHolder
import com.daily.events.calender.Fragment.WeekFragmentsHolder
import com.daily.events.calender.Fragment.YearFragmentsHolder
import com.daily.events.calender.R
import com.daily.events.calender.databinding.FragmentHomeBinding
import com.daily.events.calender.helpers.*
import com.daily.events.calender.helpers.Formatter
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

open class HomeFragment : Fragment(), View.OnClickListener {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentHomeBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        mActivity = requireActivity()
        cllManager = childFragmentManager
        fragmentHomeBinding?.yearIV?.setOnClickListener(this)
        fragmentHomeBinding?.monthIV?.setOnClickListener(this)
        fragmentHomeBinding?.weekIV?.setOnClickListener(this)
        fragmentHomeBinding?.dayIV?.setOnClickListener(this)

        monthChanges()
        val fragment = MonthFragmentsHolder()
        MainActivity.mainBinding?.today?.setOnClickListener {
            if (MainActivity.mainBinding!!.navigationDrawer.isDrawerOpen)
                MainActivity.mainBinding!!.navigationDrawer.closeDrawer()
            fragment.goToToday()
        }
        val bundle = Bundle()
        bundle.putString(DAY_CODE, Formatter.getTodayCode())
        fragment.arguments = bundle
        childFragmentManager.beginTransaction().replace(R.id.container1, fragment).commitNow()
        /*   when (requireActivity().config.storedView) {
               YEARLY_VIEW -> {
                   yearChanges()
               }
               MONTHLY_VIEW -> {
                   monthChanges()
               }
               WEEKLY_VIEW -> {
                   AddBirthdayTask().execute()
               }
               else -> {
                   dayChanges()
               }
           }
           updateViewPager()*/

        return fragmentHomeBinding?.root
    }

    open fun updateViewPager(dayCode: String? = Formatter.getTodayCode()) {

        val fragment = getFragmentsHolder()
        MainActivity.mainBinding?.today?.setOnClickListener {
            if (MainActivity.mainBinding!!.navigationDrawer.isDrawerOpen)
                MainActivity.mainBinding!!.navigationDrawer.closeDrawer()
            fragment.goToToday()
        }
        val bundle = Bundle()

        if (requireActivity().config.storedView == MONTHLY_VIEW ||
            requireActivity().config.storedView == MONTHLY_DAILY_VIEW
        ) {
            bundle.putString(DAY_CODE, dayCode)
        } else if (requireActivity().config.storedView == WEEKLY_VIEW) {
            bundle.putString(WEEK_START_DATE_TIME, getThisWeekDateTime())
        } else if (requireActivity().config.storedView == DAILY_VIEW) {
            bundle.putString(DAY_CODE, dayCode)
            bundle.putString(WEEK_START_DATE_TIME, getThisWeekDateTime())
        }

        fragment.arguments = bundle
        childFragmentManager.beginTransaction().replace(R.id.container1, fragment).commitNow()
    }

    private fun getThisWeekDateTime(): String {
        val currentOffsetHours = TimeZone.getDefault().rawOffset / 1000 / 60 / 60

        // not great, not terrible
        val useHours = if (currentOffsetHours >= 10) 8 else 12
        var thisweek =
            DateTime().withZone(DateTimeZone.UTC).withDayOfWeek(1).withHourOfDay(useHours)
                .minusDays(if (requireActivity().config.isSundayFirst) 1 else 0)
        if (DateTime().minusDays(7).seconds() > thisweek.seconds()) {
            thisweek = thisweek.plusDays(7)
        }
        return thisweek.toString()
    }

    private fun getFragmentsHolder() = when (requireActivity().config.storedView) {
        DAILY_VIEW -> DayFragmentsHolder()
        MONTHLY_VIEW -> MonthFragmentsHolder()
        WEEKLY_VIEW -> WeekFragmentsHolder()
        YEARLY_VIEW -> YearFragmentsHolder()
        else -> MonthFragmentsHolder()
    }

    companion object {
        var cllManager: FragmentManager? = null

        lateinit var mActivity: Activity
        var fragmentHomeBinding: FragmentHomeBinding? = null

        private fun getFragmentsHolder() = when (mActivity.config.storedView) {
            DAILY_VIEW -> DayFragmentsHolder()
            MONTHLY_VIEW -> MonthFragmentsHolder()
            WEEKLY_VIEW -> WeekFragmentsHolder()
            YEARLY_VIEW -> YearFragmentsHolder()
            else -> MonthFragmentsHolder()
        }

        private fun getThisWeekDateTime(): String {
            val currentOffsetHours = TimeZone.getDefault().rawOffset / 1000 / 60 / 60

            // not great, not terrible
            val useHours = if (currentOffsetHours >= 10) 8 else 12
            var thisweek =
                DateTime().withZone(DateTimeZone.UTC).withDayOfWeek(1).withHourOfDay(useHours)
                    .minusDays(if (mActivity.config.isSundayFirst) 1 else 0)
            if (DateTime().minusDays(7).seconds() > thisweek.seconds()) {
                thisweek = thisweek.plusDays(7)
            }
            return thisweek.toString()
        }

        open fun updateViewPager(dayCode: String? = Formatter.getTodayCode()) {

            val fragment = getFragmentsHolder()
            MainActivity.mainBinding?.today?.setOnClickListener {
                if (MainActivity.mainBinding!!.navigationDrawer.isDrawerOpen)
                    MainActivity.mainBinding!!.navigationDrawer.closeDrawer()
                fragment.goToToday()
            }
            val bundle = Bundle()

            if (mActivity.config.storedView == MONTHLY_VIEW ||
                mActivity.config.storedView == MONTHLY_DAILY_VIEW
            ) {
                bundle.putString(DAY_CODE, dayCode)
            } else if (mActivity.config.storedView == WEEKLY_VIEW) {
                bundle.putString(WEEK_START_DATE_TIME, getThisWeekDateTime())
            } else if (mActivity.config.storedView == DAILY_VIEW) {
                bundle.putString(DAY_CODE, dayCode)
                bundle.putString(WEEK_START_DATE_TIME, getThisWeekDateTime())
            }

            fragment.arguments = bundle
            cllManager?.beginTransaction()?.replace(R.id.container1, fragment)?.commitNow()
        }

        fun weekChanges() {
            mActivity.let {
                fragmentHomeBinding?.yearIV?.setColorFilter(
                    ContextCompat.getColor(
                        it,
                        R.color.grey
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                fragmentHomeBinding?.monthIV?.setColorFilter(
                    ContextCompat.getColor(
                        it,
                        R.color.grey
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                fragmentHomeBinding?.weekIV?.setColorFilter(
                    ContextCompat.getColor(
                        it,
                        R.color.theme_color
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                fragmentHomeBinding?.dayIV?.setColorFilter(
                    ContextCompat.getColor(
                        it,
                        R.color.grey
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        }

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun yearChanges() {
        activity?.let {
            fragmentHomeBinding?.yearIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.theme_color
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.monthIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.weekIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.dayIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }

    fun monthChanges() {
        activity?.let {
            fragmentHomeBinding?.yearIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.monthIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.theme_color
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.weekIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.dayIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }

    fun weekChanges() {
        requireActivity().let {
            fragmentHomeBinding?.yearIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.monthIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.weekIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.theme_color
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.dayIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }

    fun dayChanges() {
        activity?.let {
            fragmentHomeBinding?.yearIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.monthIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.weekIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.grey
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
            fragmentHomeBinding?.dayIV?.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.theme_color
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }

    override fun onClick(v: View?) {
        System.gc()
        when (v?.id) {
            R.id.weekIV -> {
                Log.e("LLLL_Click: ", "Done")
                requireActivity().config.storedView = WEEKLY_VIEW
//                mActivity.runOnUiThread {
//                    Companion.weekChanges()
//                    Companion.updateViewPager()
//                }
                AddBirthdayTask().execute()
//                doAsync {
//                    requireActivity().runOnUiThread {
//                        weekChanges()
//                        updateViewPager()
//                    }
//                }
            }
            R.id.yearIV -> {
                yearChanges()
                requireActivity().config.storedView = YEARLY_VIEW
                updateViewPager()
            }
            R.id.monthIV -> {
                monthChanges()
                requireActivity().config.storedView = MONTHLY_VIEW
                updateViewPager()
            }
            R.id.dayIV -> {
                dayChanges()
                requireActivity().config.storedView = DAILY_VIEW
                updateViewPager()
            }
        }
    }

    class AddBirthdayTask : AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            MainActivity.mainBinding?.progressBar1?.visibility = View.VISIBLE
            MainActivity.mainBinding?.hideBack?.visibility = View.VISIBLE
//            mActivity.runOnUiThread {
//                MainActivity.mainBinding?.lottieLoader?.visibility = View.VISIBLE
//            }
        }

        override fun doInBackground(vararg params: Void?): String {
            mActivity.runOnUiThread {
                weekChanges()
                updateViewPager()
            }
            return ""
        }

    }
}