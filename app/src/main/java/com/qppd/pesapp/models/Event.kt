package com.qppd.pesapp.models

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val location: String = "",
    val category: EventCategory = EventCategory.GENERAL,
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val authorId: String = "",
    val authorName: String = "",
    val attendees: List<String> = emptyList(), // List of user IDs
    val maxAttendees: Int? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)