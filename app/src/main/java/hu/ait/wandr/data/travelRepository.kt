package hu.ait.wandr.data

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TravelRepository @Inject constructor(
    private val travelPinDao: TravelPinDao
) {
    fun getAllPinsAsFlow(): Flow<List<TravelPin>> {
        return travelPinDao.getAllAsFlow()
    }

    fun getAllPinsRankedAsFlow(): Flow<List<TravelPin>> {
        return travelPinDao.getAllRankedAsFlow()
    }

    suspend fun getRandomPairForComparison(): List<TravelPin> {
        return travelPinDao.getRandomPairForComparison()
    }

    suspend fun insertTravelPin(
        latLng: LatLng,
        note: String,
        photoUri: Uri?,
        rating: Int = 1000 // Default ELO rating
    ): Long {
        val travelPin = TravelPin(
            latitude = latLng.latitude,
            longitude = latLng.longitude,
            note = note,
            rating = rating,
            photoUri = photoUri?.toString()
        )
        return travelPinDao.insert(travelPin)
    }

    suspend fun updatePinRating(id: Int, newRating: Int) {
        val pin = travelPinDao.getById(id) ?: return
        travelPinDao.update(pin.copy(rating = newRating))
    }

    // ELO rating system for comparing two places
    suspend fun compareAndUpdateRatings(winnerId: Int, loserId: Int) {
        val winner = travelPinDao.getById(winnerId) ?: return
        val loser = travelPinDao.getById(loserId) ?: return

        // Calculate new ELO ratings
        val kFactor = 32 // Factor determining how much ratings change
        val expectedWinnerScore = 1.0 / (1.0 + Math.pow(10.0, (loser.rating - winner.rating) / 400.0))
        val expectedLoserScore = 1.0 / (1.0 + Math.pow(10.0, (winner.rating - loser.rating) / 400.0))

        val newWinnerRating = (winner.rating + kFactor * (1 - expectedWinnerScore)).toInt()
        val newLoserRating = (loser.rating + kFactor * (0 - expectedLoserScore)).toInt()

        travelPinDao.update(winner.copy(rating = newWinnerRating))
        travelPinDao.update(loser.copy(rating = newLoserRating))
    }

    suspend fun deletePin(pin: TravelPin) {
        travelPinDao.delete(pin)
    }
}