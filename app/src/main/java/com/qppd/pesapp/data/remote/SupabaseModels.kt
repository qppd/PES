package com.qppd.pesapp.data.remote

import com.qppd.pesapp.models.User
import com.qppd.pesapp.models.UserRole
import com.qppd.pesapp.models.Event
import com.qppd.pesapp.models.EventCategory
import kotlinx.serialization.Serializable

// Supabase-specific data models that match your database schema

@Serializable
data class SupabaseUser(
    val id: String = "",
    val email: String = "",
    val display_name: String = "",
    val role: String = "GUEST",
    val contact_number: String = "",
    val profile_image: String = "",
    val children: List<String> = emptyList(),
    val created_at: String = "",
    val updated_at: String = ""
)

@Serializable
data class SupabaseEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val event_date: String = "", // ISO string format
    val end_date: String? = null,
    val location: String = "",
    val category: String = "GENERAL",
    val image_url: String = "",
    val is_active: Boolean = true,
    val author_id: String = "",
    val author_name: String = "",
    val attendees: List<String> = emptyList(),
    val max_attendees: Int? = null,
    val tags: List<String> = emptyList(),
    val created_at: String = "",
    val updated_at: String = ""
)

// Extension functions to convert between app models and Supabase models

fun SupabaseUser.toAppUser(): User {
    return User(
        uid = id,
        email = email,
        displayName = display_name,
        role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.GUEST },
        contactNumber = contact_number,
        profileImage = profile_image,
        children = children,
        createdAt = try { 
            java.time.Instant.parse(created_at).toEpochMilli() 
        } catch (e: Exception) { 
            System.currentTimeMillis() 
        }
    )
}

fun User.toSupabaseUser(): SupabaseUser {
    return SupabaseUser(
        id = uid,
        email = email,
        display_name = displayName,
        role = role.name,
        contact_number = contactNumber,
        profile_image = profileImage,
        children = children,
        created_at = java.time.Instant.ofEpochMilli(createdAt).toString(),
        updated_at = java.time.Instant.now().toString()
    )
}

fun SupabaseEvent.toAppEvent(): Event {
    return Event(
        id = id,
        title = title,
        description = description,
        date = try { 
            java.time.Instant.parse(event_date).toEpochMilli() 
        } catch (e: Exception) { 
            System.currentTimeMillis() 
        },
        endDate = end_date?.let { 
            try { 
                java.time.Instant.parse(it).toEpochMilli() 
            } catch (e: Exception) { 
                null 
            } 
        },
        location = location,
        category = try { EventCategory.valueOf(category) } catch (e: Exception) { EventCategory.GENERAL },
        imageUrl = image_url,
        isActive = is_active,
        authorId = author_id,
        authorName = author_name,
        attendees = attendees,
        maxAttendees = max_attendees,
        tags = tags,
        createdAt = try { 
            java.time.Instant.parse(created_at).toEpochMilli() 
        } catch (e: Exception) { 
            System.currentTimeMillis() 
        },
        updatedAt = try { 
            java.time.Instant.parse(updated_at).toEpochMilli() 
        } catch (e: Exception) { 
            System.currentTimeMillis() 
        }
    )
}

fun Event.toSupabaseEvent(): SupabaseEvent {
    return SupabaseEvent(
        id = id,
        title = title,
        description = description,
        event_date = java.time.Instant.ofEpochMilli(date).toString(),
        end_date = endDate?.let { java.time.Instant.ofEpochMilli(it).toString() },
        location = location,
        category = category.name,
        image_url = imageUrl,
        is_active = isActive,
        author_id = authorId,
        author_name = authorName,
        attendees = attendees,
        max_attendees = maxAttendees,
        tags = tags,
        created_at = java.time.Instant.ofEpochMilli(createdAt).toString(),
        updated_at = java.time.Instant.ofEpochMilli(updatedAt).toString()
    )
}
