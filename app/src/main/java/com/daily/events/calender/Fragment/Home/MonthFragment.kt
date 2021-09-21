package com.daily.events.calender.Fragment.Home

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.daily.events.calender.Activity.MainActivity
import com.daily.events.calender.Extensions.config
import com.daily.events.calender.Extensions.getViewBitmap
import com.daily.events.calender.Extensions.printBitmap
import com.daily.events.calender.Model.DayMonthly
import com.daily.events.calender.R
import com.daily.events.calender.helpers.Config
import com.daily.events.calender.helpers.DAY_CODE
import com.daily.events.calender.helpers.Formatter
import com.daily.events.calender.helpers.MonthlyCalendarImpl
import com.daily.events.calender.interfaces.MonthlyCalendar
import com.daily.events.calender.interfaces.NavigationListener
import com.daily.events.calender.views.MonthViewWrapper
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisible
import kotlinx.android.synthetic.main.fragment_month.view.*
import kotlinx.android.synthetic.main.top_navigation.view.*
import org.joda.time.DateTime

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MonthFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MonthFragment : Fragment(), MonthlyCalendar {

    private var mTextColor = 0
    private var mSundayFirst = false
    private var mShowWeekNumbers = false
    private var mDayCode = ""
    private var mPackageName = ""
    private var mLastHash = 0L
    private var mCalendar: MonthlyCalendarImpl? = null

    var listener: NavigationListener? = null

    lateinit var mRes: Resources
    lateinit var mHolder: RelativeLayout
    lateinit var wrapper: MonthViewWrapper
    lateinit var mConfig: Config

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
        val view = inflater.inflate(R.layout.fragment_month, container, false)
        mRes = resources
        mPackageName = requireActivity().packageName
        mHolder = view.month_calendar_holder
        wrapper = view.month_view_wrapper

        mDayCode = requireArguments().getString(DAY_CODE)!!
//        Log.e("LLL_Code: ", mDayCode)
        mConfig = requireContext().config
        storeStateVariables()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setupButtons()
        }
        mCalendar = MonthlyCalendarImpl(this, requireContext())

        return view
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
    }

    override fun onResume() {
        super.onResume()
        if (mConfig.showWeekNumbers != mShowWeekNumbers) {
            mLastHash = -1L
        }

        mCalendar!!.apply {
            mTargetDate = Formatter.getDateTimeFromCode(mDayCode)
            getDays(false)    // prefill the screen asap, even if without events
        }

        storeStateVariables()
        updateCalendar()
    }

    private fun storeStateVariables() {
        mConfig.apply {
            mSundayFirst = false
            mShowWeekNumbers = showWeekNumbers
        }
    }

    fun updateCalendar() {
        mCalendar?.updateMonthlyCalendar(Formatter.getDateTimeFromCode(mDayCode))
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MonthFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MonthFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun updateMonthlyCalendar(
        context: Context,
        month: String,
        days: ArrayList<DayMonthly>,
        checkedEvents: Boolean,
        currTargetDate: DateTime
    ) {
        val newHash = month.hashCode() + days.hashCode().toLong()
        if ((mLastHash != 0L && !checkedEvents) || mLastHash == newHash) {
            return
        }

        mLastHash = newHash

        activity?.runOnUiThread {
            mHolder.top_value.apply {

            text = month
                contentDescription = text
                setTextColor(resources.getColor(R.color.black))
            }
            updateDays(days)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun setupButtons() {
        mTextColor = mConfig.textColor

        mHolder.top_left_arrow.apply {
            applyColorFilter(mTextColor)
            background = null
            setOnClickListener {
                listener?.goLeft()
            }

            val pointerLeft = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                requireContext().getDrawable(R.drawable.ic_chevron_left_vector)
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP")
            }
            pointerLeft?.isAutoMirrored = true
            setImageDrawable(pointerLeft)
        }

        mHolder.top_right_arrow.apply {
            applyColorFilter(mTextColor)
            background = null
            setOnClickListener {
                listener?.goRight()
            }

            val pointerRight = requireContext().getDrawable(R.drawable.ic_chevron_right_vector)
            pointerRight?.isAutoMirrored = true
            setImageDrawable(pointerRight)
        }

        mHolder.top_value.apply {
            setTextColor(resources.getColor(R.color.black))
            setOnClickListener {
//                (activity as MainActivity).showGoToDateDialog()
            }
        }
    }

    private fun updateDays(days: ArrayList<DayMonthly>) {
        mHolder.month_view_wrapper.updateDays(days, true) {
            (activity as MainActivity).openDayFromMonthly(Formatter.getDateTimeFromCode(it.code))
        }
    }

    fun printCurrentView() {
        mHolder.apply {
            top_left_arrow.beGone()
            top_right_arrow.beGone()
            top_value.setTextColor(resources.getColor(R.color.black))
            month_view_wrapper.togglePrintMode()

            requireContext().printBitmap(month_calendar_holder.getViewBitmap())

            top_left_arrow.beVisible()
            top_right_arrow.beVisible()
            top_value.setTextColor(resources.getColor(R.color.black))
            month_view_wrapper.togglePrintMode()
        }
    }


}