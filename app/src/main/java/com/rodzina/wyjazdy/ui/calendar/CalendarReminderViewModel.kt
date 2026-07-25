package com.rodzina.wyjazdy.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodzina.wyjazdy.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CalendarReminderViewModel(
    private val userId: String,
    initialHours: List<Int>,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _reminderHoursBefore = MutableStateFlow(initialHours.toSet())
    val reminderHoursBefore: StateFlow<Set<Int>> = _reminderHoursBefore

    fun toggleHour(hour: Int) {
        val current = _reminderHoursBefore.value
        val updated = if (hour in current) current - hour else current + hour
        _reminderHoursBefore.value = updated
        viewModelScope.launch { userRepository.updateReminderHours(userId, updated.sortedDescending()) }
    }

    companion object {
        fun factory(userId: String, initialHours: List<Int>, userRepository: UserRepository) = viewModelFactory {
            initializer { CalendarReminderViewModel(userId, initialHours, userRepository) }
        }
    }
}
