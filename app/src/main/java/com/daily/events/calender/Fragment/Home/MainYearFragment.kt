package com.daily.events.calender.Fragment.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.daily.events.calender.Extensions.config
import com.daily.events.calender.Extensions.seconds
import com.daily.events.calender.R
import com.daily.events.calender.databinding.FragmentMainYearBinding
import com.daily.events.calender.helpers.*
import com.daily.events.calender.helpers.Formatter
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainYearFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainYearFragment : Fragment() {
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


    var fragmentMainYearBinding: FragmentMainYearBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentMainYearBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_main_year, container, false)

        updateViewPager()
        return fragmentMainYearBinding?.root
    }

    private fun updateViewPager(dayCode: String? = Formatter.getTodayCode()) {
        val fragment = YearFragment()

        val bundle = Bundle()

        when (requireActivity().config.storedView) {
            DAILY_VIEW, MONTHLY_VIEW, MONTHLY_DAILY_VIEW -> bundle.putString(DAY_CODE, dayCode)
            WEEKLY_VIEW -> bundle.putString(WEEK_START_DATE_TIME, getThisWeekDateTime())
        }

        fragment.arguments = bundle
        childFragmentManager.beginTransaction().add(R.id.fragments_holder, fragment).commitNow()
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainYearFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainYearFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}