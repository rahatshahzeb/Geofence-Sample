package com.android.task.vo

import androidx.room.Entity

@Entity(primaryKeys = ["latitude", "longitude"])
data class Zone(
    val latitude: String,
    val longitude: String?,
    val radius: String?,
    val wifi: String?
)
