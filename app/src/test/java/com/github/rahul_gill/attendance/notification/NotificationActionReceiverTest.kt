package com.github.rahul_gill.attendance.notification

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.DBOps
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S], application = TestAttendanceApp::class)
class NotificationActionReceiverTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var shadowNotificationManager: ShadowNotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = shadowOf(notificationManager)
        
        mockkObject(DBOps)
        mockkObject(NotificationHelper)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `onReceive - mark present - updates DB and dismisses notification`() {
        // Arrange
        val date = LocalDate.now()
        val data = NotificationActionData(
            notificationId = 1,
            scheduleId = 10L,
            courseId = 100L,
            date = date,
            status = CourseClassStatus.Present
        )
        
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(NotificationActionData.EXTRA_KEY, data)
        }
        
        every { DBOps.instance.markAttendanceForScheduleClass(any(), any(), any(), any(), any()) } returns Unit
        every { NotificationHelper.dismissNotification(any(), any()) } returns Unit

        // Act
        val receiver = NotificationActionReceiver()
        receiver.onReceive(context, intent)

        // Assert
        verify { 
            DBOps.instance.markAttendanceForScheduleClass(
                attendanceId = null,
                classStatus = CourseClassStatus.Present,
                scheduleId = 10L,
                date = date,
                courseId = 100L
            )
        }
        verify { NotificationHelper.dismissNotification(context, 1) }
    }

    @Test
    fun `onReceive - mark absent - updates DB and dismisses notification`() {
        // Arrange
        val date = LocalDate.now()
        val data = NotificationActionData(
            notificationId = 2,
            scheduleId = 11L,
            courseId = 101L,
            date = date,
            status = CourseClassStatus.Absent
        )
        
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(NotificationActionData.EXTRA_KEY, data)
        }
        
        every { DBOps.instance.markAttendanceForScheduleClass(any(), any(), any(), any(), any()) } returns Unit
        every { NotificationHelper.dismissNotification(any(), any()) } returns Unit

        // Act
        val receiver = NotificationActionReceiver()
        receiver.onReceive(context, intent)

        // Assert
        verify { 
            DBOps.instance.markAttendanceForScheduleClass(
                attendanceId = null,
                classStatus = CourseClassStatus.Absent,
                scheduleId = 11L,
                date = date,
                courseId = 101L
            )
        }
        verify { NotificationHelper.dismissNotification(context, 2) }
    }
}
