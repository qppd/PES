package com.qppd.pesapp.auth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.qppd.pesapp.models.Announcement
import com.qppd.pesapp.cache.CacheManager
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AnnouncementManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("announcements")
    private val cacheManager = CacheManager.getInstance()

    companion object {
        @Volatile
        private var INSTANCE: AnnouncementManager? = null
        fun getInstance(): AnnouncementManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnnouncementManager().also { INSTANCE = it }
            }
        }
    }

    suspend fun getAllAnnouncements(): List<Announcement> {
        return try {
            cacheManager.getWithBackgroundRefresh(
                key = CacheManager.CacheKeys.ANNOUNCEMENTS,
                fetchFromNetwork = {
                    val snapshot = collection.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
                    snapshot.documents.mapNotNull { it.toObject(Announcement::class.java) }
                },
                expirationDuration = CacheManager.CacheDurations.ANNOUNCEMENTS
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            val newAnnouncement = announcement.copy(id = id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
            collection.document(id).set(newAnnouncement).await()
            // Invalidate cache
            cacheManager.remove(CacheManager.CacheKeys.ANNOUNCEMENTS)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            collection.document(announcement.id).set(announcement.copy(updatedAt = System.currentTimeMillis())).await()
            // Invalidate cache
            cacheManager.remove(CacheManager.CacheKeys.ANNOUNCEMENTS)
            cacheManager.remove("${CacheManager.CacheKeys.ANNOUNCEMENT_DETAIL}${announcement.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(id: String): Result<Unit> {
        return try {
            collection.document(id).delete().await()
            // Invalidate cache
            cacheManager.remove(CacheManager.CacheKeys.ANNOUNCEMENTS)
            cacheManager.remove("${CacheManager.CacheKeys.ANNOUNCEMENT_DETAIL}$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 