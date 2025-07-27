package com.qppd.pesapp.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.GUEST,
    val profileImage: String = "",
    val contactNumber: String = "",
    val children: List<String> = emptyList(), // for parents
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)

