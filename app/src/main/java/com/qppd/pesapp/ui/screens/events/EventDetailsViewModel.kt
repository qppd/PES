package com.qppd.pesapp.ui.screens.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qppd.pesapp.auth.AuthManager
import com.qppd.pesapp.data.repository.EventRepository
import com.qppd.pesapp.domain.model.Event
import com.qppd.pesapp.domain.model.EventAttendee
import com.qppd.pesapp.domain.model.AttendanceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EventDetailsUiState(
    val event: Event? = null,
    val attendees: List<EventAttendee> = emptyList(),
    val userAttendanceStatus: AttendanceStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authManager: AuthManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val eventId: String = checkNotNull(savedStateHandle["eventId"])
    
    private val _uiState = MutableStateFlow(EventDetailsUiState(isLoading = true))
    val uiState: StateFlow<EventDetailsUiState> = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    fun loadEvent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val userId = authManager.getCurrentUserId()
                if (userId == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Not authenticated"
                        )
                    }
                    return@launch
                }

                combine(
                    eventRepository.observeEventById(eventId),
                    eventRepository.observeEventAttendees(eventId)
                ) { event, attendees ->
                    val userAttendance = attendees.find { it.userId == userId }
                    Triple(event, attendees, userAttendance?.status)
                }.collect { (event, attendees, userStatus) ->
                    if (event == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Event not found"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                event = event,
                                attendees = attendees,
                                userAttendanceStatus = userStatus
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load event"
                    )
                }
            }
        }
    }

    fun updateAttendance(status: AttendanceStatus) {
        viewModelScope.launch {
            try {
                val userId = authManager.getCurrentUserId() ?: return@launch
                
                val eventAttendee = EventAttendee(
                    id = UUID.randomUUID().toString(),
                    eventId = eventId,
                    userId = userId,
                    status = status,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                eventRepository.updateEventAttendance(eventAttendee)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
}
