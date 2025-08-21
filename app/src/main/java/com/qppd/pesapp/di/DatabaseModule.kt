package com.qppd.pesapp.di

import android.content.Context
import androidx.room.Room
import com.qppd.pesapp.data.local.PESDatabase
import com.qppd.pesapp.data.local.SchoolDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PESDatabase = Room.databaseBuilder(
        context,
        PESDatabase::class.java,
        PESDatabase.DATABASE_NAME
    ).build()
    
    @Provides
    fun provideSchoolDao(database: PESDatabase): SchoolDao = database.schoolDao()
}
