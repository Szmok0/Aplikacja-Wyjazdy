package com.rodzina.wyjazdy.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rodzina.wyjazdy.data.model.StatusUpdate
import com.rodzina.wyjazdy.data.model.Trip
import com.rodzina.wyjazdy.data.model.TripStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TripRepository(private val firestore: FirebaseFirestore) {

    private fun tripsCollection(familyId: String) =
        firestore.collection("families").document(familyId).collection("trips")

    fun observeTrips(familyId: String): Flow<List<Trip>> = callbackFlow {
        val registration = tripsCollection(familyId)
            .orderBy("dateStart", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val trips = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Trip.fromFirestore(doc.id, it) }
                } ?: emptyList()
                trySend(trips)
            }
        awaitClose { registration.remove() }
    }

    fun observeTrip(familyId: String, tripId: String): Flow<Trip?> = callbackFlow {
        val registration = tripsCollection(familyId).document(tripId).addSnapshotListener { snapshot, _ ->
            val data = snapshot?.data
            trySend(if (data == null) null else Trip.fromFirestore(snapshot.id, data))
        }
        awaitClose { registration.remove() }
    }

    /** Generuje lokalnie nowe id (bez zapisu) - potrzebne, żeby wgrać bilet do Storage pod ścieżkę
     * z tripId, zanim dokument wyjazdu zostanie utworzony. */
    fun newTripId(familyId: String): String = tripsCollection(familyId).document().id

    suspend fun createTrip(familyId: String, tripId: String, trip: Trip) {
        val data = trip.toFirestoreMap().toMutableMap()
        data["createdAt"] = FieldValue.serverTimestamp()
        data["updatedAt"] = FieldValue.serverTimestamp()
        tripsCollection(familyId).document(tripId).set(data).await()
        addStatusUpdate(familyId, tripId, trip.status, note = null)
    }

    suspend fun updateTrip(familyId: String, tripId: String, trip: Trip, previousStatus: TripStatus?) {
        val data = trip.toFirestoreMap().toMutableMap()
        data["updatedAt"] = FieldValue.serverTimestamp()
        tripsCollection(familyId).document(tripId).set(data).await()
        if (previousStatus != null && previousStatus != trip.status) {
            addStatusUpdate(familyId, tripId, trip.status, note = null)
        }
    }

    suspend fun deleteTrip(familyId: String, tripId: String) {
        tripsCollection(familyId).document(tripId).delete().await()
    }

    fun observeStatusUpdates(familyId: String, tripId: String): Flow<List<StatusUpdate>> = callbackFlow {
        val registration = tripsCollection(familyId).document(tripId).collection("statusUpdates")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val updates = snapshot?.documents?.mapNotNull { doc ->
                    val status = TripStatus.fromValue(doc.getString("status"))
                    StatusUpdate(
                        id = doc.id,
                        status = status,
                        timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                        note = doc.getString("note"),
                    )
                } ?: emptyList()
                trySend(updates)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addStatusUpdate(familyId: String, tripId: String, status: TripStatus, note: String?) {
        tripsCollection(familyId).document(tripId).collection("statusUpdates").document().set(
            mapOf(
                "status" to status.value,
                "timestamp" to FieldValue.serverTimestamp(),
                "note" to note,
            ),
        ).await()
    }
}
