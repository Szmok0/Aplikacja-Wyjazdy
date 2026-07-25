package com.rodzina.wyjazdy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodzina.wyjazdy.data.model.Family
import com.rodzina.wyjazdy.data.model.Trip
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.data.repository.FamilyRepository
import com.rodzina.wyjazdy.data.repository.TripRepository
import com.rodzina.wyjazdy.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

sealed interface SessionState {
    data object Loading : SessionState
    data object NeedsFamily : SessionState
    data class Ready(
        val currentUser: User,
        val family: Family,
        val members: List<User>,
        val trips: List<Trip>,
    ) : SessionState
}

/**
 * Współdzielony stan sesji (przypięty do MainActivity) - jedno źródło prawdy dla rodziny,
 * członków i wyjazdów, żeby każdy ekran nie otwierał własnych, zduplikowanych listenerów Firestore.
 */
class SessionViewModel(
    private val userId: String,
    private val userRepository: UserRepository,
    private val familyRepository: FamilyRepository,
    private val tripRepository: TripRepository,
) : ViewModel() {

    val state: StateFlow<SessionState> = userRepository.observeUser(userId)
        .flatMapLatest { user ->
            val familyId = user?.familyId
            if (user == null || familyId == null) {
                flowOf(SessionState.NeedsFamily)
            } else {
                familyRepository.observeFamily(familyId).flatMapLatest { family ->
                    if (family == null || userId !in family.memberUserIds) {
                        flow {
                            userRepository.setFamilyId(userId, null)
                            emit(SessionState.NeedsFamily)
                        }
                    } else {
                        combine(
                            userRepository.observeUsers(family.memberUserIds),
                            tripRepository.observeTrips(family.id),
                        ) { members, trips ->
                            SessionState.Ready(user, family, members, trips)
                        }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState.Loading)

    companion object {
        fun factory(
            userId: String,
            userRepository: UserRepository,
            familyRepository: FamilyRepository,
            tripRepository: TripRepository,
        ) = viewModelFactory {
            initializer { SessionViewModel(userId, userRepository, familyRepository, tripRepository) }
        }
    }
}
