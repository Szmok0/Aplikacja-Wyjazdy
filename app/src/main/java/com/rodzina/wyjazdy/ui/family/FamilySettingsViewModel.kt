package com.rodzina.wyjazdy.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodzina.wyjazdy.data.repository.FamilyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FamilySettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class FamilySettingsViewModel(
    private val familyId: String,
    private val familyRepository: FamilyRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FamilySettingsUiState())
    val state: StateFlow<FamilySettingsUiState> = _state

    private fun run(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = FamilySettingsUiState(isLoading = true)
            try {
                block()
                _state.value = FamilySettingsUiState()
            } catch (e: Exception) {
                _state.value = FamilySettingsUiState(errorMessage = e.message ?: "Operacja nie powiodła się")
            }
        }
    }

    fun regenerateInviteCode() = run { familyRepository.regenerateInviteCode(familyId) }
    fun transferAdmin(newAdminUserId: String) = run { familyRepository.transferAdmin(familyId, newAdminUserId) }
    fun removeMember(userId: String) = run { familyRepository.removeMember(familyId, userId) }

    companion object {
        fun factory(familyId: String, familyRepository: FamilyRepository) = viewModelFactory {
            initializer { FamilySettingsViewModel(familyId, familyRepository) }
        }
    }
}
