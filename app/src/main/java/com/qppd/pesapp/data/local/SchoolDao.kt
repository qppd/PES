package com.qppd.pesapp.data.local

import androidx.room.*
import com.qppd.pesapp.domain.model.School
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    @Query("SELECT * FROM schools")
    fun observeAllSchools(): Flow<List<School>>

    @Query("SELECT * FROM schools WHERE id = :schoolId")
    fun observeSchoolById(schoolId: String): Flow<School?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(school: School)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchools(schools: List<School>)

    @Delete
    suspend fun deleteSchool(school: School)

    @Query("DELETE FROM schools")
    suspend fun deleteAllSchools()
}
