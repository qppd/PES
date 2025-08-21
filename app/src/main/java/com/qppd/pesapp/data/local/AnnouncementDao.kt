package com.qppd.pesapp.data.local

import androidx.room.*
import com.qppd.pesapp.domain.model.Announcement
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements WHERE schoolId = :schoolId ORDER BY createdAt DESC")
    fun observeAnnouncementsBySchool(schoolId: String): Flow<List<Announcement>>

    @Query("SELECT * FROM announcements WHERE id = :announcementId")
    fun observeAnnouncementById(announcementId: String): Flow<Announcement?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<Announcement>)

    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)

    @Query("DELETE FROM announcements WHERE schoolId = :schoolId")
    suspend fun deleteAnnouncementsBySchool(schoolId: String)
}
