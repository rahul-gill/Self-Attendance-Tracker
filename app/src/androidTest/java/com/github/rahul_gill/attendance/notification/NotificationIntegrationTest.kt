package com.github.rahul_gill.attendance.notification

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.ClassDetail
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.DBOps
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.exp

class NotificationIntegrationTest {

    private lateinit var context: Context
    private lateinit var device: UiDevice
    private val dbOps = DBOps.instance

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // Grant notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS"
            )
        }

        // Ensure channel exists
        NotificationHelper.createNotificationChannel(context)
        
        // Clear existing notifications
        device.openNotification()
        device.wait(Until.hasObject(By.pkg("com.android.systemui")), 2000)
        
        // Try to find "Clear all" or similar. If not found, just swipe up to close shade.
        val clearAll =device.wait(
            Until.findObject(By.text(Pattern.compile("(?i).*clear all.*"))),
            3000
        )
        clearAll?.click()
        device.pressBack()
        device.pressHome() // Start from home for a clean state
    }

    @Test
    fun testFullNotificationLifecycle() = runBlocking {
        val courseName = "Integration Test Course"
        val now = LocalTime.now()
        
        // Ensure we are not too close to midnight to avoid weekday issues
        val startTime = if (now.isAfter(LocalTime.of(23, 0))) {
            now.minusHours(1) // Should have already triggered if we were at the end of day
        } else {
            now.plusMinutes(10)
        }
        val endTime = startTime.plusHours(1)
        val today = LocalDate.now()

        // 1. Prepare data in DB
        val courseId = dbOps.createCourse(
            name = courseName,
            requiredAttendancePercentage = 75.0,
            schedule = listOf(
                ClassDetail(
                    dayOfWeek = today.dayOfWeek,
                    startTime = startTime,
                    endTime = endTime
                )
            )
        )
        
        val scheduleItem = dbOps.getScheduleClassesForCourse(courseId).first().first()
        val realScheduleId = scheduleItem.scheduleId!!

        // 2. Test Scheduling Step
        // We need to ensure notifications are enabled in prefs for the scheduler to work
        com.github.rahul_gill.attendance.prefs.PreferenceManager.notificationsEnabled.setValue(true)
        
        ClassReminderScheduler.scheduleAlarmsForToday(context)
        
        // Verify Alarm is scheduled
        val alarmDump = device.executeShellCommand("dumpsys alarm")
        assertTrue("Alarm should be scheduled for our package", alarmDump.contains(context.packageName))
        // More specific check for the schedule ID which is used as the request code
        assertTrue("Alarm should contain the schedule ID as request code", alarmDump.contains(realScheduleId.toString()))

        // 3. Manually trigger AlarmReceiver
        val reminderData = ClassReminderData(
            scheduleId = realScheduleId,
            courseId = courseId,
            courseName = courseName,
            startTime = startTime,
            endTime = endTime,
            date = today
        )
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(ClassReminderData.EXTRA_KEY, reminderData)
            setPackage(context.packageName)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }
        context.sendBroadcast(intent)

        // 4. Verify Notification appears
        device.openNotification()
        
        // Wait longer and look for the specific course name
        val notificationFound = device.wait(Until.hasObject(By.text(courseName)), 10000)
        assertTrue("Notification for '$courseName' should appear in the shade", notificationFound)

        // 5. Click "Present" action
        val presentText = context.getString(R.string.mark_present)
        device.wait(
            Until.findObject(By.text(presentText)),
            1000
        )

        val hierarchy = run {
            var root = device.findObject(By.text(courseName))
            while (root.parent != null) {
                root = root.parent
            }
            fun printHierarchy(node: UiObject2, depth: Int): String {
                var s = "--|".repeat(depth) + "${node.className}::${node.text}::${node.resourceName}\n"
                for(child in node.children) {
                    s += printHierarchy(child, depth + 1)
                }
                return s
            }

            printHierarchy(root, 0)
        }

        val courseNotification = device.findObject(By.text(courseName))
        val notificationObject = run {
            var node = courseNotification
            while (node.parent != null && node.resourceName != "android:id/notification_headerless_view_row") {
                node = node.parent
            }
            node
        }
        val expandButton
            = notificationObject.findObject(By.res("android:id/expand_button"))
        assertNotNull("hierarchy : $hierarchy", expandButton)
        expandButton.click()

        val presentAction = device.findObject(By.text(Pattern.compile("(?i)${presentText}")))
        assertNotNull("Button with text '$presentText' not found in notification hierarchy: $hierarchy",
            presentAction)
        presentAction.click()

        // 6. Verify notification is dismissed
        val notificationGone = device.wait(Until.gone(By.text(courseName)), 10000)
        assertTrue("Notification should disappear after clicking action", notificationGone)
        
        // Close notification shade if it's still open (some OEMs keep it open after action)
        device.pressBack()

        // 7. Verify DB update
        val attendances = dbOps.getMarkedAttendancesForCourse(courseId).first()
        val attendance = attendances.filterIsInstance<com.github.rahul_gill.attendance.db.AttendanceRecordHybrid.ScheduledClass>()
            .find { it.scheduleId == realScheduleId && it.date == today }
            
        assertNotNull("Attendance record was not created in DB", attendance)
        assertEquals("Attendance status should be Present", CourseClassStatus.Present, attendance?.classStatus)
        
        // Cleanup: delete the test course
        dbOps.deleteCourse(courseId)
    }
}
