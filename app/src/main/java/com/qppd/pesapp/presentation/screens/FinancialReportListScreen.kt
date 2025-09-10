package com.qppd.pesapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.qppd.pesapp.auth.FinancialReportManager
import com.qppd.pesapp.models.FinancialReport
import com.qppd.pesapp.models.ReportCategory
import com.qppd.pesapp.models.ReportStatus
import com.qppd.pesapp.models.UserRole
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportListScreen(currentUserRole: UserRole) {
    var reports by remember { mutableStateOf<List<FinancialReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<FinancialReport?>(null) }
    var showDetailDialog by remember { mutableStateOf<FinancialReport?>(null) }
    var selectedCategory by remember { mutableStateOf<ReportCategory?>(null) }
    var selectedStatus by remember { mutableStateOf<ReportStatus?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var totalBudget by remember { mutableStateOf(0.0) }
    var totalExpenses by remember { mutableStateOf(0.0) }
    var totalSolicitations by remember { mutableStateOf(0.0) }
    var remainingBudget by remember { mutableStateOf(0.0) }
    var errorMessage by remember { mutableStateOf("") }
    var reportToDelete by remember { mutableStateOf<FinancialReport?>(null) }
    var pendingReportToAdd by remember { mutableStateOf<FinancialReport?>(null) }
    var pendingReportToEdit by remember { mutableStateOf<FinancialReport?>(null) }
    
    val reportManager = FinancialReportManager.getInstance()
    
    // Load reports and summary data
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val allReports = reportManager.getAllReports()
            
            // If no reports exist, add sample data for testing
            if (allReports.isEmpty()) {
                val sampleReports = listOf(
                    FinancialReport(
                        id = "1",
                        title = "Coco Lilay Festival 2025",
                        description = "Annual school festival with cultural performances and food stalls",
                        category = ReportCategory.EVENTS,
                        totalBudget = 50000.0,
                        remainingBudget = 12500.0,
                        totalExpenses = 37500.0,
                        totalSolicitations = 15000.0,
                        status = ReportStatus.ACTIVE,
                        authorName = "School Admin",
                        startDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L), // 30 days ago
                        notes = "Major annual event for the school community"
                    ),
                    FinancialReport(
                        id = "2",
                        title = "Solar Power Station Project",
                        description = "Installation of solar panels for sustainable energy",
                        category = ReportCategory.INFRASTRUCTURE,
                        totalBudget = 150000.0,
                        remainingBudget = 0.0,
                        totalExpenses = 150000.0,
                        totalSolicitations = 75000.0,
                        status = ReportStatus.COMPLETED,
                        authorName = "School Admin",
                        startDate = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L), // 90 days ago
                        endDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L), // 30 days ago
                        notes = "Successfully completed solar installation"
                    ),
                    FinancialReport(
                        id = "3",
                        title = "School Supplies Fund",
                        description = "Monthly budget for classroom supplies and materials",
                        category = ReportCategory.SUPPLIES,
                        totalBudget = 25000.0,
                        remainingBudget = 8500.0,
                        totalExpenses = 16500.0,
                        totalSolicitations = 5000.0,
                        status = ReportStatus.ACTIVE,
                        authorName = "School Admin",
                        startDate = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000L), // 15 days ago
                        notes = "Ongoing monthly supplies budget"
                    )
                )
                
                // Add sample reports to Firestore
                sampleReports.forEach { report ->
                    reportManager.addReport(report)
                }
                reports = sampleReports
            } else {
                reports = allReports
            }
            
            // Calculate summary
            totalBudget = reportManager.getTotalBudget()
            totalExpenses = reportManager.getTotalExpenses()
            totalSolicitations = reportManager.getTotalSolicitations()
            remainingBudget = reportManager.getRemainingBudget()
            
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load reports: ${e.message}"
            isLoading = false
        }
    }
    
    // Filter reports based on selected category and status
    val filteredReports = remember(reports, selectedCategory, selectedStatus) {
        reports.filter { report ->
            (selectedCategory == null || report.category == selectedCategory) &&
            (selectedStatus == null || report.status == selectedStatus)
        }
    }
    
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
                text = "Financial Reports",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                // Filter button
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
                
                // Add button (Admin/Teacher only)
                if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.TEACHER) {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Report")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Summary Cards
        FinancialSummaryCards(
            totalBudget = totalBudget,
            totalExpenses = totalExpenses,
            totalSolicitations = totalSolicitations,
            remainingBudget = remainingBudget
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filters
        if (showFilters) {
            FilterSection(
                selectedCategory = selectedCategory,
                selectedStatus = selectedStatus,
                onCategorySelected = { selectedCategory = it },
                onStatusSelected = { selectedStatus = it },
                onClearFilters = {
                    selectedCategory = null
                    selectedStatus = null
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Reports List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredReports.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No financial reports found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredReports) { report ->
                    FinancialReportCard(
                        report = report,
                        canEdit = currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.TEACHER,
                        onViewDetails = { showDetailDialog = report },
                        onEdit = { showEditDialog = report },
                        onDelete = { reportToDelete = it }
                    )
                }
            }
        }
    }
    
    // Add Report Dialog
    val addDialogOpen = showAddDialog
    if (addDialogOpen) {
        AddEditFinancialReportDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newReport ->
                pendingReportToAdd = newReport
            }
        )
    }
    
    // Edit Report Dialog
    val editDialogReport = showEditDialog
    if (editDialogReport != null) {
        AddEditFinancialReportDialog(
            initialReport = editDialogReport,
            onDismiss = { showEditDialog = null },
            onSave = { updatedReport ->
                pendingReportToEdit = updatedReport
            }
        )
    }
    
    // Detail Dialog
    val detailDialogReport = showDetailDialog
    if (detailDialogReport != null) {
        FinancialReportDetailDialog(
            report = detailDialogReport,
            onDismiss = { showDetailDialog = null }
        )
    }

    if (reportToDelete != null) {
        LaunchedEffect(reportToDelete) {
            val result = reportManager.deleteReport(reportToDelete!!.id)
            result.fold(
                onSuccess = {
                    reports = reports.filter { it.id != reportToDelete!!.id }
                    reportToDelete = null
                },
                onFailure = { exception ->
                    errorMessage = "Failed to delete report: ${exception.message}"
                    reportToDelete = null
                }
            )
        }
    }

    if (pendingReportToAdd != null) {
        LaunchedEffect(pendingReportToAdd) {
            val result = reportManager.addReport(pendingReportToAdd!!)
            result.fold(
                onSuccess = {
                    reports = listOf(pendingReportToAdd!!) + reports
                    showAddDialog = false
                    pendingReportToAdd = null
                },
                onFailure = { exception ->
                    errorMessage = "Failed to add report: ${exception.message}"
                    pendingReportToAdd = null
                }
            )
        }
    }

    if (pendingReportToEdit != null) {
        LaunchedEffect(pendingReportToEdit) {
            val result = reportManager.updateReport(pendingReportToEdit!!)
            result.fold(
                onSuccess = {
                    reports = reports.map { if (it.id == pendingReportToEdit!!.id) pendingReportToEdit!! else it }
                    showEditDialog = null
                    pendingReportToEdit = null
                },
                onFailure = { exception ->
                    errorMessage = "Failed to update report: ${exception.message}"
                    pendingReportToEdit = null
                }
            )
        }
    }
    
    // Error Snackbar
    if (errorMessage.isNotEmpty()) {
        LaunchedEffect(errorMessage) {
            delay(3000)
            errorMessage = ""
        }
    }
}

@Composable
fun FinancialSummaryCards(
    totalBudget: Double,
    totalExpenses: Double,
    totalSolicitations: Double,
    remainingBudget: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Total Budget",
                amount = totalBudget,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Total Expenses",
                amount = totalExpenses,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Total Solicitations",
                amount = totalSolicitations,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Remaining Budget",
                amount = remainingBudget,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "₱${String.format("%,.2f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun FilterSection(
    selectedCategory: ReportCategory?,
    selectedStatus: ReportStatus?,
    onCategorySelected: (ReportCategory?) -> Unit,
    onStatusSelected: (ReportStatus?) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Category Filter
            Text(
                text = "Category:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                        label = { Text("All") }
                    )
                }
                items(ReportCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.name.replace("_", " ")) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status Filter
            Text(
                text = "Status:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { onStatusSelected(null) },
                        label = { Text("All") }
                    )
                }
                items(ReportStatus.values()) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onStatusSelected(status) },
                        label = { Text(status.name.replace("_", " ")) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onClearFilters,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear Filters")
            }
        }
    }
}

@Composable
fun FinancialReportCard(
    report: FinancialReport,
    canEdit: Boolean,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (FinancialReport) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = report.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (canEdit) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { onDelete(report) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Budget: ₱${String.format("%,.2f", report.totalBudget)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Remaining: ₱${String.format("%,.2f", report.remainingBudget)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (report.remainingBudget < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = report.category.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = report.status.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = when (report.status) {
                            ReportStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                            ReportStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                            ReportStatus.ON_HOLD -> MaterialTheme.colorScheme.tertiary
                            ReportStatus.CANCELLED -> MaterialTheme.colorScheme.error
                            ReportStatus.APPROVED -> MaterialTheme.colorScheme.primary
                            ReportStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                            ReportStatus.REJECTED -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Created: ${dateFormat.format(Date(report.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 
