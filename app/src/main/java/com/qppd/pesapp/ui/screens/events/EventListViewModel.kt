package com.qppd.pesapp.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qppd.pesapp.auth.AuthManager
import com.qppd.pesapp.data.repository.EventRepository
import com.qppd.pesapp.data.repository.UserRepository
import com.qppd.pesapp.domain.model.Event
import com.qppd.pesapp.domain.model.EventAttendee
import com.qppd.pesapp.domain.model.AttendanceStatus
import com.qppd.pesapp.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EventListUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val canCreateEvent: Boolean = false
)

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(EventListUiState(isLoading = true))
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
        checkCreatePermission()
    }

    fun loadEvents() {
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

                userRepository.observeUserById(userId)
                    .filterNotNull()
                    .flatMapLatest { user ->
                        eventRepository.observeUpcomingEvents(
                            schoolId = user.schoolId,
                            currentTime = System.currentTimeMillis()
                        )
                    }
                    .collect { events ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                events = events.sortedBy { it.startDate }
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load events"
                    )
                }
            }
        }
    }

    fun updateAttendance(eventId: String, status: AttendanceStatus) {
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
                // Handle error, maybe show a snackbar
                e.printStackTrace()
            }
        }
    }

    private fun checkCreatePermission() {
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId() ?: return@launch
            userRepository.observeUserById(userId)
                .filterNotNull()
                .collect { user ->
                    _uiState.update {
                        it.copy(
                            canCreateEvent = user.role in listOf(
                                UserRole.ADMIN,
                                UserRole.TEACHER
                            )
                        )
                    }
                }
        }
    }
}
