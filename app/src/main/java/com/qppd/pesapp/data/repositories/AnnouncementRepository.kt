package com.qppd.pesapp.data.repositories

import com.qppd.pesapp.models.Announcement
import com.qppd.pesapp.data.remote.SupabaseManager
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SupabaseAnnouncement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val author_id: String = "",
    val author_name: String = "",
    val category: String = "GENERAL",
    val priority: String = "MEDIUM",
    val is_active: Boolean = true,
    val target_roles: List<String> = listOf("PARENT", "TEACHER", "ADMIN"),
    val created_at: String = "",
    val updated_at: String = ""
)

fun SupabaseAnnouncement.toAppAnnouncement(): Announcement {
    return Announcement(
        id = id,
        title = title,
        content = content,
        authorId = author_id,
        authorName = author_name,
        category = category,
        priority = priority,
        isActive = is_active,
        targetRoles = target_roles,
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

fun Announcement.toSupabaseAnnouncement(): SupabaseAnnouncement {
    return SupabaseAnnouncement(
        id = id,
        title = title,
        content = content,
        author_id = authorId,
        author_name = authorName,
        category = category,
        priority = priority,
        is_active = isActive,
        target_roles = targetRoles,
        created_at = java.time.Instant.ofEpochMilli(createdAt).toString(),
        updated_at = java.time.Instant.ofEpochMilli(updatedAt).toString()
    )
}

class AnnouncementRepository {

    companion object {
        @Volatile
        private var INSTANCE: AnnouncementRepository? = null
        fun getInstance(): AnnouncementRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnnouncementRepository().also { INSTANCE = it }
            }
        }
    }

    suspend fun getAllAnnouncements(): List<Announcement> {
        return withContext(Dispatchers.IO) {
            SupabaseManager.withClientSuspend(
                fallback = { emptyList() }
            ) { client ->
                try {
                    val supabaseAnnouncements = client.from("announcements")
                        .select()
                        .decodeList<SupabaseAnnouncement>()
                        .filter { it.is_active }
                    
                    supabaseAnnouncements.map { it.toAppAnnouncement() }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }

    suspend fun addAnnouncement(announcement: Announcement): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    val id = UUID.randomUUID().toString()
                    val newAnnouncement = announcement.copy(
                        id = id, 
                        createdAt = System.currentTimeMillis(), 
                        updatedAt = System.currentTimeMillis()
                    )
                    client.from("announcements").insert(newAnnouncement.toSupabaseAnnouncement())
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateAnnouncement(announcement: Announcement): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    val updatedAnnouncement = announcement.copy(updatedAt = System.currentTimeMillis())
                    client.from("announcements")
                        .update(updatedAnnouncement.toSupabaseAnnouncement()) {
                            filter { eq("id", announcement.id) }
                        }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteAnnouncement(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseManager.withClientSuspend(
                    fallback = { }
                ) { client ->
                    client.from("announcements")
                        .delete {
                            filter { eq("id", id) }
                        }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 