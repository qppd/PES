package com.qppd.pesapp.auth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.qppd.pesapp.models.FinancialReport
import com.qppd.pesapp.models.ReportCategory
import com.qppd.pesapp.models.ReportStatus
import com.qppd.pesapp.cache.CacheManager
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FinancialReportManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("financial_reports")
    private val cacheManager = CacheManager.getInstance()

    companion object {
        @Volatile
        private var INSTANCE: FinancialReportManager? = null
        fun getInstance(): FinancialReportManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FinancialReportManager().also { INSTANCE = it }
            }
        }
    }

    suspend fun getAllReports(): List<FinancialReport> {
        return try {
            cacheManager.getWithBackgroundRefresh(
                key = CacheManager.CacheKeys.FINANCIAL_REPORTS,
                fetchFromNetwork = {
                    val snapshot = collection.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
                    snapshot.documents.mapNotNull { it.toObject(FinancialReport::class.java) }
                },
                expirationDuration = CacheManager.CacheDurations.FINANCIAL_REPORTS
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getReportsByCategory(category: ReportCategory): List<FinancialReport> {
        return try {
            val snapshot = collection
                .whereEqualTo("category", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(FinancialReport::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getReportsByStatus(status: ReportStatus): List<FinancialReport> {
        return try {
            val snapshot = collection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(FinancialReport::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getReportById(id: String): FinancialReport? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(FinancialReport::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addReport(report: FinancialReport): Result<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            val newReport = report.copy(
                id = id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            collection.document(id).set(newReport).await()
            // Invalidate cache
            cacheManager.remove(CacheManager.CacheKeys.FINANCIAL_REPORTS)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReport(report: FinancialReport): Result<Unit> {
        return try {
            collection.document(report.id).set(
                report.copy(updatedAt = System.currentTimeMillis())
            ).await()
            // Invalidate cache
            cacheManager.remove(CacheManager.CacheKeys.FINANCIAL_REPORTS)
            cacheManager.remove("${CacheManager.CacheKeys.FINANCIAL_REPORT_DETAIL}${report.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReport(id: String): Result<Unit> {
        return try {
            collection.document(id).delete().await()
            // Invalidate cache
            cacheManager.remove(CacheManager.CacheKeys.FINANCIAL_REPORTS)
            cacheManager.remove("${CacheManager.CacheKeys.FINANCIAL_REPORT_DETAIL}$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReportStatus(id: String, status: ReportStatus): Result<Unit> {
        return try {
            collection.document(id).update(
                mapOf(
                    "status" to status,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTotalBudget(): Double {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.toObject(FinancialReport::class.java) }
                .sumOf { it.totalBudget }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getTotalExpenses(): Double {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.toObject(FinancialReport::class.java) }
                .sumOf { it.totalExpenses }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getTotalSolicitations(): Double {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.toObject(FinancialReport::class.java) }
                .sumOf { it.totalSolicitations }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getRemainingBudget(): Double {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.toObject(FinancialReport::class.java) }
                .sumOf { it.remainingBudget }
        } catch (e: Exception) {
            0.0
        }
    }
} 