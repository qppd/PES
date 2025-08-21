package com.qppd.pesapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.qppd.pesapp.data.local.*
import com.qppd.pesapp.data.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideSchoolRepository(
        schoolDao: SchoolDao
    ): SchoolRepository = SchoolRepository(schoolDao)
    
    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao
    ): UserRepository = UserRepository(userDao)
    
    @Provides
    @Singleton
    fun provideAnnouncementRepository(
        announcementDao: AnnouncementDao
    ): AnnouncementRepository = AnnouncementRepository(announcementDao)
    
    @Provides
    @Singleton
    fun provideEventRepository(
        eventDao: EventDao,
        eventAttendeeDao: EventAttendeeDao
    ): EventRepository = EventRepository(eventDao, eventAttendeeDao)
}
