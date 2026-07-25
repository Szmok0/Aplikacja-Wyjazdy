package com.rodzina.wyjazdy.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.rodzina.wyjazdy.data.model.Trip

@Composable
fun RouteMapScreen(trip: Trip?, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip?.venueAddress ?: "Mapa") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
            )
        },
    ) { padding ->
        val lat = trip?.venueLat
        val lng = trip?.venueLng
        if (lat == null || lng == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Brak zapisanej lokalizacji dla tego wyjazdu")
            }
            return@Scaffold
        }
        val position = LatLng(lat, lng)
        val cameraPositionState = rememberCameraPositionState {
            this.position = CameraPosition.fromLatLngZoom(position, 15f)
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize().padding(padding),
            cameraPositionState = cameraPositionState,
        ) {
            Marker(
                state = remember(lat, lng) { MarkerState(position = position) },
                title = trip.venueAddress,
                snippet = trip.eventTitle,
            )
        }
    }
}
