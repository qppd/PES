package com.qppd.pesapp.auth

import com.qppd.pesapp.data.remote.SupabaseConfig
import com.qppd.pesapp.data.repository.UserRepository
import com.qppd.pesapp.domain.model.User
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val userRepository: UserRepository
) {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        _isLoggedIn.value = SupabaseConfig.client.auth.currentUser != null
    }

    // Sign in with email and password
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            SupabaseConfig.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            _isLoggedIn.value = true
            
            // Get user data
            val userId = getCurrentUserId()
            userId?.let { id ->
                userRepository.observeUserById(id).collect { user ->
                    user?.let {
                        return Result.success(it)
                    } ?: return Result.failure(Exception("User data not found"))
                }
            } ?: return Result.failure(Exception("Authentication failed"))
            
            Result.failure(Exception("Authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Sign out
    suspend fun signOut() {
        SupabaseConfig.client.auth.signOut()
        _isLoggedIn.value = false
    }
    
    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            SupabaseConfig.client.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Create user account (admin only)
    suspend fun createUser(email: String, password: String, displayName: String, role: UserRole, schoolId: String): Result<User> {
        return try {
            val response = SupabaseConfig.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            response.user?.let { supabaseUser ->
                val user = User(
                    id = supabaseUser.id,
                    email = email,
                    displayName = displayName,
                    role = role,
                    schoolId = schoolId,
                    profilePicture = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                userRepository.createUser(user)
                Result.success(user)
            } ?: Result.failure(Exception("User creation failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUserId(): String? {
        return SupabaseConfig.client.auth.currentUser?.id
    }
        }
    }
    
    // Update last login time
    private suspend fun updateLastLogin(uid: String) {
        try {
            firestore.collection("users").document(uid)
                .update("lastLogin", System.currentTimeMillis()).await()
        } catch (e: Exception) {
            // Handle error
        }
    }
} 