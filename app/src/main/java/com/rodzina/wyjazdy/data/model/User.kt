package com.rodzina.wyjazdy.data.model

data class NotificationPrefs(
    val reminderHoursBefore: List<Int> = listOf(24, 1),
    val statusUpdates: Boolean = true,
)

data class User(
    val id: String = "",
    val displayName: String = "",
    val familyId: String? = null,
    val fcmTokens: List<String> = emptyList(),
    val notificationPrefs: NotificationPrefs = NotificationPrefs(),
) {
    val initials: String
        get() = displayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifBlank { "?" }

    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "displayName" to displayName,
        "familyId" to familyId,
        "fcmTokens" to fcmTokens,
        "notificationPrefs" to mapOf(
            "reminderHoursBefore" to notificationPrefs.reminderHoursBefore,
            "statusUpdates" to notificationPrefs.statusUpdates,
        ),
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestore(id: String, data: Map<String, Any?>): User {
            val prefsMap = data["notificationPrefs"] as? Map<String, Any?>
            val hours = (prefsMap?.get("reminderHoursBefore") as? List<Number>)?.map { it.toInt() }
            return User(
                id = id,
                displayName = data["displayName"] as? String ?: "",
                familyId = data["familyId"] as? String,
                fcmTokens = (data["fcmTokens"] as? List<String>) ?: emptyList(),
                notificationPrefs = NotificationPrefs(
                    reminderHoursBefore = hours ?: listOf(24, 1),
                    statusUpdates = prefsMap?.get("statusUpdates") as? Boolean ?: true,
                ),
            )
        }
    }
}
