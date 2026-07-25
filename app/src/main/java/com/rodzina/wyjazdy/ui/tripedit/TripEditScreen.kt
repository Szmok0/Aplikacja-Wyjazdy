package com.rodzina.wyjazdy.ui.tripedit

import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodzina.wyjazdy.data.model.EventType
import com.rodzina.wyjazdy.data.model.Trip
import com.rodzina.wyjazdy.data.model.TransportType
import com.rodzina.wyjazdy.data.model.TripStatus
import com.rodzina.wyjazdy.di.AppContainer
import com.rodzina.wyjazdy.ui.common.DateTimePickerField
import com.rodzina.wyjazdy.ui.common.MiniMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.location.Geocoder
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripEditScreen(
    container: AppContainer,
    familyId: String,
    currentUserId: String,
    existingTrip: Trip?,
    onDone: () -> Unit,
) {
    val viewModel: TripEditViewModel = viewModel(
        key = existingTrip?.id ?: "new",
        factory = TripEditViewModel.factory(
            familyId, currentUserId, existingTrip,
            container.tripRepository, container.storageRepository,
        ),
    )
    val form by viewModel.form.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(saved) { if (saved) onDone() }

    val ticketPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "dat"
            viewModel.setPendingTicket(uri, extension)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingTrip == null) "Nowy wyjazd" else "Edytuj wyjazd") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Wstecz")
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
            FormSection(title = "Podstawowe") {
                OutlinedTextField(
                    value = form.city,
                    onValueChange = viewModel::setCity,
                    label = { Text("Miasto") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = form.eventTitle,
                    onValueChange = viewModel::setEventTitle,
                    label = { Text("Nazwa wydarzenia") },
                    modifier = Modifier.fillMaxWidth(),
                )
                EnumDropdown(
                    label = "Typ wydarzenia",
                    options = EventType.entries,
                    selected = form.eventType,
                    optionLabel = { it.label },
                    onSelected = viewModel::setEventType,
                )
                DateTimePickerField("Wyjazd", form.dateStart, viewModel::setDateStart)
                DateTimePickerField("Powrót", form.dateEnd, viewModel::setDateEnd)
                EnumDropdown(
                    label = "Status",
                    options = TripStatus.entries,
                    selected = form.status,
                    optionLabel = { it.label },
                    onSelected = viewModel::setStatus,
                )
            }

            FormSection(title = "Transport") {
                EnumDropdown(
                    label = "Środek transportu",
                    options = TransportType.entries,
                    selected = form.transportType,
                    optionLabel = { it.label },
                    onSelected = viewModel::setTransportType,
                )
                OutlinedTextField(
                    value = form.ticketNumber,
                    onValueChange = viewModel::setTicketNumber,
                    label = { Text("Numer biletu (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = { ticketPickerLauncher.launch(arrayOf("image/*", "application/pdf")) }) {
                    Icon(Icons.Filled.AttachFile, contentDescription = null)
                    Text(
                        text = when {
                            form.pendingTicketUri != null -> "Nowy plik wybrany"
                            !form.existingTicketUrl.isNullOrBlank() -> "Zmień załączony bilet"
                            else -> "Dodaj zdjęcie/PDF biletu"
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            FormSection(title = "Nocleg") {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Switch(checked = form.hasHotel, onCheckedChange = viewModel::setHasHotel)
                    Text("Nocleg w hotelu", modifier = Modifier.padding(start = 8.dp))
                }
                if (form.hasHotel) {
                    OutlinedTextField(
                        value = form.hotelName,
                        onValueChange = viewModel::setHotelName,
                        label = { Text("Nazwa hotelu") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = form.hotelAddress,
                        onValueChange = viewModel::setHotelAddress,
                        label = { Text("Adres hotelu") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DateTimePickerField("Zameldowanie", form.hotelCheckIn, viewModel::setHotelCheckIn)
                    DateTimePickerField("Wymeldowanie", form.hotelCheckOut, viewModel::setHotelCheckOut)
                }
            }

            FormSection(title = "Miejsce szkolenia") {
                OutlinedTextField(
                    value = form.venueAddress,
                    onValueChange = viewModel::setVenueAddress,
                    label = { Text("Adres") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    enabled = form.venueAddress.isNotBlank(),
                    onClick = {
                        val address = form.venueAddress
                        scope.launch(Dispatchers.IO) {
                            @Suppress("DEPRECATION")
                            val results = runCatching {
                                Geocoder(context, Locale("pl")).getFromLocationName(address, 1)
                            }.getOrNull()
                            val location = results?.firstOrNull()
                            if (location != null) {
                                viewModel.setVenueLatLng(location.latitude, location.longitude)
                            }
                        }
                    },
                ) {
                    Text("Znajdź na mapie")
                }
                val lat = form.venueLat
                val lng = form.venueLng
                if (lat != null && lng != null) {
                    MiniMap(lat = lat, lng = lng, title = form.venueAddress)
                }
            }

            FormSection(title = "Kontakt awaryjny") {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Switch(checked = form.hasEmergencyContact, onCheckedChange = viewModel::setHasEmergencyContact)
                    Text("Podaj kontakt awaryjny", modifier = Modifier.padding(start = 8.dp))
                }
                if (form.hasEmergencyContact) {
                    OutlinedTextField(
                        value = form.emergencyContactName,
                        onValueChange = viewModel::setEmergencyContactName,
                        label = { Text("Imię i nazwisko") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = form.emergencyContactPhone,
                        onValueChange = viewModel::setEmergencyContactPhone,
                        label = { Text("Telefon") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            FormSection(title = "Komentarz") {
                OutlinedTextField(
                    value = form.comment,
                    onValueChange = viewModel::setComment,
                    label = { Text("Komentarz (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }

            if (form.errorMessage != null) {
                Text(text = form.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = viewModel::save,
                enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text("Zapisz wyjazd")
            }
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
