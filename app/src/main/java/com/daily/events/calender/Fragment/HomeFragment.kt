package com.daily.events.calender.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.daily.events.calender.R
import com.daily.events.calender.databinding.FragmentHomeBinding

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

    var fragmentHomeBinding:FragmentHomeBinding? =null
    var yearFragment: YearFragment ?=null
    var monthFragment: MonthFragment ?=null
    var weekFragment: WeekFragment ?=null
    var dayFragment: DayFragment ?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false )

        fragmentHomeBinding?.yearIV?.setOnClickListener(this)
        fragmentHomeBinding?.monthIV?.setOnClickListener(this)
        fragmentHomeBinding?.weekIV?.setOnClickListener(this)
        fragmentHomeBinding?.dayIV?.setOnClickListener(this)

        yearFragment= YearFragment()
        monthFragment= MonthFragment()
        weekFragment= WeekFragment()
        dayFragment=DayFragment()

        yearFragment?.let {
            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container1, it)?.commit()
        }

        return fragmentHomeBinding?.getRoot()
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
                activity?.let{
                    fragmentHomeBinding?.yearIV?.setColorFilter(ContextCompat.getColor(it, R.color.theme_color), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.monthIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.weekIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.dayIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    yearFragment?.let { it1 ->
                        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container1,
                            it1
                        )?.commit()
                    }
                }
            }
            R.id.monthIV ->{
                activity?.let{
                    fragmentHomeBinding?.yearIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.monthIV?.setColorFilter(ContextCompat.getColor(it, R.color.theme_color), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.weekIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.dayIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    monthFragment?.let { it1 ->
                        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container1,
                            it1
                        )?.commit()
                    }
                }
            }
            R.id.weekIV ->{
                activity?.let{
                    fragmentHomeBinding?.yearIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.monthIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.weekIV?.setColorFilter(ContextCompat.getColor(it, R.color.theme_color), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.dayIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    weekFragment?.let { it1 ->
                        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container1,
                            it1
                        )?.commit()
                    }
                }
            }
            R.id.dayIV ->{
                activity?.let{
                    fragmentHomeBinding?.yearIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.monthIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.weekIV?.setColorFilter(ContextCompat.getColor(it, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    fragmentHomeBinding?.dayIV?.setColorFilter(ContextCompat.getColor(it, R.color.theme_color), android.graphics.PorterDuff.Mode.SRC_IN);
                    dayFragment?.let { it1 ->
                        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container1,
                            it1
                        )?.commit()
                    }
                }
            }
        }
    }
}