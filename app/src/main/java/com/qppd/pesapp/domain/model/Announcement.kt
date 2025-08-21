package com.qppd.pesapp.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "announcements")
@Serializable
data class Announcement(
    @PrimaryKey
    val id: String,
    val schoolId: String,
    val title: String,
    val content: String,
    val authorId: String,
    val attachments: List<Attachment>,
    val publishedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class Attachment(
    val id: String,
    val type: AttachmentType,
    val url: String,
    val name: String,
    val size: Long
)

enum class AttachmentType {
    IMAGE,
    DOCUMENT,
    VIDEO
}
