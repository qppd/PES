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
    
    // UserManager removed. Use Supabase client for user management.