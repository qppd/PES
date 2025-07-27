package com.qppd.pesapp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PESApplication : Application() {
    
    companion object {
        lateinit var auth: FirebaseAuth
        lateinit var firestore: FirebaseFirestore
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        //FirebaseApp.initializeApp(this)
        
        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }
} 