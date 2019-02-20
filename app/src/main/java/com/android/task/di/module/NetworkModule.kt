package com.android.task.di.module

import android.app.Application
import androidx.room.Room
import com.android.task.db.SSDb
import com.android.task.db.ZoneDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@Suppress("unused")
class NetworkModule {

    @Singleton
    @Provides
    fun provideDb(app: Application): SSDb {
        return Room
            .databaseBuilder(app, SSDb::class.java, "slate_studio.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideUserDao(db: SSDb): ZoneDao {
        return db.zoneDao()
    }
}