package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {
    @Query("SELECT * FROM favorite_stations ORDER BY frequency ASC")
    fun getAllFavorites(): Flow<List<FavoriteStation>>

    @Query("SELECT * FROM favorite_stations WHERE ABS(frequency - :freq) < 0.06 LIMIT 1")
    fun getFavoriteByFrequency(freq: Float): Flow<FavoriteStation?>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE ABS(frequency - :freq) < 0.06)")
    fun isFavorite(freq: Float): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: FavoriteStation)

    @Query("DELETE FROM favorite_stations WHERE ABS(frequency - :freq) < 0.06")
    suspend fun deleteByFrequency(freq: Float)

    @Delete
    suspend fun delete(station: FavoriteStation)

    @Query("SELECT COUNT(*) FROM favorite_stations")
    suspend fun getCount(): Int
}
