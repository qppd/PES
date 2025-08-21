package com.qppd.pesapp.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qppd.pesapp.domain.model.Event
import com.qppd.pesapp.domain.model.AttendanceStatus
import com.qppd.pesapp.ui.components.EmptyStateView
import com.qppd.pesapp.ui.components.ErrorView
import com.qppd.pesapp.ui.components.LoadingSpinner
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventListScreen(
    onEventClick: (String) -> Unit,
    onCreateEvent: () -> Unit,
    viewModel: EventListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        floatingActionButton = {
            if (uiState.canCreateEvent) {
                FloatingActionButton(onClick = onCreateEvent) {
                    Icon(Icons.Default.Add, contentDescription = "Create Event")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                uiState.isLoading -> LoadingSpinner()
                uiState.error != null -> ErrorView(
                    message = uiState.error!!,
                    onRetry = viewModel::loadEvents
                )
                uiState.events.isEmpty() -> EmptyStateView(
                    title = "No Events",
                    message = "There are no upcoming events.",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                )
                else -> EventList(
                    events = uiState.events,
                    onEventClick = onEventClick,
                    onRsvp = viewModel::updateAttendance
                )
            }
        }
    }
}

@Composable
private fun EventList(
    events: List<Event>,
    onEventClick: (String) -> Unit,
    onRsvp: (String, AttendanceStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(events) { event ->
            EventCard(
                event = event,
                onClick = { onEventClick(event.id) },
                onRsvp = { status -> onRsvp(event.id, status) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(
    event: Event,
    onClick: () -> Unit,
    onRsvp: (AttendanceStatus) -> Unit
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
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date and time
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateFormat.format(Date(event.startDate)),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = timeFormat.format(Date(event.startDate)),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            if (event.location != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (event.requiresRSVP) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { onRsvp(AttendanceStatus.CONFIRMED) },
                        label = { Text("Going") }
                    )
                    AssistChip(
                        onClick = { onRsvp(AttendanceStatus.DECLINED) },
                        label = { Text("Not Going") }
                    )
                }
            }
        }
    }
}
