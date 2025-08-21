package com.qppd.pesapp.data.local

import androidx.room.*
import com.qppd.pesapp.domain.model.Event
import com.qppd.pesapp.domain.model.EventAttendee
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE schoolId = :schoolId AND endDate >= :currentTime ORDER BY startDate ASC")
    fun observeUpcomingEvents(schoolId: String, currentTime: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun observeEventById(eventId: String): Flow<Event?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events WHERE schoolId = :schoolId")
    suspend fun deleteEventsBySchool(schoolId: String)
}

@Dao
interface EventAttendeeDao {
    @Query("SELECT * FROM event_attendees WHERE eventId = :eventId")
    fun observeEventAttendees(eventId: String): Flow<List<EventAttendee>>

    @Query("SELECT * FROM event_attendees WHERE userId = :userId AND eventId = :eventId")
    fun observeAttendeeStatus(userId: String, eventId: String): Flow<EventAttendee?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventAttendee(eventAttendee: EventAttendee)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventAttendees(eventAttendees: List<EventAttendee>)

    @Delete
    suspend fun deleteEventAttendee(eventAttendee: EventAttendee)

    @Query("DELETE FROM event_attendees WHERE eventId = :eventId")
    suspend fun deleteEventAttendees(eventId: String)
}
