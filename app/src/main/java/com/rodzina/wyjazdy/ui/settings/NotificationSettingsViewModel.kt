package com.rodzina.wyjazdy.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodzina.wyjazdy.data.model.User
import com.rodzina.wyjazdy.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    val reminderHoursBefore: Set<Int> = setOf(24, 1),
    val statusUpdates: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
)

class NotificationSettingsViewModel(
    private val userId: String,
    initialUser: User,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        NotificationSettingsUiState(
            reminderHoursBefore = initialUser.notificationPrefs.reminderHoursBefore.toSet(),
            statusUpdates = initialUser.notificationPrefs.statusUpdates,
        ),
    )
    val state: StateFlow<NotificationSettingsUiState> = _state

    fun toggleHour(hour: Int) {
        val current = _state.value.reminderHoursBefore
        _state.value = _state.value.copy(
            reminderHoursBefore = if (hour in current) current - hour else current + hour,
            saved = false,
        )
    }

    fun setStatusUpdates(enabled: Boolean) {
        _state.value = _state.value.copy(statusUpdates = enabled, saved = false)
    }

    fun save() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val s = _state.value
            userRepository.updateNotificationPrefs(
                userId,
                s.reminderHoursBefore.sortedDescending(),
                s.statusUpdates,
            )
            _state.value = _state.value.copy(isSaving = false, saved = true)
        }
    }

    companion object {
        fun factory(userId: String, initialUser: User, userRepository: UserRepository) = viewModelFactory {
            initializer { NotificationSettingsViewModel(userId, initialUser, userRepository) }
        }
    }
}
