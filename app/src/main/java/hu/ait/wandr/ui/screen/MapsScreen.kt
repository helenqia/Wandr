package hu.ait.wandr.ui.screen

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.CameraUpdateFactory
import hu.ait.wandr.R
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Random

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapsScreen(
    modifier: Modifier,
    mapsViewModel: MapsViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var cameraState = rememberCameraPositionState {
        CameraPosition.fromLatLngZoom(LatLng(47.0, 19.0), 10f)
    }
    var uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = true, zoomGesturesEnabled = true)) }
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.SATELLITE,
                isTrafficEnabled = true,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.mymapconfig)
            )
        )
    }

    var clickedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var addressResult by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // For displaying photo when marker clicked
    var showPhotoDialog by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> photoUri = uri }
    )

    Column(modifier = modifier.fillMaxSize()) {
        val fineLocationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

        if (fineLocationPermissionState.status.isGranted) {
            Column {
                Button(onClick = { mapsViewModel.startLocationMonitoring() }) {
                    Text(text = "Start location monitoring")
                }
                Text(text = "Location: ${getLocationText(mapsViewModel.locationState.value)}")
            }
        } else {
            val permissionText = if (fineLocationPermissionState.status.shouldShowRationale) {
                "Please consider giving permission"
            } else {
                "Give permission for location"
            }
            Text(text = permissionText)
            Button(onClick = { fineLocationPermissionState.launchPermissionRequest() }) {
                Text(text = "Request permission")
            }
        }

        var isSatellite by remember { mutableStateOf(true) }
        Switch(
            checked = isSatellite,
            onCheckedChange = {
                isSatellite = it
                mapProperties = mapProperties.copy(mapType = if (it) MapType.SATELLITE else MapType.NORMAL)
            }
        )

        var addressText by rememberSaveable { mutableStateOf("N/A") }
        Text(text = addressText)

        GoogleMap(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
            cameraPositionState = cameraState,
            uiSettings = uiSettings,
            properties = mapProperties,
            onMapClick = { clickCoordinate ->
                clickedLocation = clickCoordinate
                addressResult = "Loading address..."

                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(
                        clickCoordinate.latitude,
                        clickCoordinate.longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                addressResult = addresses.firstOrNull()?.getAddressLine(0) ?: "Unknown address"
                            }
                            override fun onError(errorMessage: String?) {
                                addressResult = "Error: $errorMessage"
                            }
                        }
                    )
                } else {
                    try {
                        val addresses = geocoder.getFromLocation(clickCoordinate.latitude, clickCoordinate.longitude, 1)
                        addressResult = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown address"
                    } catch (e: Exception) {
                        addressResult = "Error: ${e.message}"
                    }
                }

                showDialog = true
            },
            onMapLongClick = { clickCoordinate ->
                val random = Random(System.currentTimeMillis())
                val cameraPosition = CameraPosition.Builder()
                    .target(clickCoordinate)
                    .zoom(1f + random.nextInt(5))
                    .tilt(30f + random.nextInt(15))
                    .bearing(-45f + random.nextInt(90))
                    .build()
                coroutineScope.launch {
                    cameraState.animate(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000)
                }
            }
        ) {
            Marker(
                state = MarkerState(position = LatLng(47.0, 19.0)),
                title = "Marker AIT",
                snippet = "Marker long text, lorem ipsum...",
                draggable = true,
                alpha = 0.5f
            )

            for ((position, note, markerPhotoUri) in mapsViewModel.getMarkersList()) {
                Marker(
                    state = MarkerState(position = position),
                    title = "Marker",
                    snippet = "Note: $note",
                    onClick = {
                        if (markerPhotoUri != null) {
                            selectedPhotoUri = markerPhotoUri
                            showPhotoDialog = true
                        }
                        val geocoder = Geocoder(context, Locale.getDefault())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(
                                it.position.latitude,
                                it.position.longitude,
                                3,
                                object : Geocoder.GeocodeListener {
                                    override fun onGeocode(addrs: MutableList<Address>) {
                                        val addr = "${addrs[0].getAddressLine(0)}, ${addrs[0].getAddressLine(1)}, ${addrs[0].getAddressLine(2)}"
                                        addressText = addr
                                    }
                                    override fun onError(errorMessage: String?) {
                                        addressText = errorMessage ?: "Error"
                                    }
                                })
                        }
                        true
                    }
                )
            }
        }

        if (showDialog && clickedLocation != null) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    noteText = ""
                    addressResult = null
                    photoUri = null
                },
                title = { Text("Add Note for Location") },
                text = {
                    Column {
                        Text(addressResult ?: "Lat: ${clickedLocation!!.latitude}, Lng: ${clickedLocation!!.longitude}")
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("Add a note") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { photoPickerLauncher.launch("image/*") }) {
                            Text("Attach Photo")
                        }
                        photoUri?.let { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected photo",
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        mapsViewModel.addMarker(clickedLocation!!, noteText, photoUri)
                        showDialog = false
                        noteText = ""
                        addressResult = null
                        photoUri = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDialog = false
                        noteText = ""
                        addressResult = null
                        photoUri = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showPhotoDialog && selectedPhotoUri != null) {
            AlertDialog(
                onDismissRequest = { showPhotoDialog = false },
                title = { Text("Attached Photo") },
                text = {
                    AsyncImage(
                        model = selectedPhotoUri,
                        contentDescription = "Attached photo",
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                },
                confirmButton = {
                    Button(onClick = { showPhotoDialog = false }) { Text("Close") }
                }
            )
        }
    }
}

fun getLocationText(location: Location?): String {
    return """
       Lat: ${location?.latitude}
       Lng: ${location?.longitude}
       Alt: ${location?.altitude}
       Speed: ${location?.speed}
       Accuracy: ${location?.accuracy}
    """.trimIndent()
}
