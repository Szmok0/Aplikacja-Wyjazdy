package com.rodzina.wyjazdy.ui.tripedit

import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.di.AppContainer
import com.rodzina.wyjazdy.ui.common.ChipSelectorRow
import com.rodzina.wyjazdy.ui.common.DateTimePickerField
import com.rodzina.wyjazdy.ui.common.MiniMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.location.Geocoder
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripEditScreen(
    container: AppContainer,
    familyId: String,
    currentUserId: String,
    members: List<User>,
    isAdmin: Boolean,
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
    var showMoreInfo by remember { mutableStateOf(false) }
    var isGeocoding by remember { mutableStateOf(false) }
    var geocodeError by remember { mutableStateOf<String?>(null) }

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
            if (isAdmin && members.size > 1) {
                FormSection(title = "Kto jedzie?") {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        members.forEach { member ->
                            FilterChip(
                                selected = form.ownerUserId == member.id,
                                onClick = { viewModel.setOwnerUserId(member.id) },
                                label = { Text(if (member.id == currentUserId) "Ja" else member.displayName.ifBlank { "?" }) },
                            )
                        }
                    }
                }
            }

            FormSection(title = "Gdzie / Kiedy / Czas") {
                OutlinedTextField(
                    value = form.city,
                    onValueChange = viewModel::setCity,
                    label = { Text("Miasto") },
                    modifier = Modifier.fillMaxWidth(),
                )
                DateTimePickerField("Wyjazd", form.dateStart, viewModel::setDateStart)
                DateTimePickerField("Powrót", form.dateEnd, viewModel::setDateEnd)
            }

            FormSection(title = "Transport") {
                ChipSelectorRow(
                    options = TransportType.entries,
                    selected = form.transportType,
                    label = { it.label },
                    onSelect = viewModel::setTransportType,
                )
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
                    onValueChange = { viewModel.setVenueAddress(it); geocodeError = null },
                    label = { Text("Adres") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Button(
                        enabled = form.venueAddress.isNotBlank() && !isGeocoding,
                        onClick = {
                            val address = form.venueAddress
                            geocodeError = null
                            isGeocoding = true
                            scope.launch(Dispatchers.IO) {
                                val result = runCatching {
                                    if (!Geocoder.isPresent()) {
                                        error("Geokodowanie nie jest dostępne na tym urządzeniu")
                                    }
                                    @Suppress("DEPRECATION")
                                    Geocoder(context, Locale("pl")).getFromLocationName(address, 1)
                                }
                                withContext(Dispatchers.Main) {
                                    isGeocoding = false
                                    val location = result.getOrNull()?.firstOrNull()
                                    if (location != null) {
                                        viewModel.setVenueLatLng(location.latitude, location.longitude)
                                    } else {
                                        geocodeError = result.exceptionOrNull()?.message
                                            ?: "Nie znaleziono tego adresu. Sprawdź pisownię albo spróbuj dokładniejszego adresu (np. z numerem domu i miastem)."
                                    }
                                }
                            }
                        },
                    ) {
                        Text("Znajdź na mapie")
                    }
                    if (isGeocoding) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(start = 12.dp).size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
                if (geocodeError != null) {
                    Text(text = geocodeError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            FormSection(title = "Rodzaj wydarzenia") {
                ChipSelectorRow(
                    options = EventType.entries,
                    selected = form.eventType,
                    label = { it.label },
                    onSelect = viewModel::setEventType,
                )
                OutlinedTextField(
                    value = form.eventTitle,
                    onValueChange = viewModel::setEventTitle,
                    label = { Text("Nazwa wydarzenia") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            FormSection(title = "Trasa przejazdu / Mapa") {
                val lat = form.venueLat
                val lng = form.venueLng
                if (lat != null && lng != null) {
                    MiniMap(lat = lat, lng = lng, title = form.venueAddress)
                } else {
                    Text(
                        "Znajdź adres miejsca szkolenia powyżej, żeby zobaczyć podgląd na mapie",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            FormSection(title = "Bilet") {
                OutlinedTextField(
                    value = form.ticketNumber,
                    onValueChange = viewModel::setTicketNumber,
                    label = { Text("Numer biletu (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Button(onClick = { ticketPickerLauncher.launch(arrayOf("image/*", "application/pdf")) }) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null)
                        Text(
                            text = when {
                                form.pendingTicketUri != null -> "Nowy plik wybrany"
                                !form.existingTicketUrl.isNullOrBlank() -> "Zmień załączony bilet"
                                else -> "Dodaj bilet (PDF, JPG, PNG)"
                            },
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    if (form.pendingTicketUri != null) {
                        IconButton(onClick = viewModel::clearPendingTicket) {
                            Icon(Icons.Filled.Close, contentDescription = "Usuń wybrany plik")
                        }
                    }
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

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = if (showMoreInfo) 8.dp else 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text("Dodatkowe informacje", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showMoreInfo = !showMoreInfo }) {
                            Icon(if (showMoreInfo) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
                        }
                    }
                    if (showMoreInfo) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Status", style = MaterialTheme.typography.titleSmall)
                            ChipSelectorRow(
                                options = TripStatus.entries,
                                selected = form.status,
                                label = { it.label },
                                onSelect = viewModel::setStatus,
                            )
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                Switch(checked = form.hasEmergencyContact, onCheckedChange = viewModel::setHasEmergencyContact)
                                Text("Kontakt awaryjny", modifier = Modifier.padding(start = 8.dp))
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
                    }
                }
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

            // Stały margines na dole, żeby przycisk dało się swobodnie doscrollować ponad
            // klawiaturę (imePadding() na niektórych urządzeniach/ROM-ach rezerwuje miejsce
            // na klawiaturę nawet gdy jest zamknięta, więc lepiej nie polegać na insetach).
            Spacer(modifier = Modifier.height(96.dp))
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
