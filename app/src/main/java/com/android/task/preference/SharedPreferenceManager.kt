package com.android.task.preference

import android.content.SharedPreferences
import com.android.task.util.SSConstants.PACKAGE_NAME

class SharedPreferenceManager(private val sharedPreferences: SharedPreferences) {

    companion object Keys {

        const val SHARED_PREFERENCE_NAME = PACKAGE_NAME + ".SC_SHARED_PREFERENCES"

        const val LATITUDE = PACKAGE_NAME + ".LATITUDE"
        const val LONGITUDE = PACKAGE_NAME + ".LONGITUDE"
        const val RADIUS = PACKAGE_NAME + ".RADIUS"
        const val WIFI = PACKAGE_NAME + ".WIFI"
        const val CONNECTED_WIFI = PACKAGE_NAME + ".CONNECTED_WIFI"
        const val GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY"
    }

    var latitude: String?
        set(value) {
            sharedPreferences.edit().apply {
                putString(LATITUDE, value)
                apply()
            }
        }
        get() {
            return sharedPreferences.getString(LATITUDE, "")
        }

    var longitude: String?
        set(value) {
            sharedPreferences.edit().apply {
                putString(LONGITUDE, value)
                apply()
            }
        }
        get() {
            return sharedPreferences.getString(LONGITUDE, "")
        }

    var radius: String?
        set(value) {
            sharedPreferences.edit().apply {
                putString(RADIUS, value)
                apply()
            }
        }
        get() {
            return sharedPreferences.getString(RADIUS, "")
        }

    var wifi: String?
        set(value) {
            sharedPreferences.edit().apply {
                putString(WIFI, value)
                apply()
            }
        }
        get() {
            return sharedPreferences.getString(WIFI, "")
        }

    var connectedWifi: String?
        set(value) {
            sharedPreferences.edit().apply {
                putString(CONNECTED_WIFI, value)
                apply()
            }
        }
        get() {
            return sharedPreferences.getString(CONNECTED_WIFI, "")
        }

    var geofenceAddedKey: Boolean
        set(value) {
            sharedPreferences.edit().apply {
                putBoolean(GEOFENCES_ADDED_KEY, value)
                apply()
            }
        }
        get() {
            return sharedPreferences.getBoolean(GEOFENCES_ADDED_KEY, false)
        }
}