package com.qppd.pesapp.auth

import com.qppd.pesapp.models.BulkRegistrationData
import com.qppd.pesapp.models.ExcelRow
import com.qppd.pesapp.models.RegistrationStatus
import com.qppd.pesapp.models.UserRole
import com.qppd.pesapp.utils.CSVProcessor

class BulkRegistrationManager {
    private val authManager = AuthManager.getInstance()
    private val userManager = UserManager.getInstance()
    
    companion object {
        @Volatile
        private var INSTANCE: BulkRegistrationManager? = null
        
        fun getInstance(): BulkRegistrationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BulkRegistrationManager().also { INSTANCE = it }
            }
        }
    }
    
    // Process CSV data and create registration list
    suspend fun processExcelData(csvRows: List<ExcelRow>): List<BulkRegistrationData> {
        val registrationData = mutableListOf<BulkRegistrationData>()
        
        for (row in csvRows) {
            val password = CSVProcessor.generatePassword()
            val children = row.childrenNames.split(",").map { it.trim() }.filter { it.isNotBlank() }
            
            registrationData.add(
                BulkRegistrationData(
                    parentName = row.parentName,
                    parentEmail = row.parentEmail,
                    children = children,
                    generatedPassword = password
                )
            )
        }
        
        return registrationData
    }
    
    // Register users in bulk
    suspend fun registerUsers(registrationData: List<BulkRegistrationData>): List<BulkRegistrationData> {
        val results = mutableListOf<BulkRegistrationData>()
        
        for (data in registrationData) {
            try {
                // Create user account
                val userResult = authManager.createUser(
                    email = data.parentEmail,
                    password = data.generatedPassword,
                    displayName = data.parentName,
                    role = UserRole.PARENT
                )
                
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    user?.let { createdUser ->
                        // Update user with children information
                        val updates = mapOf(
                            "children" to data.children
                        )
                        userManager.updateUserProfile(updates)
                        
                        results.add(data.copy(status = RegistrationStatus.SUCCESS))
                    } ?: results.add(data.copy(
                        status = RegistrationStatus.FAILED,
                        errorMessage = "Failed to create user"
                    ))
                } else {
                    results.add(data.copy(
                        status = RegistrationStatus.FAILED,
                        errorMessage = userResult.exceptionOrNull()?.message ?: "Unknown error"
                    ))
                }
            } catch (e: Exception) {
                results.add(data.copy(
                    status = RegistrationStatus.FAILED,
                    errorMessage = e.message ?: "Exception occurred"
                ))
            }
        }
        
        return results
    }
    
    // Validate email format
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Validate registration data
    fun validateRegistrationData(data: List<BulkRegistrationData>): List<String> {
        val errors = mutableListOf<String>()
        
        for ((index, item) in data.withIndex()) {
            if (!isValidEmail(item.parentEmail)) {
                errors.add("Row ${index + 1}: Invalid email format for ${item.parentName}")
            }
        }
        
        return errors
    }
} 