package com.qppd.pesapp.ui.screens.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qppd.pesapp.auth.AuthManager
import com.qppd.pesapp.data.repository.AnnouncementRepository
import com.qppd.pesapp.data.repository.UserRepository
import com.qppd.pesapp.domain.model.Announcement
import com.qppd.pesapp.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnnouncementListUiState(
    val announcements: List<Announcement> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val canCreateAnnouncement: Boolean = false
)

@HiltViewModel
class AnnouncementListViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    private val userRepository: UserRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnnouncementListUiState(isLoading = true))
    val uiState: StateFlow<AnnouncementListUiState> = _uiState.asStateFlow()

    init {
        loadAnnouncements()
        checkCreatePermission()
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Get current user's school
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
                        announcementRepository.observeAnnouncementsBySchool(user.schoolId)
                    }
                    .collect { announcements ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                announcements = announcements.sortedByDescending { it.createdAt }
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load announcements"
                    )
                }
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
                            canCreateAnnouncement = user.role in listOf(
                                UserRole.ADMIN,
                                UserRole.TEACHER
                            )
                        )
                    }
                }
        }
    }
}
