package com.qppd.pesapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.qppd.pesapp.data.worker.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
@HiltAndroidApp
class PESApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var authManager: AuthManager
    
    @Inject
    lateinit var syncManager: SyncManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Observe auth state and manage sync accordingly
        lifecycleScope.launch {
            authManager.getCurrentUserId()?.let { userId ->
                userRepository.observeUserById(userId)
                    .filterNotNull()
                    .collect { user ->
                        syncManager.schedulePeriodicSync(user.schoolId)
                    }
            }
        }
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}