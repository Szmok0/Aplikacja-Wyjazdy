package com.rodzina.wyjazdy.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.rodzina.wyjazdy.data.model.Trip
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.ui.theme.personHueForMarker
import com.google.firebase.Timestamp

private const val DEFAULT_LAT = 52.0
private const val DEFAULT_LNG = 19.0

@Composable
fun MapScreen(
    trips: List<Trip>,
    members: List<User>,
    onOpenTrip: (String) -> Unit,
) {
    val memberUserIds = members.map { it.id }
    val now = Timestamp.now()
    val pins = remember(trips) {
        trips.filter { it.dateEnd >= now && it.venueLat != null && it.venueLng != null }
    }

    val cameraPositionState = rememberCameraPositionState {
        val first = pins.firstOrNull()
        position = if (first?.venueLat != null && first.venueLng != null) {
            CameraPosition.fromLatLngZoom(LatLng(first.venueLat, first.venueLng), 6f)
        } else {
            CameraPosition.fromLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), 5f)
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Mapa") }) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (pins.isEmpty()) {
                Text(
                    text = "Brak wyjazdów z zapisaną lokalizacją",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
            ) {
                pins.forEach { trip ->
                    val lat = trip.venueLat ?: return@forEach
                    val lng = trip.venueLng ?: return@forEach
                    val owner = members.firstOrNull { it.id == trip.ownerUserId }
                    Marker(
                        state = remember(trip.id) { MarkerState(position = LatLng(lat, lng)) },
                        title = "${trip.city} - ${owner?.displayName.orEmpty()}",
                        snippet = trip.eventTitle,
                        icon = BitmapDescriptorFactory.defaultMarker(personHueForMarker(trip.ownerUserId, memberUserIds)),
                        onInfoWindowClick = { onOpenTrip(trip.id) },
                    )
                }
            }
        }
    }
}
