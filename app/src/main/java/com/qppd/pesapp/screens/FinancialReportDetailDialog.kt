package com.qppd.pesapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun FinancialReportDetailDialog(
    report: FinancialReport,
    onDismiss: () -> Unit
) {
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
                        text = "Financial Report Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Basic Information
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = report.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = report.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Category: ${report.category.name.replace("_", " ")}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Status: ${report.status.name.replace("_", " ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (report.status) {
                                            ReportStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                                            ReportStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                                            ReportStatus.ON_HOLD -> MaterialTheme.colorScheme.tertiary
                                            ReportStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        // Budget Summary
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Budget Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Budget:")
                                    Text(
                                        "₱${String.format("%,.2f", report.totalBudget)}",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Expenses:")
                                    Text(
                                        "₱${String.format("%,.2f", report.totalExpenses)}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Solicitations:")
                                    Text(
                                        "₱${String.format("%,.2f", report.totalSolicitations)}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Remaining Budget:")
                                    Text(
                                        "₱${String.format("%,.2f", report.remainingBudget)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (report.remainingBudget < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        // Dates
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Timeline",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Start Date: ${dateFormat.format(Date(report.startDate))}")
                                if (report.endDate != null) {
                                    Text("End Date: ${dateFormat.format(Date(report.endDate))}")
                                }
                                Text("Created: ${dateFormat.format(Date(report.createdAt))}")
                                Text("Last Updated: ${dateFormat.format(Date(report.updatedAt))}")
                            }
                        }
                    }
                    
                    // Expenses Section
                    if (report.expenses.isNotEmpty()) {
                        item {
                            Text(
                                text = "Expenses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(report.expenses) { expense ->
                            ExpenseCard(expense = expense, dateFormat = dateFormat)
                        }
                    }
                    
                    // Solicitations Section
                    if (report.solicitations.isNotEmpty()) {
                        item {
                            Text(
                                text = "Solicitations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(report.solicitations) { solicitation ->
                            SolicitationCard(solicitation = solicitation, dateFormat = dateFormat)
                        }
                    }
                    
                    // Notes Section
                    if (report.notes.isNotBlank()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Notes",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(report.notes)
                                }
                            }
                        }
                    }
                    
                    // Author Information
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Report Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (report.authorName.isNotBlank()) {
                                    Text("Created by: ${report.authorName}")
                                }
                                if (report.attachments.isNotEmpty()) {
                                    Text("Attachments: ${report.attachments.size} file(s)")
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(expense: Expense, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (expense.category.isNotBlank()) {
                        Text(
                            text = "Category: ${expense.category}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Date: ${dateFormat.format(Date(expense.date))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (expense.approvedBy.isNotBlank()) {
                        Text(
                            text = "Approved by: ${expense.approvedBy}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (expense.notes.isNotBlank()) {
                        Text(
                            text = "Notes: ${expense.notes}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Text(
                    text = "₱${String.format("%,.2f", expense.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SolicitationCard(solicitation: Solicitation, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = solicitation.donorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Type: ${solicitation.type.name.replace("_", " ")}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (solicitation.purpose.isNotBlank()) {
                        Text(
                            text = "Purpose: ${solicitation.purpose}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Date: ${dateFormat.format(Date(solicitation.date))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (solicitation.contactInfo.isNotBlank()) {
                        Text(
                            text = "Contact: ${solicitation.contactInfo}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (solicitation.notes.isNotBlank()) {
                        Text(
                            text = "Notes: ${solicitation.notes}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Text(
                    text = "₱${String.format("%,.2f", solicitation.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
} 