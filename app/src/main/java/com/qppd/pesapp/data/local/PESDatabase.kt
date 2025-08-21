package com.qppd.pesapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.qppd.pesapp.domain.model.School

@Database(
    entities = [
        School::class,
        User::class,
        Announcement::class,
        Event::class,
        EventAttendee::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class PESDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao
    abstract fun userDao(): UserDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun eventDao(): EventDao
    abstract fun eventAttendeeDao(): EventAttendeeDao
    
    companion object {
        const val DATABASE_NAME = "pes_db"
    }
}
