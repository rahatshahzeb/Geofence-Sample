package com.android.task.di.module

import com.android.task.service.GeofenceTransitionsJobIntentService
import com.android.task.service.NetworkSchedulerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    internal abstract fun contributeGeofenceTransitionsJobIntentService(): GeofenceTransitionsJobIntentService

    @ContributesAndroidInjector
    internal abstract fun contributeNetworkSchedulerService(): NetworkSchedulerService
}