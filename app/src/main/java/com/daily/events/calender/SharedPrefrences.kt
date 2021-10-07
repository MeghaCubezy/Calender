package com.daily.events.calender

import android.content.Context

open class SharedPrefrences {

    companion object {
        val MyPREFERENCES = "Calendar"
        var FirstUser: String = "value"

        fun setUser(c1: Context, firstTime: Boolean) {
            val prefs = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            val edit = prefs.edit()
            edit.putBoolean(
                FirstUser,
                firstTime
            )
            edit.commit()
        }

        fun getUser(c1: Context): Boolean {
            val sharedpreferences = c1.getSharedPreferences(
                MyPREFERENCES,
                Context.MODE_PRIVATE
            )
            return sharedpreferences.getBoolean(
                FirstUser,
                false
            )
        }
    }
}