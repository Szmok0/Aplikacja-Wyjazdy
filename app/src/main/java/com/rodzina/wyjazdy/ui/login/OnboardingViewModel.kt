package com.rodzina.wyjazdy.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodzina.wyjazdy.data.repository.FamilyRepository
import com.rodzina.wyjazdy.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface OnboardingUiState {
    data object Idle : OnboardingUiState
    data object Loading : OnboardingUiState
    data class Error(val message: String) : OnboardingUiState
}

class OnboardingViewModel(
    private val userId: String,
    private val familyRepository: FamilyRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val state: StateFlow<OnboardingUiState> = _state

    fun createFamily(name: String) {
        if (name.isBlank()) {
            _state.value = OnboardingUiState.Error("Podaj nazwę rodziny")
            return
        }
        viewModelScope.launch {
            _state.value = OnboardingUiState.Loading
            try {
                val family = familyRepository.createFamily(userId, name.trim())
                userRepository.setFamilyId(userId, family.id)
                _state.value = OnboardingUiState.Idle
            } catch (e: Exception) {
                _state.value = OnboardingUiState.Error(e.message ?: "Nie udało się utworzyć rodziny")
            }
        }
    }

    fun joinFamily(code: String) {
        if (code.isBlank()) {
            _state.value = OnboardingUiState.Error("Podaj kod zaproszenia")
            return
        }
        viewModelScope.launch {
            _state.value = OnboardingUiState.Loading
            try {
                val familyId = familyRepository.resolveInviteCode(code.trim())
                    ?: throw IllegalArgumentException("Nieprawidłowy kod zaproszenia")
                familyRepository.joinFamily(familyId, userId)
                userRepository.setFamilyId(userId, familyId)
                _state.value = OnboardingUiState.Idle
            } catch (e: Exception) {
                _state.value = OnboardingUiState.Error(e.message ?: "Nie udało się dołączyć do rodziny")
            }
        }
    }

    companion object {
        fun factory(userId: String, familyRepository: FamilyRepository, userRepository: UserRepository) =
            viewModelFactory {
                initializer { OnboardingViewModel(userId, familyRepository, userRepository) }
            }
    }
}
