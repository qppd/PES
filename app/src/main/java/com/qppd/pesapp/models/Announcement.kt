package com.qppd.pesapp.models

import java.util.Date

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val imageUrl: String = "",
    val isPinned: Boolean = false,
    val category: String = "GENERAL",
    val priority: String = "MEDIUM",
    val isActive: Boolean = true,
    val targetRoles: List<String> = listOf("PARENT", "TEACHER", "ADMIN"),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val scheduledDate: Long? = null
) 