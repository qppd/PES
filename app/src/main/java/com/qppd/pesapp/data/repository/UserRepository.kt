package com.qppd.pesapp.data.repository

import com.qppd.pesapp.data.local.UserDao
import com.qppd.pesapp.data.remote.SupabaseConfig
import com.qppd.pesapp.domain.model.User
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun observeUsersBySchool(schoolId: String): Flow<List<User>> = 
        userDao.observeUsersBySchool(schoolId)
    
    fun observeUserById(id: String): Flow<User?> = userDao.observeUserById(id)
    
    suspend fun syncUsers(schoolId: String) {
        try {
            val remoteUsers = SupabaseConfig.client.postgrest["users"]
                .select { eq("school_id", schoolId) }
                .decodeList<User>()
            
            userDao.insertUsers(remoteUsers)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun createUser(user: User) {
        try {
            SupabaseConfig.client.postgrest["users"]
                .insert(user)
            userDao.insertUser(user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun updateUser(user: User) {
        try {
            SupabaseConfig.client.postgrest["users"]
                .update { 
                    set("display_name", user.displayName)
                    set("profile_picture", user.profilePicture)
                    set("updated_at", user.updatedAt)
                }
                .eq("id", user.id)
                .execute()
            userDao.insertUser(user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
