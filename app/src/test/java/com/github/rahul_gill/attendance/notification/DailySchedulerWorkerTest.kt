package com.github.rahul_gill.attendance.notification

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S], application = TestAttendanceApp::class)
class DailySchedulerWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockkObject(ClassReminderScheduler)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `doWork - calls scheduleAlarmsForToday and returns success`() = runBlocking {
        // Arrange
        every { ClassReminderScheduler.scheduleAlarmsForToday(any()) } returns Unit
        
        val worker = TestListenableWorkerBuilder<DailySchedulerWorker>(context).build()

        // Act
        val result = worker.doWork()

        // Assert
        assertEquals(ListenableWorker.Result.success(), result)
        verify { ClassReminderScheduler.scheduleAlarmsForToday(any()) }
    }

    @Test
    fun `doWork - on failure - returns retry`() = runBlocking {
        // Arrange
        every { ClassReminderScheduler.scheduleAlarmsForToday(any()) } throws RuntimeException("Test failure")
        
        val worker = TestListenableWorkerBuilder<DailySchedulerWorker>(context).build()

        // Act
        val result = worker.doWork()

        // Assert
        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
