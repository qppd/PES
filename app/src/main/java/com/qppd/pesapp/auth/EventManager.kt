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

    suspend fun getEventsByCategory(category: EventCategory): List<Event> {
        return try {
            val snapshot = collection
                .whereEqualTo("category", category)
                .whereEqualTo("isActive", true)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUpcomingEvents(): List<Event> {
        return try {
            val currentTime = System.currentTimeMillis()
            val snapshot = collection
                .whereGreaterThan("date", currentTime)
                .whereEqualTo("isActive", true)
                .orderBy("date", Query.Direction.ASCENDING)
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getEventById(id: String): Event? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Event::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addEvent(event: Event): Result<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            val newEvent = event.copy(
                id = id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            collection.document(id).set(newEvent).await()
            // Invalidate cache
            cacheManager.remove(CacheManager.CacheKeys.EVENTS)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            collection.document(event.id).set(
                event.copy(updatedAt = System.currentTimeMillis())
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(id: String): Result<Unit> {
        return try {
            collection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deactivateEvent(id: String): Result<Unit> {
        return try {
            collection.document(id).update(
                mapOf(
                    "isActive" to false,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAttendee(eventId: String, userId: String): Result<Unit> {
        return try {
            val event = getEventById(eventId)
            if (event != null) {
                val currentAttendees = event.attendees.toMutableList()
                if (!currentAttendees.contains(userId)) {
                    currentAttendees.add(userId)
                    collection.document(eventId).update(
                        mapOf(
                            "attendees" to currentAttendees,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeAttendee(eventId: String, userId: String): Result<Unit> {
        return try {
            val event = getEventById(eventId)
            if (event != null) {
                val currentAttendees = event.attendees.toMutableList()
                currentAttendees.remove(userId)
                collection.document(eventId).update(
                    mapOf(
                        "attendees" to currentAttendees,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 