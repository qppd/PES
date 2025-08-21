package com.qppd.pesapp.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.qppd.pesapp.data.repository.SchoolRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val schoolRepository: SchoolRepository,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository,
    private val eventRepository: EventRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val schoolId = inputData.getString(KEY_SCHOOL_ID)
                ?: return Result.failure()
                
            // Sync all data for the school
            schoolRepository.syncSchools()
            userRepository.syncUsers(schoolId)
            announcementRepository.syncAnnouncements(schoolId)
            eventRepository.syncEvents(schoolId)
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "sync_worker"
        const val KEY_SCHOOL_ID = "school_id"
        
        fun createInputData(schoolId: String): Data {
            return Data.Builder()
                .putString(KEY_SCHOOL_ID, schoolId)
                .build()
        }
    }
}
