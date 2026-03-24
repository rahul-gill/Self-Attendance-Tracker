package com.github.rahul_gill.attendance

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.rahul_gill.attendance.db.ClassDetail
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.CourseDetailsOverallItem
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.ExtraClassTimings
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.ui.RootNavHost
import com.github.rahul_gill.attendance.ui.comps.AttendanceAppTheme
import com.github.rahul_gill.attendance.ui.comps.ColorSchemeType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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
        val dbOps = DBOps.instance
        PreferenceManager.onboardingCompleted.setValue(true)
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        courses.forEach {
            dbOps.createCourse(
                name = it.courseName,
                requiredAttendancePercentage = it.requiredAttendance,
                schedule = listOf(
                    ClassDetail(
                        dayOfWeek = DayOfWeek.MONDAY,
                    ),
                    ClassDetail(
                        dayOfWeek = DayOfWeek.WEDNESDAY,
                    )
                )
            )
        }
        runBlocking {
            dbOps.getCoursesDetailsList().firstOrNull()?.let { courses ->
                courses.forEachIndexed { courseIndex, course ->
                    val schedule = dbOps.getScheduleClassesForCourse(course.courseId).first()
                    val dateX = LocalDate.now()
                        .with(TemporalAdjusters.firstInMonth(schedule.first().dayOfWeek))
                    for (i in 0..10) {
                        dbOps.markAttendanceForScheduleClass(
                            attendanceId = null,
                            classStatus = if (course.courseName == "Chemistry" && (i == 2 || i == 4))
                                CourseClassStatus.Absent
                            else CourseClassStatus.Present,
                            scheduleId = schedule.first().scheduleId,
                            date = dateX.minusWeeks(i.toLong()),
                            courseId = course.courseId
                        )
                    }
                    if (courseIndex % 2 == 1) {
                        dbOps.createExtraClasses(
                            courseId = course.courseId,
                            timings = ExtraClassTimings(
                                date = LocalDate.now(),
                                startTime = LocalTime.of(13, 0),
                                endTime = LocalTime.of(14, 0)
                            )
                        )
                    } else {
                        dbOps.markAttendanceForScheduleClass(
                            attendanceId = null,
                            classStatus = CourseClassStatus.Unset,
                            scheduleId = schedule.first().scheduleId,
                            date = LocalDate.now(),
                            courseId = course.courseId
                        )
                    }
                }
            }
        }
    }

    @Test
    fun onboardingScreenshots() {
        PreferenceManager.onboardingCompleted.setValue(false)
        composeTestRule.setContent {
            AttendanceAppTheme(
                colorSchemeType = ColorSchemeType.WithSeed(Color.Green)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavHost()
                }
            }
        }
        composeTestRule.takeScreenshot("0_onboarding")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun mainScreenScreenshots() {
        PreferenceManager.onboardingCompleted.setValue(true)
        composeTestRule.setContent {
            AttendanceAppTheme(
                colorSchemeType = ColorSchemeType.WithSeed(Color.Green)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavHost(
                    )
                }
            }
        }
        //main screen
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("courses_button").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.takeScreenshot("1_main_screen_today")
        composeTestRule.onNodeWithTag(testTag = "courses_button").performClick()
        composeTestRule.takeScreenshot("2_main_screen_overall_courses")
        //course details
        composeTestRule.onNodeWithText(text = "Chemistry").performClick()
        composeTestRule.takeScreenshot("3_course_details")
        composeTestRule.onNodeWithTag(testTag = "go_back").performClick()
        //create course
        composeTestRule.onNodeWithTag(testTag = "create_course_button").performClick()
        composeTestRule.onNodeWithTag(testTag = "course_name_input").run {
            performClick()
            performKeyInput { listOf(Key.M, Key.A, Key.T, Key.H, Key.S).forEach { pressKey(it) } }
        }
        composeTestRule.onNodeWithTag(testTag = "add_class_button").performClick()
        composeTestRule.onNodeWithTag(testTag = "sheet_add_class_button").performClick()
        composeTestRule.takeScreenshot("4_create_course")
        composeTestRule.onNodeWithTag(testTag = "go_back").performClick()
        //settings
        composeTestRule.onNodeWithTag(testTag = "go_to_settings").performClick()
        composeTestRule.takeScreenshot("5_settings")
    }


    private val courses = listOf(
        "Physics",
        "Chemistry",
        "Social",
        "Fighting lab",
        "French",
        "Mathematics",
        "English",
        "Politics"
    ).mapIndexed { indx, name ->
        val presents = 10
        val absents = if (indx == 4) 2 else 0
        val cancels = 0
        CourseDetailsOverallItem(
            courseId = indx.toLong(),
            courseName = name,
            requiredAttendance = 75.0,
            currentAttendancePercentage = 100.0 * presents / (presents + absents),
            presents = presents,
            absents = absents,
            cancels = cancels
        )
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