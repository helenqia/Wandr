package hu.ait.wandr.ui.screen

import android.location.Location
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.ait.wandr.location.LocationManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    val locationManager: LocationManager
) : ViewModel() {

    // Now storing LatLng + note + optional photoUri
    private var _markerList = mutableStateListOf<Triple<LatLng, String, Uri?>>()

    fun getMarkersList(): List<Triple<LatLng, String, Uri?>> {
        return _markerList
    }

    fun addMarker(latLng: LatLng, note: String, photoUri: Uri?) {
        _markerList.add(Triple(latLng, note, photoUri))
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
}
