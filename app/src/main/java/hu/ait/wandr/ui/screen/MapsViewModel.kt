package hu.ait.wandr.ui.screen

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.multidex.BuildConfig
import androidx.multidex.BuildConfig.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.ait.wandr.data.TravelPin
import hu.ait.wandr.data.TravelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.internal.NoOpContinuation.context
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val travelRepository: TravelRepository
) : ViewModel() {

    private val _allPins = MutableStateFlow<List<TravelPin>>(emptyList())
    val allPins: StateFlow<List<TravelPin>> = _allPins.asStateFlow()

    init {
        viewModelScope.launch {
            if (DEBUG) {
                travelRepository.deleteAllPins()
            }

            travelRepository.getAllPinsRankedAsFlow().collect { pins ->
                _allPins.value = pins
            }
        }
    }

    private val _newPinForComparison = mutableStateOf<TravelPin?>(null)
    val newPinForComparison = _newPinForComparison

    private val _compareLow = mutableStateOf(0)
    private val _compareHigh = mutableStateOf(0)

    private val _currentMid = mutableStateOf(0)

    fun startCompareNewPin(latLng: LatLng, note: String, photoUri: Uri?) {

        val newPin = TravelPin(
            latitude = latLng.latitude,
            longitude = latLng.longitude,
            note = note,
            rating = 1200,
            photoUri = photoUri?.toString()
        )
        _newPinForComparison.value = newPin

        // no existing pins
        val currentPins = _allPins.value
        if (currentPins.isEmpty()) {
            insertNewPinAndClear(newPin)
        } else {
            _compareLow.value = 0
            _compareHigh.value = currentPins.size - 1
            _currentMid.value = (0 + (currentPins.size - 1)) / 2
        }
    }

    fun getCurrentComparisonCandidate(): TravelPin? {
        val newPin = _newPinForComparison.value ?: return null
        val pins = _allPins.value
        if (pins.isEmpty()) return null
        val low = _compareLow.value
        val high = _compareHigh.value
        // Choose the middle candidate.
        val mid = _currentMid.value
        return pins.getOrNull(mid)
    }

    // doesn't work- need to fix
    fun handleComparisonDecision(isBetter: Boolean) {
        val newPin = _newPinForComparison.value ?: return
        val mid = _currentMid.value
        val pins = _allPins.value
        val midRating = pins[mid].rating

        if (isBetter) {
            _compareHigh.value = mid - 1
            newPin.rating = midRating + 1
        } else {
            _compareLow.value = mid + 1
            newPin.rating = midRating - 1
        }

        val newLow = _compareLow.value
        val newHigh = _compareHigh.value

        if (newLow > newHigh) {
            insertNewPinAndClear(newPin)
        } else {
            _currentMid.value = (newLow + newHigh) / 2
        }
    }

    private fun insertNewPinAndClear(newPin: TravelPin) {
        viewModelScope.launch {
            val insertIndex = _compareLow.value
            val pins = _allPins.value.sortedByDescending { it.rating } // enforce sorted order

            val newRating = when {
                pins.isEmpty() -> 1200
                insertIndex == 0 -> pins.first().rating + 100 // ensure it's higher than highest
                insertIndex >= pins.size -> pins.last().rating - 100 // lower than lowest
                else -> {
                    val high = pins[insertIndex - 1].rating
                    val low = pins[insertIndex].rating
                    (high + low) / 2
                }
            }

            newPin.rating = newRating
            travelRepository.insertTravelPin(newPin)
            _newPinForComparison.value = null
        }
    }


    /*fun getLocationName(context: Context, latLng: LatLng): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                address.featureName ?: address.locality ?: address.adminArea ?: "Unknown Location"
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            "Unknown Location"
        }
    }*/

    //var locationState = mutableStateOf<Location?>(null)

    /*fun startLocationMonitoring() {
        viewModelScope.launch {
            locationManager
                .fetchUpdates()
                .collect {
                    locationState.value = it
                }
        }
    }*/

    /*fun addMarker(latLng: LatLng, note: String, photoUri: Uri?) {
        viewModelScope.launch {
            travelRepository.insertTravelPin(latLng, note, photoUri)
        }
    }*/

    fun getMarkersList(): List<Triple<LatLng, String, Uri?>> {
        return _allPins.value.map { pin ->
            Triple(
                LatLng(pin.latitude, pin.longitude),
                pin.note,
                pin.photoUri?.let { Uri.parse(it) }
            )
        }
    }
}
