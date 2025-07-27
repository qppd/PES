package com.qppd.pesapp.models

data class FinancialReport(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: ReportCategory = ReportCategory.GENERAL,
    val totalBudget: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSolicitations: Double = 0.0,
    val expenses: List<Expense> = emptyList(),
    val solicitations: List<Solicitation> = emptyList(),
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val status: ReportStatus = ReportStatus.ACTIVE,
    val authorId: String = "",
    val authorName: String = "",
    val attachments: List<String> = emptyList(), // URLs to uploaded documents
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ReportCategory {
    SOLICITATIONS,
    PROJECTS_FUND,
    EXPENSES,
    GENERAL,
    INFRASTRUCTURE,
    EVENTS,
    MAINTENANCE,
    SUPPLIES,
    UTILITIES,
    OTHER
}

enum class ReportStatus {
    ACTIVE,
    COMPLETED,
    ON_HOLD,
    CANCELLED
}

data class Expense(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val category: String = "",
    val receiptUrl: String = "",
    val approvedBy: String = "",
    val notes: String = ""
)

data class Solicitation(
    val id: String = "",
    val donorName: String = "",
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val type: SolicitationType = SolicitationType.CASH,
    val purpose: String = "",
    val contactInfo: String = "",
    val notes: String = ""
)

enum class SolicitationType {
    CASH,
    CHECK,
    IN_KIND,
    MATERIALS,
    SERVICES,
    OTHER
} 