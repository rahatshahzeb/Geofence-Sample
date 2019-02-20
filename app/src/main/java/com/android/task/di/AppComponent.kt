package com.android.task.di

import android.app.Application
import com.android.task.SSApplication
import com.android.task.di.module.ActivityBindingModule
import com.android.task.di.module.AppModule
import com.android.task.di.module.NetworkModule
import com.android.task.di.module.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        NetworkModule::class,
        ViewModelModule::class,
        ActivityBindingModule::class]
)
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun appModule(appModule: AppModule): Builder
        fun build(): AppComponent
    }

    fun inject(SSApplication: SSApplication)
}
