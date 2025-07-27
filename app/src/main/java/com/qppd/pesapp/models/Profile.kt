package com.qppd.pesapp.models

import com.qppd.pesapp.models.UserRole

data class Profile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.GUEST,
    val profileImage: String = "",
    val contactNumber: String = "",
    val children: List<String> = emptyList(),
    val bio: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val preferences: Map<String, Boolean> = emptyMap(),
    val lastUpdated: Long = System.currentTimeMillis()
)