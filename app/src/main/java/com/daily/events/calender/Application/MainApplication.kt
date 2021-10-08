package com.daily.events.calender.Application

import android.content.Context
import android.util.Log
import androidx.multidex.MultiDexApplication


class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    override fun attachBaseContext(base: Context) {
        val context = setupTheme(base)
        super.attachBaseContext(context)
    }

    fun setupTheme(context: Context): Context {
        Log.e("LLL_Tag: ", "Application")
        return context
    }

    companion object {
        private var context: Context? = null
        fun requireContext(): Context? {
            return context
        }
    }
}
