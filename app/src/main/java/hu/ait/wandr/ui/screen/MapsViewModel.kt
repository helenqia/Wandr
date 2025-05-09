package hu.ait.wandr.ui.screen

import android.location.Location
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.ait.wandr.data.TravelPin
import hu.ait.wandr.data.TravelRepository
import hu.ait.wandr.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    val locationManager: LocationManager,
    private val travelRepository: TravelRepository
) : ViewModel() {

    private val _allPins = MutableStateFlow<List<TravelPin>>(emptyList())
    val allPins: StateFlow<List<TravelPin>> = _allPins.asStateFlow()

    init {
        viewModelScope.launch {
            travelRepository.getAllPinsAsFlow().collect { pins ->
                _allPins.value = pins
            }
        }
    }

    // Location monitoring
    var locationState = mutableStateOf<Location?>(null)

    fun startLocationMonitoring() {
        viewModelScope.launch {
            locationManager
                .fetchUpdates()
                .collect {
                    locationState.value = it
                }
        }
    }

    fun addMarker(latLng: LatLng, note: String, photoUri: Uri?) {
        viewModelScope.launch {
            travelRepository.insertTravelPin(latLng, note, photoUri)
        }
    }

    // Convert TravelPin objects to format needed for the map display
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