package com.qppd.pesapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qppd.pesapp.models.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFinancialReportDialog(
    initialReport: FinancialReport? = null,
    onDismiss: () -> Unit,
    onSave: (FinancialReport) -> Unit
) {
    var title by remember { mutableStateOf(initialReport?.title ?: "") }
    var description by remember { mutableStateOf(initialReport?.description ?: "") }
    var category by remember { mutableStateOf(initialReport?.category ?: ReportCategory.GENERAL) }
    var totalBudget by remember { mutableStateOf(initialReport?.totalBudget?.toString() ?: "") }
    var startDate by remember { mutableStateOf(initialReport?.startDate ?: System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(initialReport?.endDate) }
    var status by remember { mutableStateOf(initialReport?.status ?: ReportStatus.ACTIVE) }
    var notes by remember { mutableStateOf(initialReport?.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var isDatePickerForEnd by remember { mutableStateOf(false) }
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialReport == null) "Add Financial Report" else "Edit Financial Report",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Form Content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Report Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    
                    item {
                        // Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                    
                    item {
                        // Category
                        var categoryExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = category.name.replace("_", " "),
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                for (cat in ReportCategory.values()) {
                                    DropdownMenuItem(
                                        text = { Text(cat.name.replace("_", " ")) },
                                        onClick = { 
                                            category = cat
                                            categoryExpanded = false 
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        // Total Budget
                        OutlinedTextField(
                            value = totalBudget,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    totalBudget = it
                                }
                            },
                            label = { Text("Total Budget (₱)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            prefix = { Text("₱") }
                        )
                    }
                    
                    item {
                        // Start Date
                        OutlinedTextField(
                            value = dateFormat.format(Date(startDate)),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Start Date") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { 
                                    isDatePickerForEnd = false
                                    showDatePicker = true 
                                }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                }
                            }
                        )
                    }
                    
                    item {
                        // End Date (Optional)
                        OutlinedTextField(
                            value = endDate?.let { dateFormat.format(Date(it)) } ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("End Date (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Row {
                                    if (endDate != null) {
                                        IconButton(onClick = { endDate = null }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear Date")
                                        }
                                    }
                                    IconButton(onClick = { 
                                        isDatePickerForEnd = true
                                        showEndDatePicker = true 
                                    }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                    }
                                }
                            }
                        )
                    }
                    
                    item {
                        // Status
                        var statusExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = status.name.replace("_", " "),
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                for (stat in ReportStatus.values()) {
                                    DropdownMenuItem(
                                        text = { Text(stat.name.replace("_", " ")) },
                                        onClick = {
                                            status = stat
                                            statusExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        // Notes
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val budget = totalBudget.toDoubleOrNull() ?: 0.0
                            val report = FinancialReport(
                                id = initialReport?.id ?: "",
                                title = title,
                                description = description,
                                category = category,
                                totalBudget = budget,
                                remainingBudget = budget, // Initially same as total budget
                                startDate = startDate,
                                endDate = endDate,
                                status = status,
                                notes = notes,
                                authorId = initialReport?.authorId ?: "",
                                authorName = initialReport?.authorName ?: "",
                                expenses = initialReport?.expenses ?: emptyList(),
                                solicitations = initialReport?.solicitations ?: emptyList(),
                                attachments = initialReport?.attachments ?: emptyList()
                            )
                            onSave(report)
                        },
                        enabled = title.isNotBlank() && description.isNotBlank() && totalBudget.isNotBlank()
                    ) {
                        Text(if (initialReport == null) "Add Report" else "Update Report")
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = rememberDatePickerState(initialSelectedDateMillis = startDate),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    
    // End Date Picker Dialog
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = rememberDatePickerState(initialSelectedDateMillis = endDate),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 