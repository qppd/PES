package com.qppd.pesapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.qppd.pesapp.models.User
import com.qppd.pesapp.models.UserRole
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
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
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    // Check if user is logged in
    fun isLoggedIn(): Boolean = auth.currentUser != null
    
    // Sign in with email and password
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                // Get user data from Firestore
                val userData = getUserData(firebaseUser.uid)
                userData?.let {
                    // Update last login
                    updateLastLogin(firebaseUser.uid)
                    Result.success(it)
                } ?: Result.failure(Exception("User data not found"))
            } ?: Result.failure(Exception("Authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Sign out
    fun signOut() {
        auth.signOut()
    }
    
    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Create user account (admin only)
    suspend fun createUser(email: String, password: String, displayName: String, role: UserRole): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val user = User(
                    uid = firebaseUser.uid,
                    email = email,
                    displayName = displayName,
                    role = role
                )
                
                // Save user data to Firestore
                saveUserData(user)
                Result.success(user)
            } ?: Result.failure(Exception("User creation failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get user data from Firestore
    private suspend fun getUserData(uid: String): User? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    // Save user data to Firestore
    private suspend fun saveUserData(user: User) {
        try {
            firestore.collection("users").document(user.uid).set(user).await()
        } catch (e: Exception) {
            // Handle error
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