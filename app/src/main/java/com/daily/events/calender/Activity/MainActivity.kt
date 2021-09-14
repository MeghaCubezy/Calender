package com.daily.events.calender.Activity

import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.daily.events.calender.Fragment.EventFragment
import com.daily.events.calender.Fragment.Home.HomeFragment
import com.daily.events.calender.Fragment.NotificationFragment
import com.daily.events.calender.Fragment.SettingFragment
import com.daily.events.calender.R
import com.daily.events.calender.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : BaseActivity() , BottomNavigationView.OnNavigationItemSelectedListener {

    var mainBinding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding =
            DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        mainBinding?.bottomnavigationbar?.setOnNavigationItemSelectedListener(this)
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
}