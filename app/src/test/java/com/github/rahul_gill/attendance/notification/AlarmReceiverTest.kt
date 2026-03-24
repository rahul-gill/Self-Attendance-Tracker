package com.github.rahul_gill.attendance.notification

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.github.rahul_gill.attendance.db.CourseClassStatus
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S], application = TestAttendanceApp::class)
class AlarmReceiverTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockkObject(NotificationHelper)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `onReceive - valid data - shows notification`() {
        // Arrange
        val data = ClassReminderData(
            scheduleId = 1L,
            courseId = 101L,
            courseName = "Test Course",
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            date = LocalDate.now()
        )
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(ClassReminderData.EXTRA_KEY, data)
        }
        every { NotificationHelper.showClassReminderNotification(any(), any()) } returns Unit

        // Act
        val receiver = AlarmReceiver()
        receiver.onReceive(context, intent)

        // Assert
        verify { NotificationHelper.showClassReminderNotification(context, data) }
    }

    @Test
    fun `onReceive - missing data - does not show notification`() {
        // Arrange
        val intent = Intent(context, AlarmReceiver::class.java)
        // No extra data put

        // Act
        val receiver = AlarmReceiver()
        receiver.onReceive(context, intent)

        // Assert
        verify(exactly = 0) { NotificationHelper.showClassReminderNotification(any(), any()) }
    }
}
