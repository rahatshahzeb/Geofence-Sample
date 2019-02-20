package com.android.task.preference

import android.content.SharedPreferences
import com.android.task.util.SSConstants.PACKAGE_NAME

class SharedPreferenceManager(private val sharedPreferences: SharedPreferences) {

    companion object Keys {

        const val SHARED_PREFERENCE_NAME = PACKAGE_NAME + ".SC_SHARED_PREFERENCES"

        const val TOKEN = PACKAGE_NAME + ".TOKEN"
    }

    var token: String?
        set(value) {
            sharedPreferences.edit().apply {
                putString(TOKEN, value)
                apply()
            }
        }
        get() {
            return sharedPreferences.getString(TOKEN, null)
        }
}