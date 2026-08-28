package com.example

import com.example.data.RadioRepository
import com.example.data.db.FavoriteStation
import com.example.data.db.FavoriteStationDao
import com.example.model.RadioStation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadioStationTest {

    private val fakeDao = object : FavoriteStationDao {
        val list = mutableListOf<FavoriteStation>()
        override fun getAllFavorites(): Flow<List<FavoriteStation>> = flowOf(list)
        override fun getFavoriteByFrequency(freq: Float): Flow<FavoriteStation?> =
            flowOf(list.find { kotlin.math.abs(it.frequency - freq) < 0.06f })
        override fun isFavorite(freq: Float): Flow<Boolean> =
            flowOf(list.any { kotlin.math.abs(it.frequency - freq) < 0.06f })
        override suspend fun insert(station: FavoriteStation) { list.add(station) }
        override suspend fun deleteByFrequency(freq: Float) {
            list.removeAll { kotlin.math.abs(it.frequency - freq) < 0.06f }
        }
        override suspend fun delete(station: FavoriteStation) { list.remove(station) }
        override suspend fun getCount(): Int = list.size
    }

    private val repository = RadioRepository(fakeDao)

    @Test
    fun testFindStationAtFrequency() {
        val station = repository.findStationAt(98.3f)
        assertNotNull(station)
        assertEquals("Radio Mirchi 98.3", station?.name)
        assertEquals("98.3", station?.formattedFrequency)
    }

    @Test
    fun testFindStationOffFrequencyReturnsNull() {
        val offFreq = repository.findStationAt(89.0f)
        assertNull(offFreq)
    }

    @Test
    fun testScanNextStationForward() {
        val nextStation = repository.scanNextStation(93.5f, forward = true)
        assertNotNull(nextStation)
        assertTrue(nextStation.frequency > 93.5f)
        assertEquals(94.3f, nextStation.frequency)
    }

    @Test
    fun testScanNextStationBackward() {
        val prevStation = repository.scanNextStation(93.5f, forward = false)
        assertNotNull(prevStation)
        assertTrue(prevStation.frequency < 93.5f)
        assertEquals(92.7f, prevStation.frequency)
    }

    @Test
    fun testScanWrapAround() {
        val wrappedStation = repository.scanNextStation(107.1f, forward = true)
        assertNotNull(wrappedStation)
        assertEquals(91.1f, wrappedStation.frequency)
    }
}
