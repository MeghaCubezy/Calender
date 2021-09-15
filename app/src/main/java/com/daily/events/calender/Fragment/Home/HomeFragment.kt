package com.daily.events.calender.Fragment.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.daily.events.calender.Extensions.config
import com.daily.events.calender.Extensions.seconds
import com.daily.events.calender.Fragment.MonthFragmentsHolder
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

class HomeFragment : Fragment() ,View.OnClickListener{
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

    var fragmentHomeBinding: FragmentHomeBinding? = null

    //    var yearFragment: MainYearFragment? = null
    var monthFragment: MonthFragmentsHolder? = null
    var weekFragment: WeekFragment? = null
    var dayFragment: DayFragment ?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentHomeBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        fragmentHomeBinding?.yearIV?.setOnClickListener(this)
        fragmentHomeBinding?.monthIV?.setOnClickListener(this)
        fragmentHomeBinding?.weekIV?.setOnClickListener(this)
        fragmentHomeBinding?.dayIV?.setOnClickListener(this)

//        yearFragment = MainYearFragment()
        monthFragment = MonthFragmentsHolder()
        weekFragment = WeekFragment()
        dayFragment = DayFragment()

//        yearFragment?.let {
//            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container1, it)?.commit()
//        }

        requireActivity().config.storedView = YEARLY_VIEW
        updateViewPager()
        return fragmentHomeBinding?.root
    }

    private fun updateViewPager(dayCode: String? = Formatter.getTodayCode()) {
        val fragment = getFragmentsHolder()

        val bundle = Bundle()

        when (requireActivity().config.storedView) {
            DAILY_VIEW, MONTHLY_VIEW, MONTHLY_DAILY_VIEW -> bundle.putString(DAY_CODE, dayCode)
            WEEKLY_VIEW -> bundle.putString(WEEK_START_DATE_TIME, getThisWeekDateTime())
        }

        fragment.arguments = bundle
        childFragmentManager.beginTransaction().add(R.id.container1, fragment).commitNow()
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
//        DAILY_VIEW -> DayFragmentsHolder()
        MONTHLY_VIEW -> MonthFragmentsHolder()
//        MONTHLY_DAILY_VIEW -> MonthDayFragmentsHolder()
        YEARLY_VIEW -> YearFragmentsHolder()
//        EVENTS_LIST_VIEW -> EventListFragment()
        else -> YearFragmentsHolder()
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.yearIV ->{
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
                    requireActivity().config.storedView = YEARLY_VIEW
                    updateViewPager()
                }
            }
            R.id.monthIV ->{
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
                    requireActivity().config.storedView = MONTHLY_VIEW
                    updateViewPager()
                }
            }
            R.id.weekIV ->{
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
                            R.color.theme_color
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    fragmentHomeBinding?.dayIV?.setColorFilter(
                        ContextCompat.getColor(
                            it,
                            R.color.grey
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    weekFragment?.let { it1 ->
                        activity?.supportFragmentManager?.beginTransaction()?.replace(
                            R.id.container1,
                            it1
                        )?.commit()
                    }
                }
            }
            R.id.dayIV ->{
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
                    dayFragment?.let { it1 ->
                        activity?.supportFragmentManager?.beginTransaction()?.replace(
                            R.id.container1,
                            it1
                        )?.commit()
                    }
                }
            }
        }
    }


}