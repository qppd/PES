package com.qppd.pesapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.qppd.pesapp.models.Profile
import com.qppd.pesapp.models.User
import com.qppd.pesapp.cache.CacheManager
import kotlinx.coroutines.tasks.await

class ProfileManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val cacheManager = CacheManager.getInstance()
    private val userManager = UserManager.getInstance()
    
    companion object {
        @Volatile
        private var INSTANCE: ProfileManager? = null
        
        fun getInstance(): ProfileManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProfileManager().also { INSTANCE = it }
            }
        }
    }
    
    // Get current user profile with caching
    suspend fun getCurrentUserProfile(): Profile? {
        val currentUser = auth.currentUser ?: return null
        val cacheKey = "${CacheManager.CacheKeys.USER_PROFILE}${currentUser.uid}"
        
        return cacheManager.getWithBackgroundRefresh(
            key = cacheKey,
            fetchFromNetwork = {
                try {
                    val document = firestore.collection("profiles").document(currentUser.uid).get().await()
                    if (document.exists()) {
                        document.toObject(Profile::class.java)
                    } else {
                        // If profile doesn't exist, create one from user data
                        val userData = userManager.getCurrentUserData()
                        userData?.let {
                            val newProfile = Profile(
                                uid = it.uid,
                                email = it.email,
                                displayName = it.displayName,
                                role = it.role,
                                profileImage = it.profileImage,
                                contactNumber = it.contactNumber,
                                children = it.children
                            )
                            // Save the new profile
                            createProfile(newProfile)
                            newProfile
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            },
            expirationDuration = CacheManager.CacheDurations.USER_PROFILE
        )
    }
    
    // Get profile by user ID
    suspend fun getProfileByUserId(userId: String): Profile? {
        val cacheKey = "${CacheManager.CacheKeys.USER_PROFILE}${userId}"
        
        return cacheManager.getWithBackgroundRefresh(
            key = cacheKey,
            fetchFromNetwork = {
                try {
                    val document = firestore.collection("profiles").document(userId).get().await()
                    document.toObject(Profile::class.java)
                } catch (e: Exception) {
                    null
                }
            },
            expirationDuration = CacheManager.CacheDurations.USER_PROFILE
        )
    }
    
    // Create a new profile
    suspend fun createProfile(profile: Profile): Result<Unit> {
        return try {
            firestore.collection("profiles").document(profile.uid).set(profile).await()
            // Invalidate cache
            cacheManager.remove("${CacheManager.CacheKeys.USER_PROFILE}${profile.uid}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update profile
    suspend fun updateProfile(updates: Map<String, Any>): Result<Unit> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        
        return try {
            firestore.collection("profiles").document(currentUser.uid)
                .update(updates).await()
            // Invalidate cache
            cacheManager.remove("${CacheManager.CacheKeys.USER_PROFILE}${currentUser.uid}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update profile image
    suspend fun updateProfileImage(imageUrl: String): Result<Unit> {
        return updateProfile(mapOf("profileImage" to imageUrl))
    }
    
    // Update user preferences
    suspend fun updatePreferences(preferences: Map<String, Boolean>): Result<Unit> {
        return updateProfile(mapOf("preferences" to preferences))
    }
}