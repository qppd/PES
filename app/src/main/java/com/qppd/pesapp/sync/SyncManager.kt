package com.qppd.pesapp.sync

import android.content.Context
import androidx.work.*
import com.qppd.pesapp.data.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedulePeriodicSync(schoolId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = java.util.concurrent.TimeUnit.MINUTES,
            flexTimeInterval = 5,
            flexTimeIntervalUnit = java.util.concurrent.TimeUnit.MINUTES
        )
            .setInputData(SyncWorker.createInputData(schoolId))
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    fun triggerImmediateSync(schoolId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(SyncWorker.createInputData(schoolId))
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(context).enqueueUniqueWork(
            "immediate_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
    
    fun cancelPeriodicSync() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(SyncWorker.WORK_NAME)
    }
}
