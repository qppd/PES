package com.qppd.pesapp.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.runBlocking

object SupabaseManager {
    private var _client: SupabaseClient? = null
    
    fun initialize(url: String, anonKey: String) {
        if (_client == null && url.isNotBlank() && anonKey.isNotBlank()) {
            try {
                _client = createSupabaseClient(
                    supabaseUrl = url,
                    supabaseKey = anonKey
                ) {
                    install(Postgrest)
                    install(Auth)
                    install(Storage)
                }
            } catch (e: Exception) {
                // Log error but don't crash - app will use fallback data
                e.printStackTrace()
            }
        }
    }
    
    val client: SupabaseClient?
        get() = _client
        
    val isInitialized: Boolean
        get() = _client != null
        
    // Safe client access with fallback
    fun <T> withClient(fallback: () -> T, action: (SupabaseClient) -> T): T {
        return if (isInitialized) {
            try {
                action(_client!!)
            } catch (e: Exception) {
                e.printStackTrace()
                fallback()
            }
        } else {
            fallback()
        }
    }
    
    // Async safe client access
    suspend fun <T> withClientSuspend(
        fallback: suspend () -> T, 
        action: suspend (SupabaseClient) -> T
    ): T {
        return if (isInitialized) {
            try {
                action(_client!!)
            } catch (e: Exception) {
                e.printStackTrace()
                fallback()
            }
        } else {
            fallback()
        }
    }
}
