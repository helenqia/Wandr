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

    suspend fun insertTravelPin(travelPin: TravelPin): Long {
        return travelPinDao.insert(travelPin)
    }

    suspend fun deletePin(pin: TravelPin) {
        travelPinDao.delete(pin)
    }

    suspend fun deleteAllPins() {
        travelPinDao.deleteAllPins()
    }
}