package com.android.task.util

import com.android.task.BuildConfig

object SSConstants {

    const val PACKAGE_NAME = BuildConfig.APPLICATION_ID

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    const val GEOFENCE_EXPIRATION_IN_HOURS: Long = 12

    const val GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000
}