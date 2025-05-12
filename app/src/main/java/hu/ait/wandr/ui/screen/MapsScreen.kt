package hu.ait.wandr.ui.screen

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import hu.ait.wandr.data.TravelPin
import hu.ait.wandr.ui.utils.getLocationNameFromCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Random
import androidx.compose.ui.text.input.ImeAction


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
                mapType = MapType.NORMAL,
                isTrafficEnabled = true,
                //mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.mymapconfig)
            )
        )
    }
    var addressText by rememberSaveable { mutableStateOf("") }
    Text(text = addressText)

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

    //TEST SEARCH FEATURE
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        //permission?

        //TEST SEARCH FEATURE
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search location") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),

            keyboardActions = KeyboardActions(
                onSearch = {
                    performSearch(
                        searchQuery,
                        context,
                        cameraState,
                        { clickedLocation = it },
                        { addressResult = it },
                        { showDialog = it }
                    )
                }
            ),
            trailingIcon = {
                IconButton(onClick = {
                    performSearch(
                        searchQuery,
                        context,
                        cameraState,
                        { clickedLocation = it },
                        { addressResult = it },
                        { showDialog = it }
                    )
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        )


        GoogleMap(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
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
            }
            //rest?
        ) {
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
                        mapsViewModel.startCompareNewPin(clickedLocation!!, noteText, photoUri)
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

        val newPinForCompare = mapsViewModel.newPinForComparison.value
        if (newPinForCompare != null) {
            val candidate = mapsViewModel.getCurrentComparisonCandidate()
            if (candidate != null) {
                CompareDialog(
                    newPin = newPinForCompare,
                    currentComparison = candidate,
                    onDecision = { isBetter ->
                        mapsViewModel.handleComparisonDecision(isBetter)
                    },
                    onDismiss = {
                        // insert or cancel the newPin if hit cancel,-> mapsViewModel.cancelComparison()?
                        // currently clear the newPin
                    }
                )
            }
        }

        // rest?
    }
}

@Composable
fun CompareDialog(
    newPin: TravelPin,
    currentComparison: TravelPin,
    onDecision: (isBetter: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val currentLocationName = remember(currentComparison) {
        getLocationNameFromCoordinates(context, currentComparison.latitude, currentComparison.longitude)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compare Places") },
        text = {
            Column {
                Text("Is your new place better than:")
                Spacer(Modifier.height(8.dp))
                Text(
                    currentLocationName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onDecision(true) }) {
                Text("Better")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDecision(false) }) {
                Text("Worse")
            }
        }
    )
}

//TEST SEARCH FEATURE
fun performSearch(
    query: String,
    context: Context,
    cameraState: CameraPositionState,
    setClickedLocation: (LatLng) -> Unit,
    setAddressResult: (String) -> Unit,
    setShowDialog: (Boolean) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())

    try {
        val addressList = geocoder.getFromLocationName(query, 1)
        if (!addressList.isNullOrEmpty()) {
            val address = addressList[0]
            val latLng = LatLng(address.latitude, address.longitude)

            // Animate camera
            CoroutineScope(Dispatchers.Main).launch {
                cameraState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                    durationMs = 2000
                )
            }

            // Trigger the same flow as map click
            setClickedLocation(latLng)
            setAddressResult(address.getAddressLine(0))
            setShowDialog(true)

        } else {
            Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}





