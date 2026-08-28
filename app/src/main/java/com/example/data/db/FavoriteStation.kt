package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stations")
data class FavoriteStation(
    @PrimaryKey val frequency: Float,
    val name: String,
    val callsign: String,
    val genre: String,
    val streamUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedFrequency: String
        get() = String.format("%.1f", frequency)
}
