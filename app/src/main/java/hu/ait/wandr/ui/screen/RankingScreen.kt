package hu.ait.wandr.ui.screen

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import hu.ait.wandr.data.TravelPin

@Composable
fun RankingScreen(
    modifier: Modifier = Modifier,
    onViewAllRankings: () -> Unit,
    rankingViewModel: RankingViewModel = hiltViewModel()
) {
    val pairForComparison by rankingViewModel.pairForComparison.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Which place do you prefer?",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (pairForComparison.size == 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PlaceComparisonCard(
                        place = pairForComparison[0],
                        modifier = Modifier.weight(1f),
                        onClick = {
                            rankingViewModel.selectBetterPlace(
                                pairForComparison[0].id,
                                pairForComparison[1].id
                            )
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    PlaceComparisonCard(
                        place = pairForComparison[1],
                        modifier = Modifier.weight(1f),
                        onClick = {
                            rankingViewModel.selectBetterPlace(
                                pairForComparison[1].id,
                                pairForComparison[0].id
                            )
                        }
                    )
                }
            } else {
                Text(
                    text = "Add at least two places to start comparing!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { rankingViewModel.loadRandomPairForComparison() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Another Pair")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onViewAllRankings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View All Rankings")
            }
        }
    }
}

@Composable
fun PlaceComparisonCard(
    place: TravelPin,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mini map preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val cameraPosition = rememberCameraPositionState {
                    CameraPosition.fromLatLngZoom(
                        LatLng(place.latitude, place.longitude), 13f
                    )
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPosition,
                    properties = MapProperties(mapType = MapType.NORMAL),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        scrollGesturesEnabled = false,
                        zoomGesturesEnabled = false
                    )
                ) {
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude))
                    )
                }
            }

            // Place details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = place.note,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Rating: ${place.rating}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Photo if available
                place.photoUri?.let { uriString ->
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = Uri.parse(uriString),
                        contentDescription = "Place photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}