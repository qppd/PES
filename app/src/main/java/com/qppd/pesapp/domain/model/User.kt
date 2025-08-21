package com.qppd.pesapp.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String,
    val schoolId: String,
    val role: UserRole,
    val profilePicture: String?,
    val createdAt: Long,
    val updatedAt: Long
)

enum class UserRole {
    GUEST,
    PARENT,
    TEACHER,
    ADMIN
}
