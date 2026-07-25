package com.rodzina.wyjazdy.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodzina.wyjazdy.data.repository.AuthRepository
import com.rodzina.wyjazdy.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state

    fun onGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            try {
                val user = authRepository.signInWithGoogleIdToken(idToken)
                userRepository.ensureUserDocument(user.uid, user.displayName ?: "")
                _state.value = LoginUiState.Idle
            } catch (e: Exception) {
                _state.value = LoginUiState.Error(e.message ?: "Logowanie nie powiodło się")
            }
        }
    }

    fun onError(message: String) {
        _state.value = LoginUiState.Error(message)
    }

    companion object {
        fun factory(authRepository: AuthRepository, userRepository: UserRepository) = viewModelFactory {
            initializer { LoginViewModel(authRepository, userRepository) }
        }
    }
}
