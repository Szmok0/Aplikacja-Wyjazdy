package com.rodzina.wyjazdy.ui.triplist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.rodzina.wyjazdy.data.model.Trip
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.ui.common.SectionHeader
import com.rodzina.wyjazdy.ui.common.TripCard

@Composable
fun TripListScreen(
    trips: List<Trip>,
    members: List<User>,
    currentUserId: String,
    onAddTrip: () -> Unit,
    onOpenTrip: (String) -> Unit,
) {
    var filterUserId by remember { mutableStateOf<String?>(null) }
    val memberUserIds = members.map { it.id }
    val membersById = members.associateBy { it.id }

    val filteredTrips = if (filterUserId == null) trips else trips.filter { it.ownerUserId == filterUserId }
    val now = Timestamp.now()
    val upcoming = filteredTrips.filter { it.dateEnd >= now }.sortedBy { it.dateStart.seconds }
    val history = filteredTrips.filter { it.dateEnd < now }.sortedByDescending { it.dateStart.seconds }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Wyjazdy") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTrip) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj wyjazd")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            FilterChip(
                                selected = filterUserId == null,
                                onClick = { filterUserId = null },
                                label = { Text("Wszyscy") },
                            )
                        }
                        items(members) { member ->
                            val label = if (member.id == currentUserId) "Ja" else member.displayName.ifBlank { "?" }
                            FilterChip(
                                selected = filterUserId == member.id,
                                onClick = { filterUserId = member.id },
                                label = { Text(label) },
                            )
                        }
                    }
                }

                item { SectionHeader("Nadchodzące") }
                if (upcoming.isEmpty()) {
                    item { EmptyRow("Brak nadchodzących wyjazdów") }
                }
                items(upcoming, key = { "up_${it.id}" }) { trip ->
                    TripCard(
                        trip = trip,
                        owner = membersById[trip.ownerUserId],
                        memberUserIds = memberUserIds,
                        onClick = { onOpenTrip(trip.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }

                item { SectionHeader("Historia") }
                if (history.isEmpty()) {
                    item { EmptyRow("Brak zakończonych wyjazdów") }
                }
                items(history, key = { "hist_${it.id}" }) { trip ->
                    TripCard(
                        trip = trip,
                        owner = membersById[trip.ownerUserId],
                        memberUserIds = memberUserIds,
                        onClick = { onOpenTrip(trip.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRow(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
