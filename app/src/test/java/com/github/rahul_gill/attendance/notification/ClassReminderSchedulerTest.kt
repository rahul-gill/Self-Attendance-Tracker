package com.github.rahul_gill.attendance.notification

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Custom application class for tests to avoid executing real AttendanceApp.onCreate()
 * which depends on global state that is hard to mock in Robolectric before application start.
 */
class TestAttendanceApp : Application() {
    override fun onCreate() {
        // Do nothing or minimal setup
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S], application = TestAttendanceApp::class)
class ClassReminderSchedulerTest {

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager
    private lateinit var shadowAlarmManager: ShadowAlarmManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        shadowAlarmManager = shadowOf(alarmManager)

        mockkObject(PreferenceManager)
        mockkObject(DBOps)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `scheduleAlarmsForToday - notifications disabled - does not schedule any alarms`() {
        // Arrange
        every { PreferenceManager.notificationsEnabled.value } returns false

        // Act
        ClassReminderScheduler.scheduleAlarmsForToday(context)

        // Assert
        assertEquals(0, shadowAlarmManager.scheduledAlarms.size)
    }

    @Test
    fun `scheduleAlarmsForToday - future class - schedules alarm`() {
        // Arrange
        every { PreferenceManager.notificationsEnabled.value } returns true
        
        val now = LocalTime.now()
        val futureClassTime = now.plusMinutes(10) 
        
        val testClass = DBOps.ScheduleClassForNotification(
            scheduleId = 1L,
            courseId = 101L,
            courseName = "Test Course",
            startTime = futureClassTime,
            endTime = futureClassTime.plusHours(1)
        )

        every { DBOps.instance.getActiveScheduleClassesForWeekday(any()) } returns listOf(testClass)

        // Act
        ClassReminderScheduler.scheduleAlarmsForToday(context)

        // Assert
        val scheduledAlarms = shadowAlarmManager.scheduledAlarms
        assertEquals(1, scheduledAlarms.size)
        val alarm = scheduledAlarms.first()
        
        val expectedTriggerTime = futureClassTime.minusMinutes(5)
            .atDate(LocalDate.now())
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedTriggerTime, alarm.triggerAtMs)
    }

    @Test
    fun `scheduleAlarmsForToday - past class - does not schedule alarm`() {
        // Arrange
        every { PreferenceManager.notificationsEnabled.value } returns true
        
        val now = LocalTime.now()
        val pastClassTime = now.minusMinutes(2) // Started 2 mins ago, reminder was 7 mins ago
        
        val testClass = DBOps.ScheduleClassForNotification(
            scheduleId = 2L,
            courseId = 102L,
            courseName = "Past Course",
            startTime = pastClassTime,
            endTime = pastClassTime.plusHours(1)
        )

        every { DBOps.instance.getActiveScheduleClassesForWeekday(any()) } returns listOf(testClass)

        // Act
        ClassReminderScheduler.scheduleAlarmsForToday(context)

        // Assert
        assertEquals(0, shadowAlarmManager.scheduledAlarms.size)
    }

    @Test
    fun `scheduleAlarmsForToday - empty schedule - does not schedule alarm`() {
        // Arrange
        every { PreferenceManager.notificationsEnabled.value } returns true
        every { DBOps.instance.getActiveScheduleClassesForWeekday(any()) } returns emptyList()

        // Act
        ClassReminderScheduler.scheduleAlarmsForToday(context)

        // Assert
        assertEquals(0, shadowAlarmManager.scheduledAlarms.size)
    }

    @Test
    fun `cancelAllAlarms - clears tracked IDs in preferences`() {
        // Arrange
        val prefs = context.getSharedPreferences("class_reminder_scheduler", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("scheduled_alarm_request_codes", setOf("10", "20")).apply()

        // Act
        ClassReminderScheduler.cancelAllAlarms(context)

        // Assert
        assertTrue(prefs.getStringSet("scheduled_alarm_request_codes", emptySet())!!.isEmpty())
    }

    @Test
    fun `scheduleAlarmsForToday - cleans up old tracked alarms before scheduling new ones`() {
         // Arrange
        every { PreferenceManager.notificationsEnabled.value } returns true
        
        // Populate prefs with a stale request code
        val prefs = context.getSharedPreferences("class_reminder_scheduler", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("scheduled_alarm_request_codes", setOf("999")).apply()

        val futureTime = LocalTime.now().plusMinutes(20)
        val testClass = DBOps.ScheduleClassForNotification(
            scheduleId = 1L,
            courseId = 101L,
            courseName = "New Course",
            startTime = futureTime,
            endTime = futureTime.plusHours(1)
        )
        every { DBOps.instance.getActiveScheduleClassesForWeekday(any()) } returns listOf(testClass)

        // Act
        ClassReminderScheduler.scheduleAlarmsForToday(context)

        // Assert
        // Verify prefs now only contain the new ID
        val savedCodes = prefs.getStringSet("scheduled_alarm_request_codes", emptySet())
        assertEquals(setOf("1"), savedCodes)
    }
}
