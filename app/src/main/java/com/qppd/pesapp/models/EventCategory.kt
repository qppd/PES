package com.qppd.pesapp.models

import kotlinx.serialization.Serializable

@Serializable
enum class EventCategory {
    GENERAL,
    ACADEMIC,
    CULTURAL,
    SPORTS,
    WORKSHOP,
    MEETING,
    HOLIDAY,
    OTHER
}
