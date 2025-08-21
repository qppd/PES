package com.qppd.pesapp.data.repository

import com.qppd.pesapp.data.local.EventDao
import com.qppd.pesapp.data.local.EventAttendeeDao
import com.qppd.pesapp.data.remote.SupabaseConfig
import com.qppd.pesapp.domain.model.Event
import com.qppd.pesapp.domain.model.EventAttendee
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val eventAttendeeDao: EventAttendeeDao
) {
    fun observeUpcomingEvents(schoolId: String, currentTime: Long): Flow<List<Event>> = 
        eventDao.observeUpcomingEvents(schoolId, currentTime)
    
    fun observeEventById(id: String): Flow<Event?> = eventDao.observeEventById(id)
    
    fun observeEventAttendees(eventId: String): Flow<List<EventAttendee>> = 
        eventAttendeeDao.observeEventAttendees(eventId)
    
    suspend fun syncEvents(schoolId: String) {
        try {
            val remoteEvents = SupabaseConfig.client.postgrest["events"]
                .select { eq("school_id", schoolId) }
                .decodeList<Event>()
            
            eventDao.insertEvents(remoteEvents)
            
            remoteEvents.forEach { event ->
                val attendees = SupabaseConfig.client.postgrest["event_attendees"]
                    .select { eq("event_id", event.id) }
                    .decodeList<EventAttendee>()
                eventAttendeeDao.insertEventAttendees(attendees)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun createEvent(event: Event) {
        try {
            // Upload attachments first
            event.attachments.forEach { attachment ->
                SupabaseConfig.client.storage["attachments"].upload(
                    bucket = "events",
                    path = "${event.schoolId}/${event.id}/${attachment.name}",
                    data = // Handle file upload
                )
            }
            
            // Then create event
            SupabaseConfig.client.postgrest["events"]
                .insert(event)
            eventDao.insertEvent(event)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun updateEventAttendance(eventAttendee: EventAttendee) {
        try {
            SupabaseConfig.client.postgrest["event_attendees"]
                .upsert(eventAttendee)
            eventAttendeeDao.insertEventAttendee(eventAttendee)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
