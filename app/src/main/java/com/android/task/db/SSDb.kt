package com.android.task.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.task.vo.Zone

/**
 * Main database description.
 */
@Database(
    entities = [
        Zone::class],
    version = 1,
    exportSchema = false
)
abstract class SSDb : RoomDatabase() {

    abstract fun zoneDao(): ZoneDao
}
