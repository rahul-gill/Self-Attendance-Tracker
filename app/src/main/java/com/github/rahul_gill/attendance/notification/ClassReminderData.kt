package com.github.rahul_gill.attendance.notification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class ClassReminderData(
    val scheduleId: Long,
    val courseId: Long,
    val courseName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val date: LocalDate
) : Parcelable {
    companion object {
        const val EXTRA_KEY = "extra_class_reminder_data"
    }
}
