package com.rodzina.wyjazdy.ui.tripdetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodzina.wyjazdy.data.model.Trip
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.di.AppContainer
import com.rodzina.wyjazdy.ui.common.AvatarBadge
import com.rodzina.wyjazdy.ui.common.MiniMap
import com.rodzina.wyjazdy.ui.common.StatusChip
import com.rodzina.wyjazdy.ui.common.transportIcon
import com.rodzina.wyjazdy.util.formatDateRange
import com.rodzina.wyjazdy.util.formatDateTime

@Composable
fun TripDetailsScreen(
    container: AppContainer,
    familyId: String,
    trip: Trip?,
    members: List<User>,
    currentUserId: String,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenRouteMap: () -> Unit,
) {
    if (trip == null) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Wyjazd") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
            )
        }) { padding ->
            Text(
                text = "Ten wyjazd już nie istnieje.",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
        }
        return
    }

    val viewModel: TripDetailsViewModel = viewModel(
        key = trip.id,
        factory = TripDetailsViewModel.factory(familyId, trip.id, container.tripRepository),
    )
    val statusUpdates by viewModel.statusUpdates.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val owner = members.firstOrNull { it.id == trip.ownerUserId }
    val memberUserIds = members.map { it.id }
    val canEdit = isAdmin || trip.ownerUserId == currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip.city) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = {
                        val shareText = buildString {
                            append(trip.eventTitle.ifBlank { trip.eventType.label })
                            append(" - ${trip.city}\n")
                            append(formatDateRange(trip.dateStart, trip.dateEnd))
                            if (trip.venueAddress.isNotBlank()) append("\n${trip.venueAddress}")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Udostępnij wyjazd"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Udostępnij")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (owner != null) AvatarBadge(owner, memberUserIds)
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = trip.eventTitle.ifBlank { trip.eventType.label }, style = MaterialTheme.typography.titleLarge)
                    Text(text = formatDateRange(trip.dateStart, trip.dateEnd), style = MaterialTheme.typography.bodyMedium)
                }
                StatusChip(status = trip.status)
            }

            val lat = trip.venueLat
            val lng = trip.venueLng
            if (lat != null && lng != null) {
                Card {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Trasa przejazdu", style = MaterialTheme.typography.titleSmall)
                        MiniMap(lat = lat, lng = lng, title = trip.venueAddress, onClick = onOpenRouteMap)
                        TextButton(onClick = onOpenRouteMap) { Text("Otwórz mapę") }
                    }
                }
            }

            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(icon = { Icon(transportIcon(trip.transportType), null) }, label = "Transport", value = trip.transportType.label)
                    DetailRow(label = "Rodzaj wydarzenia", value = trip.eventType.label)
                    if (trip.venueAddress.isNotBlank()) {
                        DetailRow(label = "Miejsce szkolenia", value = trip.venueAddress)
                    }
                    trip.hotel?.let { hotel ->
                        val hotelValue = buildString {
                            if (hotel.name.isNotBlank()) append(hotel.name)
                            if (hotel.address.isNotBlank()) append(if (isEmpty()) hotel.address else ", ${hotel.address}")
                            if (hotel.checkIn != null && hotel.checkOut != null) {
                                append("\n${formatDateTime(hotel.checkIn)} – ${formatDateTime(hotel.checkOut)}")
                            }
                        }
                        DetailRow(label = "Nocleg", value = hotelValue.ifBlank { "Tak" })
                    }
                    trip.ticketInfo?.let { ticket ->
                        Column {
                            Text("Bilet", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (ticket.number.isNotBlank()) Text("Numer: ${ticket.number}")
                            if (ticket.fileUrl.isNotBlank()) {
                                TextButton(onClick = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ticket.fileUrl)))
                                }) { Text("Otwórz bilet") }
                            }
                        }
                    }
                    if (trip.comment.isNotBlank()) {
                        DetailRow(label = "Komentarz", value = trip.comment)
                    }
                }
            }

            trip.emergencyContact?.let { contact ->
                if (contact.name.isNotBlank() || contact.phone.isNotBlank()) {
                    Card {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Kontakt awaryjny", style = MaterialTheme.typography.titleSmall)
                            if (contact.name.isNotBlank()) Text(contact.name)
                            if (contact.phone.isNotBlank()) {
                                TextButton(onClick = {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}")))
                                }) { Text(contact.phone) }
                            }
                        }
                    }
                }
            }

            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Historia statusów", style = MaterialTheme.typography.titleSmall)
                    if (statusUpdates.isEmpty()) {
                        Text("Brak historii", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        statusUpdates.forEachIndexed { index, update ->
                            Row {
                                Text(
                                    text = update.status.label,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(text = formatDateTime(update.timestamp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (index != statusUpdates.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }

            if (canEdit) {
                Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                    Text("Edytuj wyjazd")
                }
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Usuń wyjazd")
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Usunąć wyjazd?") },
            text = { Text("Tej operacji nie można odwrócić.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteTrip(onBack)
                }) { Text("Usuń") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Anuluj") } },
        )
    }
}

@Composable
private fun DetailRow(icon: (@Composable () -> Unit)? = null, label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        icon?.invoke()
        Column {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
