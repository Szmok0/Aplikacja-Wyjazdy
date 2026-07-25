package com.rodzina.wyjazdy.data.model

import com.google.firebase.Timestamp

data class TicketInfo(
    val number: String = "",
    val fileUrl: String = "",
)

data class Hotel(
    val name: String = "",
    val address: String = "",
    val checkIn: Timestamp? = null,
    val checkOut: Timestamp? = null,
)

data class EmergencyContact(
    val name: String = "",
    val phone: String = "",
)

data class StatusUpdate(
    val id: String = "",
    val status: TripStatus = TripStatus.PLANOWANY,
    val timestamp: Timestamp = Timestamp.now(),
    val note: String? = null,
)

data class Trip(
    val id: String = "",
    val ownerUserId: String = "",
    val city: String = "",
    val dateStart: Timestamp = Timestamp.now(),
    val dateEnd: Timestamp = Timestamp.now(),
    val transportType: TransportType = TransportType.CAR,
    val ticketInfo: TicketInfo? = null,
    val hotel: Hotel? = null,
    val venueAddress: String = "",
    val venueLat: Double? = null,
    val venueLng: Double? = null,
    val eventType: EventType = EventType.INNE,
    val eventTitle: String = "",
    val routePolyline: String? = null,
    val comment: String = "",
    val status: TripStatus = TripStatus.PLANOWANY,
    val emergencyContact: EmergencyContact? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "ownerUserId" to ownerUserId,
        "city" to city,
        "dateStart" to dateStart,
        "dateEnd" to dateEnd,
        "transportType" to transportType.value,
        "ticketInfo" to ticketInfo?.let { mapOf("number" to it.number, "fileUrl" to it.fileUrl) },
        "hotel" to hotel?.let {
            mapOf(
                "name" to it.name,
                "address" to it.address,
                "checkIn" to it.checkIn,
                "checkOut" to it.checkOut,
            )
        },
        "venueAddress" to venueAddress,
        "venueLat" to venueLat,
        "venueLng" to venueLng,
        "eventType" to eventType.value,
        "eventTitle" to eventTitle,
        "routePolyline" to routePolyline,
        "comment" to comment,
        "status" to status.value,
        "emergencyContact" to emergencyContact?.let { mapOf("name" to it.name, "phone" to it.phone) },
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestore(id: String, data: Map<String, Any?>): Trip {
            val ticketMap = data["ticketInfo"] as? Map<String, Any?>
            val hotelMap = data["hotel"] as? Map<String, Any?>
            val emergencyMap = data["emergencyContact"] as? Map<String, Any?>
            return Trip(
                id = id,
                ownerUserId = data["ownerUserId"] as? String ?: "",
                city = data["city"] as? String ?: "",
                dateStart = data["dateStart"] as? Timestamp ?: Timestamp.now(),
                dateEnd = data["dateEnd"] as? Timestamp ?: Timestamp.now(),
                transportType = TransportType.fromValue(data["transportType"] as? String),
                ticketInfo = ticketMap?.let {
                    TicketInfo(
                        number = it["number"] as? String ?: "",
                        fileUrl = it["fileUrl"] as? String ?: "",
                    )
                },
                hotel = hotelMap?.let {
                    Hotel(
                        name = it["name"] as? String ?: "",
                        address = it["address"] as? String ?: "",
                        checkIn = it["checkIn"] as? Timestamp,
                        checkOut = it["checkOut"] as? Timestamp,
                    )
                },
                venueAddress = data["venueAddress"] as? String ?: "",
                venueLat = (data["venueLat"] as? Number)?.toDouble(),
                venueLng = (data["venueLng"] as? Number)?.toDouble(),
                eventType = EventType.fromValue(data["eventType"] as? String),
                eventTitle = data["eventTitle"] as? String ?: "",
                routePolyline = data["routePolyline"] as? String,
                comment = data["comment"] as? String ?: "",
                status = TripStatus.fromValue(data["status"] as? String),
                emergencyContact = emergencyMap?.let {
                    EmergencyContact(
                        name = it["name"] as? String ?: "",
                        phone = it["phone"] as? String ?: "",
                    )
                },
                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now(),
            )
        }
    }
}
