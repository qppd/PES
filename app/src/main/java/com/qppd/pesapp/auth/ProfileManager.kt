package com.qppd.pesapp.auth

import com.qppd.pesapp.models.Profile
import com.qppd.pesapp.models.User
import com.qppd.pesapp.data.remote.SupabaseManager
import com.qppd.pesapp.data.remote.SupabaseUser
import com.qppd.pesapp.data.remote.toAppUser
import com.qppd.pesapp.data.remote.toSupabaseUser
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileManager {
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
    
    // Get current user profile
    suspend fun getCurrentUserProfile(): Profile? {
        return withContext(Dispatchers.IO) {
            val currentUser = AuthManager.getInstance().getCurrentUser() ?: return@withContext null
            
            SupabaseManager.withClientSuspend(
                fallback = { 
                    // Create profile from current user data as fallback
                    Profile(
                        uid = currentUser.uid,
                        email = currentUser.email,
                        displayName = currentUser.displayName,
                        role = currentUser.role,
                        profileImage = currentUser.profileImage,
                        contactNumber = currentUser.contactNumber,
                        children = currentUser.children
                    )
                }
            ) { client ->
                try {
                    val supabaseUsers = client.from("users")
                        .select() {
                            filter {
                                eq("id", currentUser.uid)
                            }
                        }
                        .decodeList<SupabaseUser>()
                    
                    if (supabaseUsers.isNotEmpty()) {
                        val user = supabaseUsers.first().toAppUser()
                        Profile(
                            uid = user.uid,
                            email = user.email,
                            displayName = user.displayName,
                            role = user.role,
                            profileImage = user.profileImage,
                            contactNumber = user.contactNumber,
                            children = user.children
                        )
                    } else {
                        // Create profile from current user data if not in database
                        Profile(
                            uid = currentUser.uid,
                            email = currentUser.email,
                            displayName = currentUser.displayName,
                            role = currentUser.role,
                            profileImage = currentUser.profileImage,
                            contactNumber = currentUser.contactNumber,
                            children = currentUser.children
                        )
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
    
    // Get profile by user ID
    suspend fun getProfileByUserId(userId: String): Profile? {
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { null }
            ) { client ->
                try {
                    val supabaseUsers = client.from("users")
                        .select() {
                            filter {
                                eq("id", userId)
                            }
                        }
                        .decodeList<SupabaseUser>()
                    
                    if (supabaseUsers.isNotEmpty()) {
                        val user = supabaseUsers.first().toAppUser()
                        Profile(
                            uid = user.uid,
                            email = user.email,
                            displayName = user.displayName,
                            role = user.role,
                            profileImage = user.profileImage,
                            contactNumber = user.contactNumber,
                            children = user.children
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
    
    // Create a new profile
    suspend fun createProfile(profile: Profile): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    val user = User(
                        uid = profile.uid,
                        email = profile.email,
                        displayName = profile.displayName,
                        role = profile.role,
                        profileImage = profile.profileImage,
                        contactNumber = profile.contactNumber,
                        children = profile.children
                    )
                    client.from("users").insert(user.toSupabaseUser())
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Update profile
    suspend fun updateProfile(updates: Map<String, Any>): Result<Unit> {
        val currentUser = AuthManager.getInstance().getCurrentUser() 
            ?: return Result.failure(Exception("No user logged in"))
        
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    client.from("users")
                        .update(updates) {
                            filter { eq("id", currentUser.uid) }
                        }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Update profile image
    suspend fun updateProfileImage(imageUrl: String): Result<Unit> {
        return updateProfile(mapOf("profile_image" to imageUrl))
    }
    
    // Update user preferences
    suspend fun updatePreferences(preferences: Map<String, Boolean>): Result<Unit> {
        return updateProfile(mapOf("preferences" to preferences))
    }
}