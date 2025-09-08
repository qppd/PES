package com.qppd.pesapp

import android.app.Application
import java.util.Properties

class PESApplication : Application() {
    lateinit var supabaseUrl: String
    lateinit var supabaseAnonKey: String

    override fun onCreate() {
        super.onCreate()
        val properties = Properties()
        try {
            assets.open("supabase.properties").use { inputStream ->
                properties.load(inputStream)
            }
            supabaseUrl = properties.getProperty("SUPABASE_URL") ?: ""
            supabaseAnonKey = properties.getProperty("SUPABASE_ANON_KEY") ?: ""
        } catch (e: Exception) {
            supabaseUrl = "https://sydibyybnowngxzcnvqo.supabase.co"
            supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN5ZGlieXlibm93bmd4emNudnFvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTY4MjgxOTYsImV4cCI6MjA3MjQwNDE5Nn0.hJ4yynMI9nRGoLILkNRLfTx3sE0MU9Qb3oroGCue6ac"
            e.printStackTrace()
        }
        // TODO: Initialize Supabase client here using supabaseUrl and supabaseAnonKey
    }
}