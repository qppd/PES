package com.qppd.pesapp.data.repository

import com.qppd.pesapp.data.local.SchoolDao
import com.qppd.pesapp.data.remote.SupabaseConfig
import com.qppd.pesapp.domain.model.School
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SchoolRepository @Inject constructor(
    private val schoolDao: SchoolDao
) {
    // Observe schools from local database (offline-first)
    fun observeAllSchools(): Flow<List<School>> = schoolDao.observeAllSchools()
    
    fun observeSchoolById(id: String): Flow<School?> = schoolDao.observeSchoolById(id)
    
    // Sync with remote
    suspend fun syncSchools() {
        try {
            val remoteSchools = SupabaseConfig.client.postgrest["schools"]
                .select()
                .decodeList<School>()
            
            schoolDao.insertSchools(remoteSchools)
        } catch (e: Exception) {
            // Handle error (log, notify UI, etc)
            e.printStackTrace()
        }
    }
    
    suspend fun createSchool(school: School) {
        try {
            // Insert to remote first
            SupabaseConfig.client.postgrest["schools"]
                .insert(school)
                
            // Then cache locally
            schoolDao.insertSchool(school)
        } catch (e: Exception) {
            // Handle error
            e.printStackTrace()
        }
    }
    
    suspend fun updateSchool(school: School) {
        try {
            // Update remote first
            SupabaseConfig.client.postgrest["schools"]
                .update({ set("name", school.name) })
                .eq("id", school.id)
                .execute()
                
            // Then update local cache
            schoolDao.insertSchool(school)
        } catch (e: Exception) {
            // Handle error
            e.printStackTrace()
        }
    }
}
