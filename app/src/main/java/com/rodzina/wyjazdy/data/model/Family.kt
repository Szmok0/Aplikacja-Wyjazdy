package com.rodzina.wyjazdy.data.model

import com.google.firebase.Timestamp

data class Family(
    val id: String = "",
    val name: String = "",
    val adminUserId: String = "",
    val memberUserIds: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    // Rozszerzenie ponad schemat z sekcji 3 - potrzebne, żeby admin mógł pokazać/zregenerować
    // aktualny kod zaproszenia bez dodatkowego zapytania do kolekcji inviteCodes.
    val activeInviteCode: String? = null,
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestore(id: String, data: Map<String, Any?>): Family = Family(
            id = id,
            name = data["name"] as? String ?: "",
            adminUserId = data["adminUserId"] as? String ?: "",
            memberUserIds = (data["memberUserIds"] as? List<String>) ?: emptyList(),
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
            activeInviteCode = data["activeInviteCode"] as? String,
        )
    }
}
