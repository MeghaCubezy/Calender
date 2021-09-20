package com.daily.events.calender.Activity

import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.daily.events.calender.Extensions.config
import com.daily.events.calender.Fragment.EventFragment
import com.daily.events.calender.Fragment.Home.HomeFragment
import com.daily.events.calender.Fragment.MonthFragmentsHolder
import com.daily.events.calender.Fragment.NotificationFragment
import com.daily.events.calender.Fragment.SettingFragment
import com.daily.events.calender.R
import com.daily.events.calender.databinding.ActivityMainBinding
import com.daily.events.calender.helpers.DAY_CODE
import com.daily.events.calender.helpers.Formatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.joda.time.DateTime
import java.util.*


class MainActivity : BaseActivity() , BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        var mainBinding: ActivityMainBinding? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding =
            DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        mainBinding?.bottomnavigationbar?.setOnNavigationItemSelectedListener(this)

        config.isSundayFirst = false
    }

    override fun permissionGranted() {
        homeFragment= HomeFragment()
        eventFragment= EventFragment()
        notificationFragment= NotificationFragment()
        settingFragment=SettingFragment()

         homeFragment?.let {
             supportFragmentManager.beginTransaction().replace(R.id.container, it)
                 .commit()
         }
    }

    var homeFragment: HomeFragment ?=null
    var eventFragment: EventFragment ?=null
    var notificationFragment: NotificationFragment ?=null
    var settingFragment: SettingFragment ?=null

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

}