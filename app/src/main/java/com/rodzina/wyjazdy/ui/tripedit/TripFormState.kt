package com.rodzina.wyjazdy.ui.tripedit

import android.net.Uri
import com.rodzina.wyjazdy.data.model.EventType
import com.rodzina.wyjazdy.data.model.Trip
import com.rodzina.wyjazdy.data.model.TransportType
import com.rodzina.wyjazdy.data.model.TripStatus
import com.rodzina.wyjazdy.util.toLocalDateTime
import java.time.LocalDateTime

data class TripFormState(
    val ownerUserId: String = "",
    val city: String = "",
    val dateStart: LocalDateTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0),
    val dateEnd: LocalDateTime = LocalDateTime.now().plusDays(2).withHour(18).withMinute(0),
    val transportType: TransportType = TransportType.CAR,
    val ticketNumber: String = "",
    val existingTicketUrl: String? = null,
    val pendingTicketUri: Uri? = null,
    val pendingTicketExtension: String = "",
    val hasHotel: Boolean = false,
    val hotelName: String = "",
    val hotelAddress: String = "",
    val hotelCheckIn: LocalDateTime = dateStart,
    val hotelCheckOut: LocalDateTime = dateEnd,
    val venueAddress: String = "",
    val venueLat: Double? = null,
    val venueLng: Double? = null,
    val eventType: EventType = EventType.SZKOLENIE,
    val eventTitle: String = "",
    val comment: String = "",
    val status: TripStatus = TripStatus.PLANOWANY,
    val hasEmergencyContact: Boolean = false,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        fun fromTrip(trip: Trip): TripFormState {
            val start = trip.dateStart.toLocalDateTime()
            val end = trip.dateEnd.toLocalDateTime()
            return TripFormState(
                ownerUserId = trip.ownerUserId,
                city = trip.city,
                dateStart = start,
                dateEnd = end,
                transportType = trip.transportType,
                ticketNumber = trip.ticketInfo?.number ?: "",
                existingTicketUrl = trip.ticketInfo?.fileUrl,
                hasHotel = trip.hotel != null,
                hotelName = trip.hotel?.name ?: "",
                hotelAddress = trip.hotel?.address ?: "",
                hotelCheckIn = trip.hotel?.checkIn?.toLocalDateTime() ?: start,
                hotelCheckOut = trip.hotel?.checkOut?.toLocalDateTime() ?: end,
                venueAddress = trip.venueAddress,
                venueLat = trip.venueLat,
                venueLng = trip.venueLng,
                eventType = trip.eventType,
                eventTitle = trip.eventTitle,
                comment = trip.comment,
                status = trip.status,
                hasEmergencyContact = trip.emergencyContact != null,
                emergencyContactName = trip.emergencyContact?.name ?: "",
                emergencyContactPhone = trip.emergencyContact?.phone ?: "",
            )
        }
    }
}
