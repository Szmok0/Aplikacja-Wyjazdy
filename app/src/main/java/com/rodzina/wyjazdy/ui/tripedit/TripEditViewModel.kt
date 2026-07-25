package com.rodzina.wyjazdy.ui.tripedit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Timestamp
import com.rodzina.wyjazdy.data.model.EmergencyContact
import com.rodzina.wyjazdy.data.model.EventType
import com.rodzina.wyjazdy.data.model.Hotel
import com.rodzina.wyjazdy.data.model.TicketInfo
import com.rodzina.wyjazdy.data.model.Trip
import com.rodzina.wyjazdy.data.model.TransportType
import com.rodzina.wyjazdy.data.model.TripStatus
import com.rodzina.wyjazdy.data.repository.StorageRepository
import com.rodzina.wyjazdy.data.repository.TripRepository
import com.rodzina.wyjazdy.util.toTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TripEditViewModel(
    private val familyId: String,
    private val currentUserId: String,
    private val existingTrip: Trip?,
    private val tripRepository: TripRepository,
    private val storageRepository: StorageRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(
        existingTrip?.let { TripFormState.fromTrip(it) } ?: TripFormState(ownerUserId = currentUserId),
    )
    val form: StateFlow<TripFormState> = _form

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun update(transform: (TripFormState) -> TripFormState) {
        _form.value = transform(_form.value).copy(errorMessage = null)
    }

    fun setOwnerUserId(value: String) = update { it.copy(ownerUserId = value) }
    fun setCity(value: String) = update { it.copy(city = value) }
    fun setDateStart(value: LocalDateTime) = update { it.copy(dateStart = value) }
    fun setDateEnd(value: LocalDateTime) = update { it.copy(dateEnd = value) }
    fun setTransportType(value: TransportType) = update { it.copy(transportType = value) }
    fun setTicketNumber(value: String) = update { it.copy(ticketNumber = value) }
    fun setPendingTicket(uri: Uri, extension: String) =
        update { it.copy(pendingTicketUri = uri, pendingTicketExtension = extension) }
    fun clearPendingTicket() = update { it.copy(pendingTicketUri = null, pendingTicketExtension = "") }
    fun setHasHotel(value: Boolean) = update { it.copy(hasHotel = value) }
    fun setHotelName(value: String) = update { it.copy(hotelName = value) }
    fun setHotelAddress(value: String) = update { it.copy(hotelAddress = value) }
    fun setHotelCheckIn(value: LocalDateTime) = update { it.copy(hotelCheckIn = value) }
    fun setHotelCheckOut(value: LocalDateTime) = update { it.copy(hotelCheckOut = value) }
    fun setVenueAddress(value: String) = update { it.copy(venueAddress = value, venueLat = null, venueLng = null) }
    fun setVenueLatLng(lat: Double, lng: Double) = update { it.copy(venueLat = lat, venueLng = lng) }
    fun setEventType(value: EventType) = update { it.copy(eventType = value) }
    fun setEventTitle(value: String) = update { it.copy(eventTitle = value) }
    fun setComment(value: String) = update { it.copy(comment = value) }
    fun setStatus(value: TripStatus) = update { it.copy(status = value) }
    fun setHasEmergencyContact(value: Boolean) = update { it.copy(hasEmergencyContact = value) }
    fun setEmergencyContactName(value: String) = update { it.copy(emergencyContactName = value) }
    fun setEmergencyContactPhone(value: String) = update { it.copy(emergencyContactPhone = value) }

    fun save() {
        val state = _form.value
        if (state.city.isBlank()) {
            _form.value = state.copy(errorMessage = "Podaj miasto")
            return
        }
        if (state.dateEnd.isBefore(state.dateStart)) {
            _form.value = state.copy(errorMessage = "Data powrotu nie może być przed datą wyjazdu")
            return
        }

        viewModelScope.launch {
            _form.value = state.copy(isSaving = true)
            try {
                val tripId = existingTrip?.id ?: tripRepository.newTripId(familyId)

                val ticketUrl = state.pendingTicketUri?.let { uri ->
                    try {
                        storageRepository.uploadTicketFile(familyId, tripId, uri, state.pendingTicketExtension)
                    } catch (e: Exception) {
                        throw IllegalStateException(
                            "Nie udało się wgrać biletu - Firebase Storage może nie być włączony w tym " +
                                "projekcie (wymaga planu Blaze). Usuń załączony plik (numer biletu możesz " +
                                "zostawić) i zapisz ponownie.",
                        )
                    }
                } ?: state.existingTicketUrl

                val ticketInfo = if (state.ticketNumber.isNotBlank() || !ticketUrl.isNullOrBlank()) {
                    TicketInfo(number = state.ticketNumber, fileUrl = ticketUrl ?: "")
                } else null

                val hotel = if (state.hasHotel) {
                    Hotel(
                        name = state.hotelName,
                        address = state.hotelAddress,
                        checkIn = state.hotelCheckIn.toTimestamp(),
                        checkOut = state.hotelCheckOut.toTimestamp(),
                    )
                } else null

                val emergencyContact = if (state.hasEmergencyContact) {
                    EmergencyContact(name = state.emergencyContactName, phone = state.emergencyContactPhone)
                } else null

                val trip = Trip(
                    id = tripId,
                    ownerUserId = state.ownerUserId,
                    city = state.city.trim(),
                    dateStart = state.dateStart.toTimestamp(),
                    dateEnd = state.dateEnd.toTimestamp(),
                    transportType = state.transportType,
                    ticketInfo = ticketInfo,
                    hotel = hotel,
                    venueAddress = state.venueAddress.trim(),
                    venueLat = state.venueLat,
                    venueLng = state.venueLng,
                    eventType = state.eventType,
                    eventTitle = state.eventTitle.trim(),
                    comment = state.comment.trim(),
                    status = state.status,
                    emergencyContact = emergencyContact,
                    createdAt = existingTrip?.createdAt ?: Timestamp.now(),
                )

                if (existingTrip == null) {
                    tripRepository.createTrip(familyId, tripId, trip)
                } else {
                    tripRepository.updateTrip(familyId, tripId, trip, existingTrip.status)
                }
                _saved.value = true
            } catch (e: Exception) {
                _form.value = _form.value.copy(isSaving = false, errorMessage = e.message ?: "Nie udało się zapisać wyjazdu")
            }
        }
    }

    companion object {
        fun factory(
            familyId: String,
            currentUserId: String,
            existingTrip: Trip?,
            tripRepository: TripRepository,
            storageRepository: StorageRepository,
        ) = viewModelFactory {
            initializer {
                TripEditViewModel(familyId, currentUserId, existingTrip, tripRepository, storageRepository)
            }
        }
    }
}
