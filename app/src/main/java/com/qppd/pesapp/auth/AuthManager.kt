package com.qppd.pesapp.auth

import com.qppd.pesapp.models.User
import com.qppd.pesapp.models.UserRole
import com.qppd.pesapp.data.remote.SupabaseManager
import com.qppd.pesapp.data.remote.SupabaseUser
import com.qppd.pesapp.data.remote.toAppUser
import com.qppd.pesapp.data.remote.toSupabaseUser
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthManager private constructor() {
    private var currentUser: User? = null

    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager().also { INSTANCE = it }
            }
        }
    }

    // Get current user
    fun getCurrentUser(): User? = currentUser
    
    // Get current Supabase user session
    fun getCurrentUserSession(): UserSession? {
        return SupabaseManager.withClient(
            fallback = { null }
        ) { client ->
            client.auth.currentSessionOrNull()
        }
    }


    // Check if user is logged in
    fun isLoggedIn(): Boolean = currentUser != null

    // Sign in with email and password
    suspend fun signIn(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = SupabaseManager.withClientSuspend(
                    fallback = {
                        // Fallback to mock user for development/offline mode
                        createMockUser(email)
                    }
                ) { client ->
                    // Authenticate with Supabase
                    client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    
                    val currentSession = client.auth.currentSessionOrNull()
                    val userId = currentSession?.user?.id ?: ""
                    
                    // Get user profile from database
                    val supabaseUsers = try {
                        client.from("users")
                            .select()
                            .decodeList<SupabaseUser>()
                            .filter { it.id == userId }
                    } catch (e: Exception) {
                        emptyList()
                    }
                    
                    if (supabaseUsers.isNotEmpty()) {
                        supabaseUsers.first().toAppUser()
                    } else {
                        // Create user profile if it doesn't exist
                        val newUser = User(
                            uid = userId,
                            email = currentSession?.user?.email ?: email,
                            displayName = currentSession?.user?.userMetadata?.get("display_name")?.toString() ?: "User",
                            role = UserRole.GUEST
                        )
                        
                        // Save to database
                        client.from("users").insert(newUser.toSupabaseUser())
                        newUser
                    }
                }

                currentUser = user
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun createMockUser(email: String): User {
        val role = when {
            email.contains("admin") -> UserRole.ADMIN
            email.contains("teacher") -> UserRole.TEACHER
            email.contains("parent") -> UserRole.PARENT
            else -> UserRole.GUEST
        }

        return User(
            uid = email.substringBefore("@") + "@" + role.name,
            email = email,
            displayName = role.name.lowercase().replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase() else it.toString() 
            } + " User",
            role = role
        )
    }

    // Sign out
    suspend fun signOut() {
        SupabaseManager.withClientSuspend(
            fallback = { }
        ) { client ->
            client.auth.signOut()
        }
        currentUser = null
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    client.auth.resetPasswordForEmail(email)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Create user account (admin only)
    suspend fun createUser(email: String, password: String, displayName: String, role: UserRole): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = SupabaseManager.withClientSuspend(
                    fallback = {
                        // Fallback for development - create mock user
                        User(
                            uid = email.substringBefore("@") + "@" + role.name + "_new",
                            email = email,
                            displayName = displayName,
                            role = role
                        )
                    }
                ) { client ->
                    // Note: This requires admin privileges or a Supabase function
                    // For now, we'll create the user record in our users table
                    val newUser = User(
                        uid = java.util.UUID.randomUUID().toString(),
                        email = email,
                        displayName = displayName,
                        role = role
                    )
                    
                    // Insert into users table
                    client.from("users").insert(newUser.toSupabaseUser())
                    newUser
                }
                
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}