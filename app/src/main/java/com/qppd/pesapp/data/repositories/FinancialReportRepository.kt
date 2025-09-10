package com.qppd.pesapp.data.repositories

import com.qppd.pesapp.models.FinancialReport
import com.qppd.pesapp.models.ReportCategory
import com.qppd.pesapp.models.ReportStatus
import com.qppd.pesapp.data.remote.SupabaseManager
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SupabaseFinancialReport(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val report_type: String = "INCOME",
    val category: String = "OTHER",
    val report_date: String = "",
    val author_id: String = "",
    val author_name: String = "",
    val attachments: List<String> = emptyList(),
    val is_active: Boolean = true,
    val created_at: String = "",
    val updated_at: String = ""
)

fun SupabaseFinancialReport.toAppFinancialReport(): FinancialReport {
    return FinancialReport(
        id = id,
        title = title,
        description = description,
        amount = amount,
        reportType = report_type,
        category = try { ReportCategory.valueOf(category) } catch (e: Exception) { ReportCategory.OTHER },
        reportDate = try { 
            java.time.Instant.parse(report_date).toEpochMilli() 
        } catch (e: Exception) { 
            System.currentTimeMillis() 
        },
        authorId = author_id,
        authorName = author_name,
        attachments = attachments,
        status = ReportStatus.APPROVED, // Default for now
        isActive = is_active,
        createdAt = try { 
            java.time.Instant.parse(created_at).toEpochMilli() 
        } catch (e: Exception) { 
            System.currentTimeMillis() 
        },
        updatedAt = try { 
            java.time.Instant.parse(updated_at).toEpochMilli() 
        } catch (e: Exception) { 
            System.currentTimeMillis() 
        }
    )
}

fun FinancialReport.toSupabaseFinancialReport(): SupabaseFinancialReport {
    return SupabaseFinancialReport(
        id = id,
        title = title,
        description = description,
        amount = amount,
        report_type = reportType,
        category = category.name,
        report_date = java.time.Instant.ofEpochMilli(reportDate).toString(),
        author_id = authorId,
        author_name = authorName,
        attachments = attachments,
        is_active = isActive,
        created_at = java.time.Instant.ofEpochMilli(createdAt).toString(),
        updated_at = java.time.Instant.ofEpochMilli(updatedAt).toString()
    )
}

class FinancialReportRepository {

    companion object {
        @Volatile
        private var INSTANCE: FinancialReportRepository? = null
        fun getInstance(): FinancialReportRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FinancialReportRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun getAllReports(): List<FinancialReport> {
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { emptyList() }
            ) { client ->
                try {
                    val supabaseReports = client.from("financial_reports")
                        .select()
                        .decodeList<SupabaseFinancialReport>()
                        .filter { it.is_active }
                    
                    supabaseReports.map { it.toAppFinancialReport() }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }

    suspend fun getReportsByCategory(category: ReportCategory): List<FinancialReport> {
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { emptyList() }
            ) { client ->
                try {
                    val supabaseReports = client.from("financial_reports")
                        .select()
                        .decodeList<SupabaseFinancialReport>()
                        .filter { report -> report.is_active && report.category == category.name }
                        .sortedByDescending { report -> report.created_at }
                    
                    supabaseReports.map { report -> report.toAppFinancialReport() }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }

    suspend fun getReportsByStatus(status: ReportStatus): List<FinancialReport> {
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { emptyList() }
            ) { client ->
                try {
                    // Note: Status filtering would need to be added to database schema
                    // For now, filter client-side
                    val supabaseReports = client.from("financial_reports")
                        .select()
                        .decodeList<SupabaseFinancialReport>()
                        .filter { report -> report.is_active }
                        .sortedByDescending { report -> report.created_at }
                    
                    supabaseReports.map { report -> report.toAppFinancialReport() }
                        .filter { appReport -> appReport.status == status }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }

    suspend fun getReportById(id: String): FinancialReport? {
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { null }
            ) { client ->
                try {
                    val supabaseReports = client.from("financial_reports")
                        .select()
                        .decodeList<SupabaseFinancialReport>()
                        .filter { report -> report.id == id }
                    
                    supabaseReports.firstOrNull()?.toAppFinancialReport()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    suspend fun addReport(report: FinancialReport): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    val id = UUID.randomUUID().toString()
                    val newReport = report.copy(
                        id = id,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    client.from("financial_reports").insert(newReport.toSupabaseFinancialReport())
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateReport(report: FinancialReport): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    val updatedReport = report.copy(updatedAt = System.currentTimeMillis())
                    client.from("financial_reports")
                        .update(updatedReport.toSupabaseFinancialReport()) {
                            filter { eq("id", report.id) }
                        }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteReport(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    client.from("financial_reports")
                        .delete {
                            filter { eq("id", id) }
                        }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateReportStatus(id: String, status: ReportStatus): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    client.from("financial_reports")
                        .update(mapOf(
                            "updated_at" to java.time.Instant.now().toString()
                        )) {
                            filter { eq("id", id) }
                        }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getTotalBudget(): Double {
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { 0.0 }
            ) { client ->
                try {
                    val reports = client.from("financial_reports")
                        .select()
                        .decodeList<SupabaseFinancialReport>()
                        .filter { report -> report.is_active && report.report_type == "INCOME" }
                    
                    reports.sumOf { report -> report.amount }
                } catch (e: Exception) {
                    0.0
                }
            }
        }
    }

    suspend fun getTotalExpenses(): Double {
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { 0.0 }
            ) { client ->
                try {
                    val reports = client.from("financial_reports")
                        .select()
                        .decodeList<SupabaseFinancialReport>()
                        .filter { report -> report.is_active && report.report_type == "EXPENSE" }
                    
                    reports.sumOf { report -> report.amount }
                } catch (e: Exception) {
                    0.0
                }
            }
        }
    }

    suspend fun getTotalSolicitations(): Double {
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { 0.0 }
            ) { client ->
                try {
                    val reports = client.from("financial_reports")
                        .select()
                        .decodeList<SupabaseFinancialReport>()
                        .filter { report -> report.is_active && report.category == "SOLICITATION" }
                    
                    reports.sumOf { report -> report.amount }
                } catch (e: Exception) {
                    0.0
                }
            }
        }
    }

    suspend fun getRemainingBudget(): Double {
        return getTotalBudget() - getTotalExpenses()
    }
} 