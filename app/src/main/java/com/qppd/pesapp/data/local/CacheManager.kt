package com.qppd.pesapp.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.hours

class CacheManager {
    private val cache = ConcurrentHashMap<String, CacheEntry<*>>()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        @Volatile
        private var INSTANCE: CacheManager? = null
        
        fun getInstance(): CacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CacheManager().also { INSTANCE = it }
            }
        }
    }
    
    // Cache entry with expiration
    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
        val expirationDuration: Duration = 30.minutes,
        val markedAsStale: Boolean = false
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > expirationDuration.inWholeMilliseconds
        }
        
        fun isStale(): Boolean {
            return markedAsStale || System.currentTimeMillis() - timestamp > (expirationDuration.inWholeMilliseconds * 0.7)
        }
    }
    
    // Cache keys
    object CacheKeys {
        const val USERS = "users"
        const val ANNOUNCEMENTS = "announcements"
        const val FINANCIAL_REPORTS = "financial_reports"
        const val EVENTS = "events"
        const val USER_PROFILE = "user_profile_"
        const val ANNOUNCEMENT_DETAIL = "announcement_detail_"
        const val FINANCIAL_REPORT_DETAIL = "financial_report_detail_"
        const val EVENT_DETAIL = "event_detail_"
    }
    
    // Default cache durations
    object CacheDurations {
        val USERS = 1.hours
        val ANNOUNCEMENTS = 15.minutes
        val FINANCIAL_REPORTS = 30.minutes
        val EVENTS = 10.minutes
        val USER_PROFILE = 2.hours
        val DETAILS = 1.hours
    }
    
    /**
     * Get data from cache
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val entry = cache[key] as? CacheEntry<T> ?: return null
        
        if (entry.isExpired()) {
            cache.remove(key)
            return null
        }
        
        return entry.data
    }
    
    /**
     * Put data in cache
     */
    fun <T> put(key: String, data: T, expirationDuration: Duration = CacheDurations.USERS) {
        cache[key] = CacheEntry(data, expirationDuration = expirationDuration)
    }
    
    /**
     * Remove specific key from cache
     */
    fun remove(key: String) {
        cache.remove(key)
    }
    
    /**
     * Clear all cache
     */
    fun clear() {
        cache.clear()
    }
    
    /**
     * Clear cache by pattern (e.g., all user-related cache)
     */
    fun clearByPattern(pattern: String) {
        cache.keys.removeIf { it.contains(pattern) }
    }
    
    /**
     * Check if cache has valid data
     */
    fun hasValidCache(key: String): Boolean {
        val entry = cache[key] ?: return false
        return !entry.isExpired()
    }
    
    /**
     * Check if cache is stale (needs refresh)
     */
    fun isStale(key: String): Boolean {
        val entry = cache[key] ?: return true
        return entry.isStale()
    }
    
    /**
     * Mark cache as stale (for background refresh)
     */
    fun markAsStale(key: String) {
        val entry = cache[key] ?: return
        cache[key] = entry.copy(markedAsStale = true)
    }
    
    /**
     * Smart cache with background refresh
     */
    suspend fun <T> getWithBackgroundRefresh(
        key: String,
        fetchFromNetwork: suspend () -> T,
        expirationDuration: Duration = CacheDurations.USERS,
        forceRefresh: Boolean = false
    ): T {
        // If force refresh or no cache, fetch from network
        if (forceRefresh || !hasValidCache(key)) {
            val data = fetchFromNetwork()
            put(key, data, expirationDuration)
            return data
        }
        
        // Get from cache
        val cachedData = get<T>(key)
        if (cachedData != null) {
            // If cache is stale, refresh in background
            if (isStale(key)) {
                scope.launch {
                    try {
                        val freshData = fetchFromNetwork()
                        put(key, freshData, expirationDuration)
                    } catch (e: Exception) {
                        // Keep stale data if refresh fails
                        markAsStale(key)
                    }
                }
            }
            return cachedData
        }
        
        // Fallback to network fetch
        val data = fetchFromNetwork()
        put(key, data, expirationDuration)
        return data
    }
    
    /**
     * Batch cache operations
     */
    fun <T> putBatch(entries: Map<String, T>, expirationDuration: Duration = CacheDurations.USERS) {
        entries.forEach { (key, data) ->
            put(key, data, expirationDuration)
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        val totalEntries = cache.size
        val expiredEntries = cache.values.count { it.isExpired() }
        val staleEntries = cache.values.count { it.isStale() }
        
        return CacheStats(
            totalEntries = totalEntries,
            expiredEntries = expiredEntries,
            staleEntries = staleEntries,
            validEntries = totalEntries - expiredEntries
        )
    }
    
    /**
     * Clean expired entries
     */
    fun cleanExpiredEntries() {
        cache.entries.removeIf { it.value.isExpired() }
    }
    
    data class CacheStats(
        val totalEntries: Int,
        val expiredEntries: Int,
        val staleEntries: Int,
        val validEntries: Int
    )
} 