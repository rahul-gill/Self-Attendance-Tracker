package com.github.rahul_gill.attendance.notification

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.github.rahul_gill.attendance.prefs.PreferenceManager
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S], application = TestAttendanceApp::class)
class BootReceiverTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockkObject(PreferenceManager)
        mockkObject(ClassReminderScheduler)
        mockkObject(DailySchedulerWorker)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `onReceive - boot completed and notifications enabled - reschedules alarms and enqueues worker`() {
        // Arrange
        every { PreferenceManager.notificationsEnabled.value } returns true
        every { ClassReminderScheduler.scheduleAlarmsForToday(any()) } returns Unit
        every { DailySchedulerWorker.enqueue(any()) } returns Unit

        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)

        // Act
        val receiver = BootReceiver()
        receiver.onReceive(context, intent)

        // Assert
        verify { ClassReminderScheduler.scheduleAlarmsForToday(context) }
        verify { DailySchedulerWorker.enqueue(context) }
    }

    @Test
    fun `onReceive - boot completed but notifications disabled - does nothing`() {
        // Arrange
        every { PreferenceManager.notificationsEnabled.value } returns false

        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)

        // Act
        val receiver = BootReceiver()
        receiver.onReceive(context, intent)

        // Assert
        verify(exactly = 0) { ClassReminderScheduler.scheduleAlarmsForToday(any()) }
        verify(exactly = 0) { DailySchedulerWorker.enqueue(any()) }
    }

    @Test
    fun `onReceive - other intent action - does nothing`() {
        // Arrange
        val intent = Intent(Intent.ACTION_SCREEN_ON)

        // Act
        val receiver = BootReceiver()
        receiver.onReceive(context, intent)

        // Assert
        verify(exactly = 0) { ClassReminderScheduler.scheduleAlarmsForToday(any()) }
        verify(exactly = 0) { DailySchedulerWorker.enqueue(any()) }
    }
}
