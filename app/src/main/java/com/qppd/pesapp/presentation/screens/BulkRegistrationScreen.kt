package com.qppd.pesapp.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Import for back arrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.qppd.pesapp.auth.BulkRegistrationManager
import com.qppd.pesapp.models.BulkRegistrationData
import com.qppd.pesapp.models.RegistrationStatus
import com.qppd.pesapp.utils.CSVProcessor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkRegistrationScreen(
) {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var registrationData by remember { mutableStateOf<List<BulkRegistrationData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val bulkManager = BulkRegistrationManager.getInstance()

    // File picker launcher
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        if (uri != null) {
            // Process the CSV file
            (context as? androidx.activity.ComponentActivity)?.lifecycleScope?.launch {
                try {
                    isLoading = true
                    errorMessage = ""
                    successMessage = "" // Clear previous success messages
                    registrationData = emptyList() // Clear previous data
                    showPreview = false

                    val csvRows = CSVProcessor.readCSVFile(context, uri)
                    val processedData = bulkManager.processExcelData(csvRows)

                    // Validate data
                    val validationErrors = bulkManager.validateRegistrationData(processedData)
                    if (validationErrors.isNotEmpty()) {
                        errorMessage = "Validation errors:\n${validationErrors.joinToString("\n")}"
                    } else {
                        registrationData = processedData
                        showPreview = true // Show preview immediately after successful processing
                    }
                } catch (e: Exception) {
                    errorMessage = "Error processing file: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold( // Using Scaffold to easily add a TopAppBar with a back button
        topBar = {
            TopAppBar(
                title = { Text("Bulk User Registration") },
                navigationIcon = {
                    IconButton(onClick = { (context as? androidx.activity.ComponentActivity)?.onBackPressed() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp) // Your original padding
        ) {
            // Instructions Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) // Changed color for visual distinction
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Instructions:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Prepare a CSV file with columns: Parent Name, Parent Email, Children Names")
                    Text("2. Children names should be comma-separated (all in one cell)")
                    Text("3. First row should be headers")
                    Text("4. Upload the file to preview and register users")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            val csv = "Parent Name,Parent Email,Children Names\nJuan Dela Cruz,juan@email.com,Maria Dela Cruz, Pedro Dela Cruz\nMaria Santos,maria@email.com,Ana Santos\n"
                            val fileName = "parent_registration_template.csv"
                            // Consider providing a way for the user to choose where to save or share it
                            // For simplicity, this still saves to cacheDir.
                            try {
                                val file = java.io.File(context.cacheDir, fileName)
                                file.writeText(csv)
                                successMessage = "Template '$fileName' saved to app cache."
                                // TODO: Implement file sharing or opening functionality
                            } catch (e: Exception) {
                                errorMessage = "Could not save template: ${e.message}"
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download Template")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upload section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Upload CSV File",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            errorMessage = "" // Clear error before picking new file
                            successMessage = "" // Clear success message
                            filePicker.launch("text/csv")
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select CSV File")
                    }
                    if (selectedFileUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "File: ${selectedFileUri?.path?.split('/')?.lastOrNull() ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Error message
            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Success message
            if (successMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer) // Using tertiary for general success
                ) {
                    Text(
                        text = successMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Preview and Register buttons
            // Changed the logic for when "Preview Data" button is shown
            if (registrationData.isNotEmpty() && !isLoading) { // Show if data exists and not loading
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Toggle Preview Button (renamed for clarity)
                    OutlinedButton(
                        onClick = { showPreview = !showPreview }, // Toggle preview visibility
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (showPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showPreview) "Hide Preview" else "Show Preview")
                    }

                    Button(
                        onClick = {
                            (context as? androidx.activity.ComponentActivity)?.lifecycleScope?.launch {
                                isLoading = true
                                errorMessage = ""
                                successMessage = ""
                                try {
                                    val results = bulkManager.registerUsers(registrationData.filter { it.status == RegistrationStatus.PENDING }) // Only register pending
                                    val successCount = results.count { it.status == RegistrationStatus.SUCCESS }
                                    val failedCount = results.count { it.status == RegistrationStatus.FAILED }

                                    successMessage = "Registration process completed!\nSuccessful: $successCount\nFailed: $failedCount"
                                    // Update the original list with new statuses
                                    val updatedData = registrationData.map { existingData ->
                                        results.find { it.parentEmail == existingData.parentEmail } ?: existingData
                                    }
                                    registrationData = updatedData
                                    showPreview = true // Keep preview open to show results
                                } catch (e: Exception) {
                                    errorMessage = "Registration failed: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && registrationData.any { it.status == RegistrationStatus.PENDING } // Enable if there are pending items
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Register Users")
                    }
                }
            }

            // Data preview table
            if (showPreview && registrationData.isNotEmpty() && !isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Registration Data Preview/Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f), // Allow LazyColumn to take available space
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(registrationData) { data ->
                        RegistrationDataCard(data = data)
                    }
                }
            }
        }
    }
}

@Composable
fun RegistrationDataCard(data: BulkRegistrationData) {
    val statusColor = when (data.status) {
        RegistrationStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        RegistrationStatus.FAILED -> MaterialTheme.colorScheme.error
        RegistrationStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusIcon = when (data.status) {
        RegistrationStatus.SUCCESS -> Icons.Default.CheckCircle
        RegistrationStatus.FAILED -> Icons.Default.ErrorOutline // Changed for better visual
        RegistrationStatus.PENDING -> Icons.Default.HourglassEmpty // Changed for better visual
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (data.status) {
                RegistrationStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                RegistrationStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                RegistrationStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.parentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = data.status.name,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = data.status.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Email: ${data.parentEmail}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Children: ${data.children.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)

            if (data.status != RegistrationStatus.FAILED && data.generatedPassword.isNotBlank()) {
                Text(
                    text = "Password: ${data.generatedPassword}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (data.status == RegistrationStatus.FAILED && data.errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Error: ${data.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
