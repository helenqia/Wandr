package hu.ait.wandr.ui.screen

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import hu.ait.wandr.data.TravelPin
import hu.ait.wandr.ui.utils.getLocationNameFromCoordinates
import java.util.Locale

@Composable
fun RankedListScreen(
    modifier: Modifier = Modifier,
    rankingViewModel: RankingViewModel = hiltViewModel()
) {
    val rankedPlaces by rankingViewModel.rankedPlaces.collectAsState()
    var clickedLocation by remember { mutableStateOf<LatLng?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Your Ranked Places",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (rankedPlaces.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(rankedPlaces) { index, place ->
                        RankedPlaceItem(
                            rank = index + 1,
                            place = place,
                            onDelete = { rankingViewModel.deletePin(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No places added yet. Add some pins on the map!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun RankedPlaceItem(
    rank: Int,
    place: TravelPin,
    onDelete: (TravelPin) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationName by remember(place.latitude, place.longitude) {
        mutableStateOf(
            getLocationNameFromCoordinates(context, place.latitude, place.longitude)
        )
    }
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = when(rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFC0C0C0) // Silver
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = rank.toString(),
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Place details + optional photo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = locationName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1
                )

                /*Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Coordinates: ${String.format("%.4f", place.latitude)}, ${String.format("%.4f", place.longitude)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )*/

            }

            // Photo thumbnail if available
            place.photoUri?.let { uriString ->
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = Uri.parse(uriString),
                    contentDescription = "Place photo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            IconButton(onClick = { onDelete(place) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Pin"
                )
            }
        }
    }
}