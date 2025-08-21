package com.qppd.pesapp.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "events")
@Serializable
data class Event(
    @PrimaryKey
    val id: String,
    val schoolId: String,
    val title: String,
    val description: String,
    val location: String?,
    val startDate: Long,
    val endDate: Long,
    val organizedById: String,
    val maxAttendees: Int?,
    val requiresRSVP: Boolean,
    val attachments: List<Attachment>,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "event_attendees")
@Serializable
data class EventAttendee(
    @PrimaryKey
    val id: String,
    val eventId: String,
    val userId: String,
    val status: AttendanceStatus,
    val createdAt: Long,
    val updatedAt: Long
)

enum class AttendanceStatus {
    PENDING,
    CONFIRMED,
    DECLINED,
    WAITLISTED
}
