package com.qppd.pesapp.data.local

import androidx.room.TypeConverter
import com.qppd.pesapp.domain.model.SchoolTheme
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromSchoolTheme(theme: SchoolTheme): String {
        return Json.encodeToString(theme)
    }

    @TypeConverter
    fun toSchoolTheme(themeString: String): SchoolTheme {
        return Json.decodeFromString(themeString)
    }

    @TypeConverter
    fun fromAttachments(attachments: List<Attachment>): String {
        return Json.encodeToString(attachments)
    }

    @TypeConverter
    fun toAttachments(attachmentsString: String): List<Attachment> {
        return Json.decodeFromString(attachmentsString)
    }

    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }

    @TypeConverter
    fun toUserRole(role: String): UserRole {
        return UserRole.valueOf(role)
    }

    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String {
        return status.name
    }

    @TypeConverter
    fun toAttendanceStatus(status: String): AttendanceStatus {
        return AttendanceStatus.valueOf(status)
    }

    @TypeConverter
    fun fromAttachmentType(type: AttachmentType): String {
        return type.name
    }

    @TypeConverter
    fun toAttachmentType(type: String): AttachmentType {
        return AttachmentType.valueOf(type)
    }
}
