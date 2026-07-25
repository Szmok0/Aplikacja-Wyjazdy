package com.rodzina.wyjazdy.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseUser
import com.rodzina.wyjazdy.di.AppContainer
import com.rodzina.wyjazdy.notifications.ReminderScheduler
import com.rodzina.wyjazdy.ui.login.LoginScreen
import com.rodzina.wyjazdy.ui.login.OnboardingScreen
import com.rodzina.wyjazdy.ui.navigation.MainNavHost

@Composable
fun WyjazdyApp(container: AppContainer) {
    val authUser by container.authRepository.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = container.authRepository.currentUser())

    val user: FirebaseUser? = authUser
    if (user == null) {
        LoginScreen(container = container)
        return
    }

    val sessionViewModel: SessionViewModel = viewModel(
        key = user.uid,
        factory = SessionViewModel.factory(
            userId = user.uid,
            userRepository = container.userRepository,
            familyRepository = container.familyRepository,
            tripRepository = container.tripRepository,
        ),
    )
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    when (val state = sessionState) {
        is SessionState.Loading -> FullScreenLoading()
        is SessionState.NeedsFamily -> OnboardingScreen(
            container = container,
            userId = user.uid,
            displayName = user.displayName ?: "",
        )
        is SessionState.Ready -> {
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            LaunchedEffect(state.trips, state.currentUser.notificationPrefs) {
                val ownTrips = state.trips.filter { it.ownerUserId == state.currentUser.id }
                ReminderScheduler.rescheduleAll(context, ownTrips, state.currentUser.notificationPrefs.reminderHoursBefore)
            }
            MainNavHost(
                container = container,
                sessionViewModel = sessionViewModel,
                ready = state,
                onSignOut = { container.authRepository.signOut(context) },
            )
        }
    }
}

@Composable
fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
