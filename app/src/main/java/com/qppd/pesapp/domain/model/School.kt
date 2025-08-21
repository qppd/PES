package com.qppd.pesapp.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "schools")
@Serializable
data class School(
    @PrimaryKey
    val id: String,
    val name: String,
    val code: String,
    val address: String,
    val contactNumber: String?,
    val email: String?,
    val logo: String?, // URL to logo in Supabase Storage
    val theme: SchoolTheme,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class SchoolTheme(
    val primaryColor: String,
    val secondaryColor: String,
    val tertiaryColor: String,
    val neutralColor: String,
    val errorColor: String,
    val cornerRadius: Int = 16 // Default rounded-2xl
)
