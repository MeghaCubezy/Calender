package com.daily.events.calender.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.daily.events.calender.R
import com.daily.events.calender.databinding.FragmentMonthScrollBinding
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MonthScrollFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MonthScrollFragment : Fragment() {

    var fragmentMonthScrollFragment: FragmentMonthScrollBinding? = null

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
        fragmentMonthScrollFragment =
            DataBindingUtil.inflate(inflater, R.layout.fragment_month_scroll, container, false)
        with(fragmentMonthScrollFragment) {
            this?.collapsibleCalendar?.setCalendarListener(object :
                CollapsibleCalendar.CalendarListener {

                override fun onClickListener() {

                }

                override fun onDataUpdate() {

                }

                override fun onDayChanged() {

                }

                override fun onDaySelect() {

                }

                override fun onItemClick(v: View) {

                }

                override fun onMonthChange() {

                }

                override fun onWeekChange(position: Int) {

                }

            })
        }

        return fragmentMonthScrollFragment?.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MonthScrollFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MonthScrollFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}