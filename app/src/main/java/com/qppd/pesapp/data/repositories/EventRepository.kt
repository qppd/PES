package com.qppd.pesapp.data.repositories

import com.qppd.pesapp.models.Event
import com.qppd.pesapp.models.EventCategory
import com.qppd.pesapp.data.remote.SupabaseManager
import com.qppd.pesapp.data.remote.SupabaseEvent
import com.qppd.pesapp.data.remote.toAppEvent
import com.qppd.pesapp.data.remote.toSupabaseEvent
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class EventRepository {

    companion object {
        @Volatile
        private var INSTANCE: EventRepository? = null
        fun getInstance(): EventRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EventRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun getAllEvents(): List<Event> {
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { getSampleEvents() }
            ) { client ->
                try {
                    val supabaseEvents = client.from("events")
                        .select()
                        .decodeList<SupabaseEvent>()
                        .filter { it.is_active }
                    
                    if (supabaseEvents.isEmpty()) {
                        getSampleEvents()
                    } else {
                        supabaseEvents.map { it.toAppEvent() }
                    }
                } catch (e: Exception) {
                    getSampleEvents()
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
    
    private fun getSampleEvents(): List<Event> {
        return listOf(
            Event(
                id = "1",
                title = "Coco Lilay Festival 2025",
                description = "Annual school festival with cultural performances and food stalls. Students showcase traditional dances and local products.",
                date = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L),
                location = "School Grounds",
                category = EventCategory.CULTURAL,
                authorName = "School Admin",
                tags = listOf("festival", "cultural", "celebration")
            ),
            Event(
                id = "2",
                title = "Rape Prevention Lecture",
                description = "PMSg Mary Ann A Limbo conducted a lecture about rape prevention tips and other gender-based cases among the teachers and students.",
                date = System.currentTimeMillis() + (15 * 24 * 60 * 60 * 1000L),
                location = "School Auditorium",
                category = EventCategory.WORKSHOP,
                authorName = "School Admin",
                tags = listOf("safety", "education", "awareness")
            ),
            Event(
                id = "3",
                title = "Parent-Teacher Meeting",
                description = "Quarterly meeting to discuss student progress and school activities.",
                date = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L),
                location = "Classrooms",
                category = EventCategory.MEETING,
                authorName = "School Admin",
                tags = listOf("meeting", "parent", "teacher")
            )
        )
    }
}