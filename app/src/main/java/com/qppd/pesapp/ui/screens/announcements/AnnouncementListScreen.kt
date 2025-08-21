package com.qppd.pesapp.ui.screens.announcements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qppd.pesapp.domain.model.Announcement
import com.qppd.pesapp.ui.components.EmptyStateView
import com.qppd.pesapp.ui.components.ErrorView
import com.qppd.pesapp.ui.components.LoadingSpinner
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnnouncementListScreen(
    onAnnouncementClick: (String) -> Unit,
    onCreateAnnouncement: () -> Unit,
    viewModel: AnnouncementListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        floatingActionButton = {
            if (uiState.canCreateAnnouncement) {
                FloatingActionButton(onClick = onCreateAnnouncement) {
                    Icon(Icons.Default.Add, contentDescription = "Create Announcement")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                uiState.isLoading -> LoadingSpinner()
                uiState.error != null -> ErrorView(
                    message = uiState.error!!,
                    onRetry = viewModel::loadAnnouncements
                )
                uiState.announcements.isEmpty() -> EmptyStateView(
                    title = "No Announcements",
                    message = "There are no announcements yet.",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Announcement,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                )
                else -> AnnouncementList(
                    announcements = uiState.announcements,
                    onAnnouncementClick = onAnnouncementClick
                )
            }
        }
    }
}

@Composable
private fun AnnouncementList(
    announcements: List<Announcement>,
    onAnnouncementClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(announcements) { announcement ->
            AnnouncementCard(
                announcement = announcement,
                onClick = { onAnnouncementClick(announcement.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementCard(
    announcement: Announcement,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (announcement.attachments.isNotEmpty()) {
                    Text(
                        text = "${announcement.attachments.size} attachment(s)",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(announcement.createdAt)),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
