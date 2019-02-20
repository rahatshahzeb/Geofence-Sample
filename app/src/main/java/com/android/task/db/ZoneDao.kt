package com.android.task.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.task.vo.Zone

/**
 * Interface for database access for Zone related operations.
 */
@Dao
interface ZoneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(zone: Zone)

    @Query("SELECT * FROM zone WHERE latitude = :latitude AND longitude = :longitude")
    fun findByLatitudeLongitutde(latitude: String, longitude: String): LiveData<Zone>

    @Query("SELECT * FROM zone")
    fun findZones(): LiveData<List<Zone>>
}
