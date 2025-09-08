package com.qppd.pesapp.auth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.qppd.pesapp.models.Event
import com.qppd.pesapp.models.EventCategory
import com.qppd.pesapp.cache.CacheManager
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EventManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("events")
    private val cacheManager = CacheManager.getInstance()

    companion object {
        @Volatile
        private var INSTANCE: EventManager? = null
        fun getInstance(): EventManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EventManager().also { INSTANCE = it }
            }
        }
    }

    suspend fun getAllEvents(): List<Event> {
        return try {
            cacheManager.getWithBackgroundRefresh(
                key = CacheManager.CacheKeys.EVENTS,
                fetchFromNetwork = {
                    val snapshot = collection
                        .whereEqualTo("isActive", true)
                        .orderBy("date", Query.Direction.DESCENDING)
                        .get().await()
                    snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
                },
                expirationDuration = CacheManager.CacheDurations.EVENTS
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    // EventManager removed. Use Supabase client for event management.