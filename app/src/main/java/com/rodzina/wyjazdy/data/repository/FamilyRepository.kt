package com.rodzina.wyjazdy.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rodzina.wyjazdy.data.model.Family
import com.rodzina.wyjazdy.util.InviteCodeGenerator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FamilyRepository(private val firestore: FirebaseFirestore) {

    private fun familiesCollection() = firestore.collection("families")
    private fun inviteCodesCollection() = firestore.collection("inviteCodes")

    suspend fun createFamily(adminUserId: String, name: String): Family {
        val ref = familiesCollection().document()
        val family = Family(
            id = ref.id,
            name = name,
            adminUserId = adminUserId,
            memberUserIds = listOf(adminUserId),
        )
        ref.set(
            mapOf(
                "name" to family.name,
                "adminUserId" to family.adminUserId,
                "memberUserIds" to family.memberUserIds,
                "createdAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
        val code = regenerateInviteCode(ref.id)
        return family.copy(activeInviteCode = code)
    }

    fun observeFamily(familyId: String): Flow<Family?> = callbackFlow {
        val registration = familiesCollection().document(familyId).addSnapshotListener { snapshot, _ ->
            val data = snapshot?.data
            trySend(if (data == null) null else Family.fromFirestore(snapshot.id, data))
        }
        awaitClose { registration.remove() }
    }

    /** Zwraca familyId dla podanego kodu, albo null jeśli kod jest nieprawidłowy/wygasły. */
    suspend fun resolveInviteCode(code: String): String? {
        val snapshot = inviteCodesCollection().document(code.uppercase()).get().await()
        return snapshot.getString("familyId")
    }

    suspend fun joinFamily(familyId: String, userId: String) {
        familiesCollection().document(familyId)
            .update("memberUserIds", FieldValue.arrayUnion(userId))
            .await()
    }

    suspend fun regenerateInviteCode(familyId: String): String {
        val previousCode = familiesCollection().document(familyId).get().await()
            .getString("activeInviteCode")
        val newCode = InviteCodeGenerator.generate()
        inviteCodesCollection().document(newCode).set(
            mapOf("familyId" to familyId, "createdAt" to FieldValue.serverTimestamp()),
        ).await()
        familiesCollection().document(familyId).update("activeInviteCode", newCode).await()
        if (!previousCode.isNullOrBlank()) {
            inviteCodesCollection().document(previousCode).delete().await()
        }
        return newCode
    }

    suspend fun transferAdmin(familyId: String, newAdminUserId: String) {
        familiesCollection().document(familyId).update("adminUserId", newAdminUserId).await()
    }

    suspend fun removeMember(familyId: String, userId: String) {
        familiesCollection().document(familyId)
            .update("memberUserIds", FieldValue.arrayRemove(userId))
            .await()
    }
}
