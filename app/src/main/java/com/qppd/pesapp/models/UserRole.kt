package com.qppd.pesapp.models

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    ADMIN,
    TEACHER,
    PARENT,
    GUEST
}
