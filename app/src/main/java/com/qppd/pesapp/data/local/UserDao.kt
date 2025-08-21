package com.qppd.pesapp.data.local

import androidx.room.*
import com.qppd.pesapp.domain.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE schoolId = :schoolId")
    fun observeUsersBySchool(schoolId: String): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserById(userId: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users WHERE schoolId = :schoolId")
    suspend fun deleteUsersBySchool(schoolId: String)
}
