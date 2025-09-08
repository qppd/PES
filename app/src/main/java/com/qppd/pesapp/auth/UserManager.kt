package com.qppd.pesapp.auth

import com.qppd.pesapp.models.User
import com.qppd.pesapp.models.UserRole
import com.qppd.pesapp.supabase.SupabaseManager
import com.qppd.pesapp.supabase.SupabaseUser
import com.qppd.pesapp.supabase.toAppUser
import com.qppd.pesapp.supabase.toSupabaseUser
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserManager {
    companion object {
        @Volatile
        private var INSTANCE: UserManager? = null
        
        fun getInstance(): UserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserManager().also { INSTANCE = it }
            }
        }
    }
    
    // Get current user data from Supabase with caching
    suspend fun getCurrentUserData(): User? {
        return withContext(Dispatchers.IO) {
            try {
                // Get from AuthManager for now
                AuthManager.getInstance().getCurrentUser()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Update user profile
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    val currentUser = AuthManager.getInstance().getCurrentUser()
                    if (currentUser != null) {
                        client.from("users")
                            .update(updates) {
                                filter { eq("id", currentUser.uid) }
                            }
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Create new user (admin only)
    suspend fun createUser(user: User): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    client.from("users").insert(user.toSupabaseUser())
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Get all users (admin only)
    suspend fun getAllUsers(): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val users = SupabaseManager.withClientSuspend(
                    fallback = { emptyList<User>() }
                ) { client ->
                    val supabaseUsers = client.from("users")
                        .select()
                        .decodeList<SupabaseUser>()
                    supabaseUsers.map { it.toAppUser() }
                }
                Result.success(users)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Delete user (admin only)
    suspend fun deleteUser(uid: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    client.from("users")
                        .delete {
                            filter { eq("id", uid) }
                        }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Update user role (admin only)
    suspend fun updateUserRole(uid: String, newRole: UserRole): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    client.from("users")
                        .update(mapOf("role" to newRole.name)) {
                            filter { eq("id", uid) }
                        }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Invalidate user cache
    fun invalidateUserCache(uid: String? = null) {
        // TODO: Implement cache invalidation if needed
    }
    
    // Force refresh user data
    suspend fun forceRefreshUserData(uid: String? = null): User? {
        return withContext(Dispatchers.IO) {
            try {
                getCurrentUserData()
            } catch (e: Exception) {
                null
            }
        }
    }
}