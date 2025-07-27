package com.qppd.pesapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.qppd.pesapp.models.User
import com.qppd.pesapp.models.UserRole
import com.qppd.pesapp.cache.CacheManager
import kotlinx.coroutines.tasks.await

class UserManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val cacheManager = CacheManager.getInstance()
    
    companion object {
        @Volatile
        private var INSTANCE: UserManager? = null
        
        fun getInstance(): UserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserManager().also { INSTANCE = it }
            }
        }
    }
    
    // Get current user data from Firestore with caching
    suspend fun getCurrentUserData(): User? {
        val currentUser = auth.currentUser ?: return null
        val cacheKey = "${CacheManager.CacheKeys.USER_PROFILE}${currentUser.uid}"
        
        return cacheManager.getWithBackgroundRefresh(
            key = cacheKey,
            fetchFromNetwork = {
                try {
                    val document = firestore.collection("users").document(currentUser.uid).get().await()
                    document.toObject(User::class.java)
                } catch (e: Exception) {
                    null
                }
            },
            expirationDuration = CacheManager.CacheDurations.USER_PROFILE
        )
    }
    
    // Update user profile
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        
        return try {
            firestore.collection("users").document(currentUser.uid)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Create new user (admin only)
    suspend fun createUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get all users (admin only) with caching
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val users = cacheManager.getWithBackgroundRefresh(
                key = CacheManager.CacheKeys.USERS,
                fetchFromNetwork = {
                    val snapshot = firestore.collection("users").get().await()
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)
                    }
                },
                expirationDuration = CacheManager.CacheDurations.USERS
            )
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete user (admin only)
    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update user role (admin only)
    suspend fun updateUserRole(uid: String, newRole: UserRole): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update("role", newRole).await()
            // Invalidate related cache
            cacheManager.remove("${CacheManager.CacheKeys.USER_PROFILE}$uid")
            cacheManager.remove(CacheManager.CacheKeys.USERS)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Invalidate user cache
    fun invalidateUserCache(uid: String? = null) {
        if (uid != null) {
            cacheManager.remove("${CacheManager.CacheKeys.USER_PROFILE}$uid")
        } else {
            cacheManager.remove(CacheManager.CacheKeys.USERS)
            cacheManager.clearByPattern(CacheManager.CacheKeys.USER_PROFILE)
        }
    }
    
    // Force refresh user data
    suspend fun forceRefreshUserData(uid: String? = null): User? {
        if (uid != null) {
            cacheManager.remove("${CacheManager.CacheKeys.USER_PROFILE}$uid")
            return getCurrentUserData()
        } else {
            cacheManager.remove(CacheManager.CacheKeys.USERS)
            return getCurrentUserData()
        }
    }
} 