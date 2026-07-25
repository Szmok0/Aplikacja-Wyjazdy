package com.rodzina.wyjazdy.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository(private val storage: FirebaseStorage) {

    suspend fun uploadTicketFile(familyId: String, tripId: String, fileUri: Uri, extension: String): String {
        val fileName = "${UUID.randomUUID()}.$extension"
        val ref = storage.reference
            .child("families/$familyId/trips/$tripId/tickets/$fileName")
        ref.putFile(fileUri).await()
        return ref.downloadUrl.await().toString()
    }
}
