package com.rodzina.wyjazdy.data.repository

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.rodzina.wyjazdy.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(private val firestore: FirebaseFirestore) {

    private fun usersCollection() = firestore.collection("users")

    suspend fun ensureUserDocument(userId: String, displayName: String) {
        val ref = usersCollection().document(userId)
        val snapshot = ref.get().await()
        if (!snapshot.exists()) {
            ref.set(User(id = userId, displayName = displayName).toFirestoreMap()).await()
        }
    }

    fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val registration = usersCollection().document(userId).addSnapshotListener { snapshot, _ ->
            val data = snapshot?.data
            trySend(if (data == null) null else User.fromFirestore(snapshot.id, data))
        }
        awaitClose { registration.remove() }
    }

    fun observeUsers(userIds: List<String>): Flow<List<User>> = callbackFlow {
        if (userIds.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = usersCollection()
            .whereIn(FieldPath.documentId(), userIds.take(30))
            .addSnapshotListener { snapshot, _ ->
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { User.fromFirestore(doc.id, it) }
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { registration.remove() }
    }

    suspend fun setFamilyId(userId: String, familyId: String?) {
        usersCollection().document(userId).update("familyId", familyId).await()
    }

    suspend fun updateReminderHours(userId: String, reminderHoursBefore: List<Int>) {
        usersCollection().document(userId)
            .update("notificationPrefs.reminderHoursBefore", reminderHoursBefore)
            .await()
    }

    suspend fun updateStatusUpdatesEnabled(userId: String, enabled: Boolean) {
        usersCollection().document(userId)
            .update("notificationPrefs.statusUpdates", enabled)
            .await()
    }
}
