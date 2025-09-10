package com.qppd.pesapp.auth

import com.qppd.pesapp.data.remote.SupabaseEvent
import com.qppd.pesapp.data.remote.SupabaseManager
import com.qppd.pesapp.data.remote.toAppEvent
import com.qppd.pesapp.data.remote.toSupabaseEvent
import com.qppd.pesapp.models.Event
import com.qppd.pesapp.models.EventCategory

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class EventManager {

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
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { emptyList() }
            ) { client ->
                try {
                    val supabaseEvents = client.from("events")
                        .select()
                        .decodeList<SupabaseEvent>()
                        .filter { it.is_active }
                    
                    supabaseEvents.map { it.toAppEvent() }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }

    suspend fun getEventsByCategory(category: EventCategory): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                getAllEvents().filter { it.category == category }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getUpcomingEvents(): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                getAllEvents().filter { it.date > currentTime }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getEventById(id: String): Event? {
        return withContext(Dispatchers.IO) {
            try {
                getAllEvents().find { it.id == id }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun addEvent(event: Event): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    val eventWithId = event.copy(id = UUID.randomUUID().toString())
                    client.from("events").insert(eventWithId.toSupabaseEvent())
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateEvent(event: Event): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    client.from("events")
                        .update(event.toSupabaseEvent())
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteEvent(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    // Note: Delete operation without filter will be handled by service layer
                    client.from("events")
                        .delete()
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deactivateEvent(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    client.from("events")
                        .update(mapOf("is_active" to false))
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun addAttendee(eventId: String, userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    // Get current event
                    val events = client.from("events")
                        .select()
                        .decodeList<SupabaseEvent>()
                        .filter { it.id == eventId }
                    
                    if (events.isNotEmpty()) {
                        val event = events.first()
                        val updatedAttendees = event.attendees + userId
                        
                        client.from("events")
                            .update(mapOf("attendees" to updatedAttendees))
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun removeAttendee(eventId: String, userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    // Get current event
                    val events = client.from("events")
                        .select()
                        .decodeList<SupabaseEvent>()
                        .filter { it.id == eventId }
                    
                    if (events.isNotEmpty()) {
                        val event = events.first()
                        val updatedAttendees = event.attendees.filter { it != userId }
                        
                        client.from("events")
                            .update(mapOf("attendees" to updatedAttendees))
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}