package com.qppd.pesapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.qppd.pesapp.data.repositories.AnnouncementRepository
import com.qppd.pesapp.models.Announcement
import com.qppd.pesapp.models.UserRole
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import com.qppd.pesapp.utils.SupabaseImageUploader
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementListScreen(currentUserRole: UserRole) {
    var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Announcement?>(null) }
    var showDetailDialog by remember { mutableStateOf<Announcement?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    val announcementRepository = AnnouncementRepository.getInstance()
    val context = LocalContext.current

    // Load announcements
    LaunchedEffect(Unit) {
        isLoading = true
        announcements = announcementRepository.getAllAnnouncements()
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Announcements",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.TEACHER) {
                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (announcements.isEmpty()) {
            Text("No announcements yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(announcements) { announcement ->
                    AnnouncementCard(
                        announcement = announcement,
                        canEdit = currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.TEACHER,
                        onEdit = { showEditDialog = it },
                        onDelete = {
                            (context as? androidx.activity.ComponentActivity)?.lifecycleScope?.launch {
                                isLoading = true
                                val result = announcementRepository.deleteAnnouncement(announcement.id)
                                if (result.isSuccess) {
                                    announcements = announcementRepository.getAllAnnouncements()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Delete failed"
                                }
                                isLoading = false
                            }
                        },
                        onClick = { showDetailDialog = it }
                    )
                }
            }
        }
        if (errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
    if (showAddDialog) {
        AnnouncementDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, content, imageUri ->
                (context as? androidx.activity.ComponentActivity)?.lifecycleScope?.launch {
                    isLoading = true
                    var imageUrl = ""
                    if (imageUri != null) {
                        imageUrl = SupabaseImageUploader.uploadImage(context, imageUri) ?: ""
                    }
                    val result = announcementRepository.addAnnouncement(
                        Announcement(title = title, content = content, imageUrl = imageUrl)
                    )
                    if (result.isSuccess) {
                        announcements = announcementRepository.getAllAnnouncements()
                        showAddDialog = false
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Add failed"
                    }
                    isLoading = false
                }
            }
        )
    }
    if (showEditDialog != null) {
        AnnouncementDialog(
            initialTitle = showEditDialog!!.title,
            initialContent = showEditDialog!!.content,
            initialImageUri = showEditDialog!!.imageUrl.takeIf { it.isNotBlank() }?.toUri(),
            onDismiss = { showEditDialog = null },
            onSave = { title, content, imageUri ->
                (context as? androidx.activity.ComponentActivity)?.lifecycleScope?.launch {
                    isLoading = true
                    var imageUrl = showEditDialog!!.imageUrl
                    if (imageUri != null && imageUri.toString() != imageUrl) {
                        imageUrl = SupabaseImageUploader.uploadImage(context, imageUri) ?: ""
                    }
                    val result = announcementRepository.updateAnnouncement(
                        showEditDialog!!.copy(title = title, content = content, imageUrl = imageUrl)
                    )
                    if (result.isSuccess) {
                        announcements = announcementRepository.getAllAnnouncements()
                        showEditDialog = null
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Update failed"
                    }
                    isLoading = false
                }
            }
        )
    }
    if (showDetailDialog != null) {
        AnnouncementDetailDialog(
            announcement = showDetailDialog!!,
            onDismiss = { showDetailDialog = null }
        )
    }
}

@Composable
fun AnnouncementCard(
    announcement: Announcement,
    canEdit: Boolean,
    onEdit: (Announcement) -> Unit,
    onDelete: () -> Unit,
    onClick: (Announcement) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(announcement) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (canEdit) {
                    Row {
                        IconButton(onClick = { onEdit(announcement) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(announcement.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        }
    }
}

@Composable
fun AnnouncementDialog(
    initialTitle: String = "",
    initialContent: String = "",
    initialImageUri: Uri? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, Uri?) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var imageUri by remember { mutableStateOf<Uri?>(initialImageUri) }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTitle.isBlank()) "Add Announcement" else "Edit Announcement") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { imagePicker.launch("image/*") }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (imageUri != null) "Change Image" else "Add Image")
                }
                if (imageUri != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Image selected: ${imageUri.toString().split("/").last()}", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, content, imageUri) },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AnnouncementDetailDialog(
    announcement: Announcement,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(announcement.title) },
        text = {
            Column {
                if (announcement.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = announcement.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                }
                Text(announcement.content, style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
} 
