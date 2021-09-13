package com.daily.events.calender.Fragment.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.daily.events.calender.R
import kotlinx.android.synthetic.main.fragment_year.view.*
import org.joda.time.DateTime


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class YearFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_year, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            YearFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setupMonths() {
        val dateTime = DateTime().withDate(mYear, 2, 1).withHourOfDay(12)
        val days = dateTime.dayOfMonth().maximumValue
        mView.month_2.setDays(days)

        val now = DateTime()

        for (i in 1..12) {
            val monthView = mView.findViewById<SmallMonthView>(
                resources.getIdentifier(
                    "month_$i",
                    "id",
                    context!!.packageName
                )
            )
            var dayOfWeek = dateTime.withMonthOfYear(i).dayOfWeek().get()
            if (!mSundayFirst) {
                dayOfWeek--
            }

            val monthLabel = mView.findViewById<TextView>(
                resources.getIdentifier(
                    "month_${i}_label",
                    "id",
                    context!!.packageName
                )
            )
            val curTextColor = when {
                isPrintVersion -> resources.getColor(R.color.theme_light_text_color)
                else -> context!!.config.textColor
            }

            monthLabel.setTextColor(curTextColor)

            monthView.firstDay = dayOfWeek
            monthView.setOnClickListener {
                (activity as MainActivity).openMonthFromYearly(DateTime().withDate(mYear, i, 1))
            }
        }

        if (!isPrintVersion) {
            markCurrentMonth(now)
        }
    }
}