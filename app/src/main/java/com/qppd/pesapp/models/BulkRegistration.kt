package com.qppd.pesapp.models

data class BulkRegistrationData(
    val parentName: String,
    val parentEmail: String,
    val children: List<String>,
    val generatedPassword: String = "",
    val status: RegistrationStatus = RegistrationStatus.PENDING,
    val errorMessage: String = ""
)

enum class RegistrationStatus {
    PENDING,
    SUCCESS,
    FAILED
}

data class ExcelRow(
    val parentName: String,
    val parentEmail: String,
    val childrenNames: String // Comma-separated children names
) 