package com.qppd.pesapp.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qppd.pesapp.R
import com.qppd.pesapp.auth.AuthManager
import com.qppd.pesapp.auth.ProfileManager
import com.qppd.pesapp.auth.UserManager
import com.qppd.pesapp.models.Profile
import com.qppd.pesapp.models.UserRole
import com.qppd.pesapp.utils.SupabaseImageUploader
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val authManager = AuthManager.getInstance()
    val profileManager = ProfileManager.getInstance()
    
    var profile by remember { mutableStateOf<Profile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingField by remember { mutableStateOf("") }
    var showPreferencesDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Upload the image
            isUploading = true
            // This would typically be done in a ViewModel or similar
            (context as? androidx.activity.ComponentActivity)?.lifecycleScope?.launch {
                try {
                    val imageUrl = SupabaseImageUploader.uploadImage(context, it, "profiles")
                    if (imageUrl != null) {
                        val result = profileManager.updateProfileImage(imageUrl)
                        if (result.isSuccess) {
                            // Refresh profile data
                            profile = profileManager.getCurrentUserProfile()
                        } else {
                            errorMessage = "Failed to update profile image"
                        }
                    } else {
                        errorMessage = "Failed to upload image"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error uploading image: ${e.message}"
                } finally {
                    isUploading = false
                }
            }
        }
    }
    
    // Load profile data
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            profile = profileManager.getCurrentUserProfile()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load profile: ${e.message}"
            isLoading = false
        }
    }
    
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (errorMessage.isNotBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: $errorMessage")
        }
        return
    }
    
    profile?.let { userProfile ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image with Upload Button
                Box(modifier = Modifier.size(130.dp)) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(130.dp)
                                .align(Alignment.Center)
                        )
                    } else if (userProfile.profileImage.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userProfile.profileImage)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .align(Alignment.Center)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.profile),
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .align(Alignment.Center)
                        )
                    }
                    
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Upload",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = userProfile.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = userProfile.role.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Basic Information Section
                SectionHeader("Basic Information")
                
                // Email
                InfoCard(
                    title = "Email",
                    value = userProfile.email,
                    icon = Icons.Default.Email,
                    onEditClick = {
                        editingField = "email"
                        showEditDialog = true
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Contact
                InfoCard(
                    title = "Contact",
                    value = userProfile.contactNumber.ifBlank { "Not set" },
                    icon = Icons.Default.Phone,
                    onEditClick = {
                        editingField = "contactNumber"
                        showEditDialog = true
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Bio
                InfoCard(
                    title = "Bio",
                    value = userProfile.bio.ifBlank { "Tell us about yourself..." },
                    icon = Icons.Default.Info,
                    onEditClick = {
                        editingField = "bio"
                        showEditDialog = true
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Address
                InfoCard(
                    title = "Address",
                    value = userProfile.address.ifBlank { "Not set" },
                    icon = Icons.Default.Home,
                    onEditClick = {
                        editingField = "address"
                        showEditDialog = true
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Emergency Contact
                InfoCard(
                    title = "Emergency Contact",
                    value = userProfile.emergencyContact.ifBlank { "Not set" },
                    icon = Icons.Default.MedicalServices,
                    onEditClick = {
                        editingField = "emergencyContact"
                        showEditDialog = true
                    }
                )
                
                // Only show children section for parents
                if (userProfile.role == UserRole.PARENT && userProfile.children.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader("Children")
                    ChildrenCard(children = userProfile.children)
                }
                
                // Preferences Section
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader("Preferences")
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notification Settings",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            IconButton(onClick = { showPreferencesDialog = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Edit Preferences")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Display current preferences
                        if (userProfile.preferences.isEmpty()) {
                            Text("No preferences set")
                        } else {
                            userProfile.preferences.forEach { (key, enabled) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (enabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(formatPreferenceKey(key))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Logout Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Profile not found")
        }
    }
    
    // Edit Profile Dialog
    if (showEditDialog) {
        profile?.let { userProfile ->
            val currentValue = when (editingField) {
                "email" -> userProfile.email
                "contactNumber" -> userProfile.contactNumber
                "bio" -> userProfile.bio
                "address" -> userProfile.address
                "emergencyContact" -> userProfile.emergencyContact
                else -> ""
            }
            
            EditProfileDialog(
                field = editingField,
                currentValue = currentValue,
                onDismiss = { showEditDialog = false },
                onSave = { newValue ->
                    // Update the profile
                    (context as? androidx.activity.ComponentActivity)?.lifecycleScope?.launch {
                        val updates = mapOf(editingField to newValue)
                        val result = profileManager.updateProfile(updates)
                        if (result.isSuccess) {
                            // Refresh profile data
                            profile = profileManager.getCurrentUserProfile()
                        } else {
                            errorMessage = "Failed to update profile"
                        }
                    }
                    showEditDialog = false
                }
            )
        }
    }
    
    // Preferences Dialog
    if (showPreferencesDialog) {
        profile?.let { userProfile ->
            PreferencesDialog(
                currentPreferences = userProfile.preferences,
                onDismiss = { showPreferencesDialog = false },
                onSave = { newPreferences ->
                    // Update preferences
                    (context as? androidx.activity.ComponentActivity)?.lifecycleScope?.launch {
                        val result = profileManager.updatePreferences(newPreferences)
                        if (result.isSuccess) {
                            // Refresh profile data
                            profile = profileManager.getCurrentUserProfile()
                        } else {
                            errorMessage = "Failed to update preferences"
                        }
                    }
                    showPreferencesDialog = false
                }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Divider(modifier = Modifier.weight(1f))
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.labelMedium)
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit $title")
            }
        }
    }
}

@Composable
fun ChildrenCard(children: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Children in Panaon Elementary School",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            children.forEachIndexed { index: Int, name: String ->
                Text(
                    text = "• $name",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                if (index != children.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    field: String,
    currentValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newValue by remember { mutableStateOf(currentValue) }
    val isSingleLine = field != "bio"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${formatFieldName(field)}") },
        text = {
            if (isSingleLine) {
                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    label = { Text(formatFieldName(field)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    label = { Text(formatFieldName(field)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    singleLine = false,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(newValue) },
                enabled = newValue.isNotBlank() || field == "bio"
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
fun PreferencesDialog(
    currentPreferences: Map<String, Boolean>,
    onDismiss: () -> Unit,
    onSave: (Map<String, Boolean>) -> Unit
) {
    val defaultPreferences = mapOf(
        "email_notifications" to true,
        "push_notifications" to true,
        "event_reminders" to true,
        "announcement_alerts" to true,
        "financial_updates" to false
    )
    
    val preferences = remember {
        mutableStateMapOf<String, Boolean>().apply {
            putAll(defaultPreferences)
            putAll(currentPreferences)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Preferences") },
        text = {
            Column {
                preferences.forEach { (key, enabled) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatPreferenceKey(key),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = { preferences[key] = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(preferences) }) {
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

// Helper function to format field names
fun formatFieldName(field: String): String {
    return when (field) {
        "contactNumber" -> "Contact Number"
        "emergencyContact" -> "Emergency Contact"
        else -> field.replaceFirstChar { it.uppercase() }
    }
}

// Helper function to format preference keys
fun formatPreferenceKey(key: String): String {
    return when (key) {
        "email_notifications" -> "Email Notifications"
        "push_notifications" -> "Push Notifications"
        "event_reminders" -> "Event Reminders"
        "announcement_alerts" -> "Announcement Alerts"
        "financial_updates" -> "Financial Updates"
        else -> key.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }
}