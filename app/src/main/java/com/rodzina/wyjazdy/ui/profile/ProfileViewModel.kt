package com.rodzina.wyjazdy.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodzina.wyjazdy.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userId: String,
    private val userRepository: UserRepository,
) : ViewModel() {

    fun setStatusUpdatesEnabled(enabled: Boolean) {
        viewModelScope.launch { userRepository.updateStatusUpdatesEnabled(userId, enabled) }
    }

    companion object {
        fun factory(userId: String, userRepository: UserRepository) = viewModelFactory {
            initializer { ProfileViewModel(userId, userRepository) }
        }
    }
}
