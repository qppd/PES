package com.qppd.pesapp.data.repository

import com.qppd.pesapp.data.local.AnnouncementDao
import com.qppd.pesapp.data.remote.SupabaseConfig
import com.qppd.pesapp.domain.model.Announcement
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnnouncementRepository @Inject constructor(
    private val announcementDao: AnnouncementDao
) {
    fun observeAnnouncementsBySchool(schoolId: String): Flow<List<Announcement>> = 
        announcementDao.observeAnnouncementsBySchool(schoolId)
    
    fun observeAnnouncementById(id: String): Flow<Announcement?> = 
        announcementDao.observeAnnouncementById(id)
    
    suspend fun syncAnnouncements(schoolId: String) {
        try {
            val remoteAnnouncements = SupabaseConfig.client.postgrest["announcements"]
                .select { eq("school_id", schoolId) }
                .decodeList<Announcement>()
            
            announcementDao.insertAnnouncements(remoteAnnouncements)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun createAnnouncement(announcement: Announcement) {
        try {
            // Upload attachments first
            announcement.attachments.forEach { attachment ->
                SupabaseConfig.client.storage["attachments"].upload(
                    bucket = "announcements",
                    path = "${announcement.schoolId}/${announcement.id}/${attachment.name}",
                    data = // Handle file upload
                )
            }
            
            // Then create announcement
            SupabaseConfig.client.postgrest["announcements"]
                .insert(announcement)
            announcementDao.insertAnnouncement(announcement)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun updateAnnouncement(announcement: Announcement) {
        try {
            SupabaseConfig.client.postgrest["announcements"]
                .update {
                    set("title", announcement.title)
                    set("content", announcement.content)
                    set("attachments", announcement.attachments)
                    set("published_at", announcement.publishedAt)
                    set("updated_at", announcement.updatedAt)
                }
                .eq("id", announcement.id)
                .execute()
            announcementDao.insertAnnouncement(announcement)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
