package com.android.task.vo

import androidx.room.Entity
import org.jetbrains.annotations.NotNull


@Entity(primaryKeys = ["latitude", "longitude"])
data class Zone(
    @NotNull
    val latitude: String,
    @NotNull
    val longitude: String,
    val radius: String,
    val wifi: String
)
