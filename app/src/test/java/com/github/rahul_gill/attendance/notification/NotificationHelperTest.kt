package com.github.rahul_gill.attendance.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.CourseClassStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager
import java.time.LocalDate
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S], application = TestAttendanceApp::class)
class NotificationHelperTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var shadowNotificationManager: ShadowNotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = shadowOf(notificationManager)
    }

    @Test
    fun `showClassReminderNotification - creates notification with correct content`() {
        // Arrange
        val data = ClassReminderData(
            scheduleId = 1L,
            courseId = 101L,
            courseName = "Test Course",
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            date = LocalDate.now()
        )

        // Act
        NotificationHelper.showClassReminderNotification(context, data)

        // Assert
        val notifications = shadowNotificationManager.allNotifications
        assertEquals(1, notifications.size)
        
        val notification = notifications[0]
        val extras = notification.extras
        
        assertEquals("Test Course", extras.getCharSequence(Notification.EXTRA_TITLE).toString())
        // Notification body uses string resource with format: "09:00 AM - 10:00 AM" (depends on R.string.notification_body)
        assertNotNull(extras.getCharSequence(Notification.EXTRA_TEXT))
        
        // Check actions
        assertEquals(3, notification.actions.size)
        assertEquals(context.getString(R.string.mark_present), notification.actions[0].title)
        assertEquals(context.getString(R.string.mark_absent), notification.actions[1].title)
        assertEquals(context.getString(R.string.mark_cancelled), notification.actions[2].title)
    }

    @Test
    fun `dismissNotification - removes notification from manager`() {
        // Arrange
        val notificationId = 1
        val notification = Notification.Builder(context, NotificationHelper.CHANNEL_ID)
            .setContentTitle("Title")
            .setSmallIcon(R.drawable.baseline_calendar_today_24)
            .build()
        notificationManager.notify("class_reminder", notificationId, notification)
        assertEquals(1, shadowNotificationManager.allNotifications.size)

        // Act
        NotificationHelper.dismissNotification(context, notificationId)

        // Assert
        assertEquals(0, shadowNotificationManager.allNotifications.size)
    }
}
