package com.android.task.di.module

import android.content.Context
import com.android.task.geofence.GeofenceTransitionsJobIntentService
import com.android.task.preference.SharedPreferenceManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(val context: Context) {

    @Provides
    @Singleton
    fun provideSharedPreferenceManager(): SharedPreferenceManager {
        val sharedPreferences = context.getSharedPreferences(
            SharedPreferenceManager.SHARED_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        return SharedPreferenceManager(sharedPreferences)
    }
}
