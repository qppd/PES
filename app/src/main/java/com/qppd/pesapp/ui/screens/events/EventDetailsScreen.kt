package com.qppd.pesapp.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qppd.pesapp.domain.model.Event
import com.qppd.pesapp.domain.model.EventAttendee
import com.qppd.pesapp.domain.model.AttendanceStatus
import com.qppd.pesapp.ui.components.LoadingSpinner
import com.qppd.pesapp.ui.components.ErrorView
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    onNavigateUp: () -> Unit,
    viewModel: EventDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingSpinner()
                uiState.error != null -> ErrorView(
                    message = uiState.error!!,
                    onRetry = viewModel::loadEvent
                )
                uiState.event != null -> EventDetails(
                    event = uiState.event!!,
                    attendees = uiState.attendees,
                    userAttendanceStatus = uiState.userAttendanceStatus,
                    onUpdateAttendance = viewModel::updateAttendance
                )
            }
        }
    }
}

@Composable
private fun EventDetails(
    event: Event,
    attendees: List<EventAttendee>,
    userAttendanceStatus: AttendanceStatus?,
    onUpdateAttendance: (AttendanceStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        
        item {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        item {
            EventMetadata(event)
        }
        
        if (event.requiresRSVP) {
            item {
                RsvpSection(
                    currentStatus = userAttendanceStatus,
                    onUpdateStatus = onUpdateAttendance,
                    maxAttendees = event.maxAttendees,
                    confirmedCount = attendees.count { it.status == AttendanceStatus.CONFIRMED }
                )
            }
        }
        
        if (event.attachments.isNotEmpty()) {
            item {
                Text(
                    text = "Attachments",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(event.attachments) { attachment ->
                ListItem(
                    headlineContent = { Text(attachment.name) },
                    leadingContent = {
                        Icon(
                            imageVector = when (attachment.type) {
                                AttachmentType.IMAGE -> Icons.Default.Image
                                AttachmentType.DOCUMENT -> Icons.Default.Description
                                AttachmentType.VIDEO -> Icons.Default.VideoFile
                            },
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun EventMetadata(event: Event) {
    val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = dateFormat.format(Date(event.startDate)),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${timeFormat.format(Date(event.startDate))} - ${timeFormat.format(Date(event.endDate))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (event.location != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun RsvpSection(
    currentStatus: AttendanceStatus?,
    onUpdateStatus: (AttendanceStatus) -> Unit,
    maxAttendees: Int?,
    confirmedCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "RSVP",
            style = MaterialTheme.typography.titleMedium
        )
        
        if (maxAttendees != null) {
            Text(
                text = "$confirmedCount/${maxAttendees} attending",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            LinearProgressIndicator(
                progress = confirmedCount.toFloat() / maxAttendees,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { onUpdateStatus(AttendanceStatus.CONFIRMED) },
                label = { Text("Going") },
                leadingIcon = if (currentStatus == AttendanceStatus.CONFIRMED) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
            
            AssistChip(
                onClick = { onUpdateStatus(AttendanceStatus.DECLINED) },
                label = { Text("Not Going") },
                leadingIcon = if (currentStatus == AttendanceStatus.DECLINED) {
                    { Icon(Icons.Default.Close, contentDescription = null) }
                } else null
            )
        }
    }
}
