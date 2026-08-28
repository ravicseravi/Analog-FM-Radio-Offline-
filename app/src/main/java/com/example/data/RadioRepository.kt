package com.example.data

import com.example.data.db.FavoriteStation
import com.example.data.db.FavoriteStationDao
import com.example.model.RadioStation
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs

class RadioRepository(
    private val favoriteStationDao: FavoriteStationDao
) {
    // Authentic catalog of Indian FM radio stations across standard 87.5 - 108.0 MHz band (100 kHz raster)
    val stationsCatalog: List<RadioStation> = listOf(
        RadioStation(
            frequency = 91.1f,
            name = "Radio City 91.1",
            callsign = "CITY-91.1",
            genre = "Bollywood & Indie Hits",
            city = "Mumbai / Delhi / Bengaluru",
            signalStrength = 97,
            rdsSample = "Radio City 91.1 • Rag Rag Mein Daude City • Playing: Kesariya",
            streamUrl = "https://eu8.fastcast4u.com/proxy/clyedupq/stream",
            fallbackUrl = "http://stream.radiobollyfm.in:8201/hd"
        ),
        RadioStation(
            frequency = 92.7f,
            name = "BIG FM 92.7",
            callsign = "BIG-92.7",
            genre = "Retro 90s & 2000s Bollywood",
            city = "Pan-India Broadcast",
            signalStrength = 96,
            rdsSample = "BIG FM 92.7 • Suno Sunao, Life Banao • Playing: Kuch Kuch Hota Hai",
            streamUrl = "https://stream.zeno.fm/rm4i9pdex3cuv",
            fallbackUrl = "http://stream.zeno.fm/8ty8szwpwfeuv"
        ),
        RadioStation(
            frequency = 93.5f,
            name = "Red FM 93.5",
            callsign = "RED-93.5",
            genre = "Superhit Bollywood & Youth Beats",
            city = "Delhi / Mumbai / Kolkata",
            signalStrength = 99,
            rdsSample = "Red FM 93.5 • Bajaate Raho! • Playing: Jhoome Jo Pathaan",
            streamUrl = "http://stream.zeno.fm/8ty8szwpwfeuv",
            fallbackUrl = "https://drive.uber.radio/uber/bollywoodnow/icecast.audio"
        ),
        RadioStation(
            frequency = 94.3f,
            name = "MY FM 94.3",
            callsign = "MYFM-94.3",
            genre = "Contemporary Hindi Pop & Chartbusters",
            city = "Jaipur / Ahmedabad / Chandigarh",
            signalStrength = 93,
            rdsSample = "MY FM 94.3 • Jiyo Dil Se! • Playing: Lut Gaye",
            streamUrl = "http://stream.radiobollyfm.in:8201/hd",
            fallbackUrl = "https://eu8.fastcast4u.com/proxy/clyedupq/stream"
        ),
        RadioStation(
            frequency = 95.0f,
            name = "Mirchi Love 95.0",
            callsign = "LOVE-95.0",
            genre = "Romantic Hindi Melodies",
            city = "Pan-India Metro",
            signalStrength = 94,
            rdsSample = "Mirchi Love 95.0 • Meethi Mirchi • Playing: Jeena Jeena",
            streamUrl = "https://drive.uber.radio/uber/bollywoodlove/icecast.audio",
            fallbackUrl = "https://stream.zeno.fm/6n6ewddtad0uv"
        ),
        RadioStation(
            frequency = 98.3f,
            name = "Radio Mirchi 98.3",
            callsign = "MRCH-98.3",
            genre = "Bollywood Top 20 & New Releases",
            city = "Mumbai / Delhi / Pune / Hyderabad",
            signalStrength = 100,
            rdsSample = "Radio Mirchi 98.3 • Mirchi Sunnewaale Always Khush! • Playing: Deewani Mastani",
            streamUrl = "https://drive.uber.radio/uber/bollywoodnow/icecast.audio",
            fallbackUrl = "https://eu8.fastcast4u.com/proxy/clyedupq/stream"
        ),
        RadioStation(
            frequency = 100.7f,
            name = "AIR FM Rainbow 100.7",
            callsign = "RAINBOW-100.7",
            genre = "Hindi Film Classics & Pop",
            city = "All India Radio National Network",
            signalStrength = 98,
            rdsSample = "AIR FM Rainbow 100.7 • Akashvani • Playing: Lag Ja Gale",
            streamUrl = "https://stream.zeno.fm/6n6ewddtad0uv",
            fallbackUrl = "http://stream.zeno.fm/g95zm67prfhvv"
        ),
        RadioStation(
            frequency = 102.6f,
            name = "AIR FM Gold 102.6",
            callsign = "GOLD-102.6",
            genre = "Golden Era Classics & Ghazals",
            city = "Prasar Bharati Delhi / Mumbai",
            signalStrength = 95,
            rdsSample = "AIR FM Gold 102.6 • Akashvani • Playing: Pal Pal Dil Ke Paas",
            streamUrl = "https://azuracast.vibesounds.in:8010/radio.mp3",
            fallbackUrl = "http://stream.zeno.fm/pg6rqp8ztm0uv"
        ),
        RadioStation(
            frequency = 104.0f,
            name = "Fever 104 FM",
            callsign = "FEVR-104.0",
            genre = "Baap of Bollywood Blockbusters",
            city = "Delhi / Mumbai / Bengaluru",
            signalStrength = 96,
            rdsSample = "Fever 104 FM • Baap Bol Raha Hai • Playing: Aankhon Mein Teri",
            streamUrl = "https://radio.canstream.co.uk:8115/live.mp3",
            fallbackUrl = "https://drive.uber.radio/uber/bollywoodnow/icecast.audio"
        ),
        RadioStation(
            frequency = 104.8f,
            name = "Ishq 104.8 FM",
            callsign = "ISHQ-104.8",
            genre = "Soulful Romantic Bollywood",
            city = "Delhi / Mumbai / Kolkata",
            signalStrength = 92,
            rdsSample = "Ishq 104.8 FM • Do The Ishq Baby • Playing: Likhe Jo Khat Tujhe",
            streamUrl = "http://stream.zeno.fm/0zkr7x8ztm0uv",
            fallbackUrl = "https://drive.uber.radio/uber/bollywoodlove/icecast.audio"
        ),
        RadioStation(
            frequency = 106.4f,
            name = "Magic FM 106.4",
            callsign = "MAGIC-106.4",
            genre = "Bollywood Dance & Party Hits",
            city = "Mumbai / Pune Metro",
            signalStrength = 94,
            rdsSample = "Magic FM 106.4 • Jee Le Zindagi • Playing: Kal Ho Naa Ho",
            streamUrl = "http://stream.zeno.fm/8ty8szwpwfeuv",
            fallbackUrl = "http://stream.radiobollyfm.in:8201/hd"
        ),
        RadioStation(
            frequency = 107.1f,
            name = "AIR Vividh Bharati 107.1",
            callsign = "VBS-107.1",
            genre = "Evergreen Classics & Chhaya Geet",
            city = "Prasar Bharati Commercial Service",
            signalStrength = 99,
            rdsSample = "AIR Vividh Bharati • Desh Ki Swar Lahari • Sangeet Sarita",
            streamUrl = "http://stream.zeno.fm/g95zm67prfhvv",
            fallbackUrl = "https://stream.zeno.fm/6n6ewddtad0uv"
        )
    )

    fun getAllStations(): List<RadioStation> = stationsCatalog

    fun getFavorites(): Flow<List<FavoriteStation>> = favoriteStationDao.getAllFavorites()

    fun isFavorite(frequency: Float): Flow<Boolean> = favoriteStationDao.isFavorite(frequency)

    suspend fun saveFavorite(station: FavoriteStation) {
        favoriteStationDao.insert(station)
    }

    suspend fun deleteFavorite(station: FavoriteStation) {
        favoriteStationDao.delete(station)
    }

    suspend fun deleteFavoriteByFrequency(frequency: Float) {
        favoriteStationDao.deleteByFrequency(frequency)
    }

    /**
     * Seeds initial default favorites if user has none saved.
     */
    suspend fun seedDefaultsIfEmpty() {
        if (favoriteStationDao.getCount() == 0) {
            // Seed beloved Indian FM stations initially into Quick Access / Favorites
            val defaultStations = listOf(
                stationsCatalog.firstOrNull { it.frequency == 98.3f }, // Radio Mirchi
                stationsCatalog.firstOrNull { it.frequency == 93.5f }, // Red FM
                stationsCatalog.firstOrNull { it.frequency == 91.1f }, // Radio City
                stationsCatalog.firstOrNull { it.frequency == 107.1f }, // AIR Vividh Bharati
                stationsCatalog.firstOrNull { it.frequency == 92.7f }, // BIG FM
                stationsCatalog.firstOrNull { it.frequency == 102.6f }  // AIR FM Gold
            ).filterNotNull()

            for (st in defaultStations) {
                favoriteStationDao.insert(
                    FavoriteStation(
                        frequency = st.frequency,
                        name = st.name,
                        callsign = st.callsign,
                        genre = st.genre,
                        streamUrl = st.streamUrl
                    )
                )
            }
        }
    }

    /**
     * Finds a station at or very close to the given frequency (within +/- 0.05 MHz).
     */
    fun findStationAt(frequency: Float): RadioStation? {
        return stationsCatalog.minByOrNull { abs(it.frequency - frequency) }?.let { closest ->
            if (abs(closest.frequency - frequency) <= 0.06f) closest else null
        }
    }

    /**
     * Finds the next available station in the given direction (1 for up, -1 for down).
     * Wraps around the 87.5 to 108.0 MHz FM band.
     */
    fun scanNextStation(currentFrequency: Float, forward: Boolean = true): RadioStation {
        val sorted = stationsCatalog.sortedBy { it.frequency }
        if (sorted.isEmpty()) {
            return RadioStation(
                frequency = 100.0f,
                name = "FM 100.0",
                callsign = "KSTN",
                genre = "FM Radio"
            )
        }

        return if (forward) {
            sorted.firstOrNull { it.frequency > currentFrequency + 0.05f } ?: sorted.first()
        } else {
            sorted.lastOrNull { it.frequency < currentFrequency - 0.05f } ?: sorted.last()
        }
    }
}
