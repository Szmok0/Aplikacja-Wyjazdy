package com.rodzina.wyjazdy.ui.tripdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rodzina.wyjazdy.data.model.StatusUpdate
import com.rodzina.wyjazdy.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TripDetailsViewModel(
    private val familyId: String,
    private val tripId: String,
    private val tripRepository: TripRepository,
) : ViewModel() {

    val statusUpdates: StateFlow<List<StatusUpdate>> = tripRepository.observeStatusUpdates(familyId, tripId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteTrip(onDeleted: () -> Unit) {
        viewModelScope.launch {
            tripRepository.deleteTrip(familyId, tripId)
            onDeleted()
        }
    }

    companion object {
        fun factory(familyId: String, tripId: String, tripRepository: TripRepository) = viewModelFactory {
            initializer { TripDetailsViewModel(familyId, tripId, tripRepository) }
        }
    }
}
