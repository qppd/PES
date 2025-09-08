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
        
        // AuthManager removed. Use Supabase client for authentication and user management.