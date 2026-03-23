package com.github.rahul_gill.attendance.notification

import android.os.Parcelable
import com.github.rahul_gill.attendance.db.CourseClassStatus
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class NotificationActionData(
    val notificationId: Int,
    val scheduleId: Long,
    val courseId: Long,
    val date: LocalDate,
    val status: CourseClassStatus
) : Parcelable {
    companion object {
        const val EXTRA_KEY = "extra_notification_action_data"
    }
}
