package com.github.rahul_gill.attendance

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.rahul_gill.attendance.db.AttendanceRecordHybrid
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.CourseDetailsOverallItem
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.ui.comps.AttendanceAppTheme
import com.github.rahul_gill.attendance.ui.screens.CreateCourseScreen
import com.github.rahul_gill.attendance.ui.screens.MainScreen
import com.github.rahul_gill.attendance.ui.screens.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters


@RunWith(AndroidJUnit4::class)
class ScreenGrabTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Before
    fun init() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
    }

    @Test
    fun mainScreenScreenshots() {
        composeTestRule.setContent {
            AttendanceAppTheme {
                MainScreen(
                    onCreateCourse = {},
                    goToSettings = {},
                    goToCourseDetails = {},
                    onSetClassStatus = { _, _ -> },
                    todayClasses = demoTodayClasses,
                    courses = courses
                )
            }
        }
        composeTestRule.takeScreenshot("1_main_screen_today")
        composeTestRule.onNodeWithTag(testTag = "courses_button").performClick()
        composeTestRule.takeScreenshot("2_main_screen_overall_courses")
    }

    @Test
    fun settingsScreenshots() {
        PreferenceManager.followSystemColors.setValue(false)
        composeTestRule.setContent {
            SettingsScreen {}
        }
        composeTestRule.takeScreenshot("3_main_screen_today")
    }

    @Test
    fun createCourseScreenshots() {
        composeTestRule.setContent {
            CreateCourseScreen(onGoBack = { }, onSave = { _, _, _ -> })
        }
    }


    private val courses = listOf(
        "Molecular Physics",
        "Chemistry",
        "Social Education",
        "Fighting lab",
        "French",
        "Mathematics",
        "English",
        "Nuclear Physics"
    ).mapIndexed { indx, name ->
        val presents = 10
        val absents = if (indx == 4) 2 else 0
        val cancels = 0
        CourseDetailsOverallItem(
            courseId = indx.toLong(),
            courseName = name,
            requiredAttendance = 75.0,
            currentAttendancePercentage = 100.0 * presents / (presents + absents),
            presents = presents.toInt(),
            absents = absents.toInt(),
            cancels = cancels.toInt()
        )
    }
    private val demoTodayClasses: List<AttendanceRecordHybrid>
        get() {
            val date = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            var startTime = LocalTime.of(10, 0)
            val endTime = { startTime.plusHours(1) }
            val list = mutableListOf<AttendanceRecordHybrid>()

            for (i in 0..7) {
                if (i == 5) {
                    AttendanceRecordHybrid.ExtraClass(
                        extraClassId = i.toLong(),
                        startTime = startTime,
                        endTime = endTime(),
                        courseName = courses[i].courseName,
                        date = date,
                        classStatus = CourseClassStatus.Present,
                        courseId = i.toLong()
                    )
                } else {
                    list.add(
                        AttendanceRecordHybrid.ScheduledClass(
                            attendanceId = i.toLong(),
                            scheduleId = i.toLong(),
                            startTime = startTime,
                            endTime = endTime(),
                            courseName = courses[i].courseName,
                            date = date,
                            classStatus = if (i == 2) CourseClassStatus.Absent else CourseClassStatus.Present,
                            courseId = i.toLong()
                        )
                    )
                }
                startTime = startTime.plusHours(1)
            }


            return list
        }

    private fun <T : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.takeScreenshot(
        screenshotName: String
    ) {
        runBlocking {
            awaitIdle()
            delay(100)
            Screengrab.screenshot(screenshotName)
        }
    }

    private fun ComposeContentTestRule.takeScreenshot(
        screenshotName: String
    ) {
        runBlocking {
            awaitIdle()
            delay(100)
            Screengrab.screenshot(screenshotName)
        }
    }
}