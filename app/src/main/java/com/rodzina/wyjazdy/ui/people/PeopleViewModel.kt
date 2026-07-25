package com.rodzina.wyjazdy.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodzina.wyjazdy.data.repository.FamilyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PeopleUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class PeopleViewModel(
    private val familyId: String,
    private val familyRepository: FamilyRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PeopleUiState())
    val state: StateFlow<PeopleUiState> = _state

    private fun run(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.value = PeopleUiState(isLoading = true)
            try {
                block()
                _state.value = PeopleUiState()
            } catch (e: Exception) {
                _state.value = PeopleUiState(errorMessage = e.message ?: "Operacja nie powiodła się")
            }
        }
    }

    fun regenerateInviteCode() = run { familyRepository.regenerateInviteCode(familyId) }
    fun transferAdmin(newAdminUserId: String) = run { familyRepository.transferAdmin(familyId, newAdminUserId) }
    fun removeMember(userId: String) = run { familyRepository.removeMember(familyId, userId) }

    companion object {
        fun factory(familyId: String, familyRepository: FamilyRepository) = viewModelFactory {
            initializer { PeopleViewModel(familyId, familyRepository) }
        }
    }
}
