package com.rodzina.wyjazdy.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rodzina.wyjazdy.R
import com.rodzina.wyjazdy.data.repository.AuthRepository
import com.rodzina.wyjazdy.data.repository.FamilyRepository
import com.rodzina.wyjazdy.data.repository.StorageRepository
import com.rodzina.wyjazdy.data.repository.TripRepository
import com.rodzina.wyjazdy.data.repository.UserRepository
import android.content.Context

/** Prosty ręczny kontener zależności - projekt jest mały, Hilt/Koin to niepotrzebny narzut. */
class AppContainer(context: Context) {

    val authRepository = AuthRepository(
        auth = FirebaseAuth.getInstance(),
        webClientId = context.getString(R.string.default_web_client_id),
    )
    val userRepository = UserRepository(FirebaseFirestore.getInstance())
    val familyRepository = FamilyRepository(FirebaseFirestore.getInstance())
    val tripRepository = TripRepository(FirebaseFirestore.getInstance())
    val storageRepository = StorageRepository(FirebaseStorage.getInstance())
}
