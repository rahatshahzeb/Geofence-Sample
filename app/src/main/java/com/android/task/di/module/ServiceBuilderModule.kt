package com.android.task.di.module

import com.android.task.geofence.GeofenceTransitionsJobIntentService
import dagger.Module
import dagger.android.ContributesAndroidInjector



@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    internal abstract fun contributeGeofenceTransitionsJobIntentService(): GeofenceTransitionsJobIntentService
}