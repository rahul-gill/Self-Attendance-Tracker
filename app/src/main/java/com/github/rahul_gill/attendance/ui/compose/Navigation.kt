package com.github.rahul_gill.attendance.ui.compose

import android.os.Parcelable
import android.widget.Toast
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.TodayCourseItem
import com.github.rahul_gill.attendance.ui.compose.screens.CourseAttendanceRecordScreen
import com.github.rahul_gill.attendance.ui.compose.screens.CourseDetailsScreen
import com.github.rahul_gill.attendance.ui.compose.screens.CreateCourseScreen
import com.github.rahul_gill.attendance.ui.compose.screens.MainScreen
import com.github.rahul_gill.attendance.ui.compose.screens.SettingsScreen
import dev.olshevski.navigation.reimagined.AnimatedNavHost
import dev.olshevski.navigation.reimagined.NavAction
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import dev.olshevski.navigation.reimagined.rememberNavController
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

sealed interface Screen : Parcelable {
    @Parcelize
    data object Main : Screen

    @Parcelize
    data object Settings : Screen

    @Parcelize
    data object CreateCourse : Screen

    @Parcelize
    class CourseDetails(val courseId: Long) : Screen


    @Parcelize
    class CourseClassRecords(val courseId: Long) : Screen
}

@Composable
fun RootNavHost() {
    val navController = rememberNavController<Screen>(Screen.Main)
    val context = LocalContext.current
    val onSetClassStatus = remember {
        { item: TodayCourseItem, status: CourseClassStatus ->
            if (item.isExtraClass)
                DBOps.instance.markAttendanceForExtraClass(
                    item.scheduleIdOrExtraClassId,
                    status
                )
            else DBOps.instance.markAttendanceForScheduleClass(
                scheduleId = item.scheduleIdOrExtraClassId,
                date = LocalDate.now(),
                classStatus = status
            )
        }
    }

    NavBackHandler(controller = navController)
    AnimatedNavHost(
        controller = navController,
        transitionSpec = { action, _, _ ->
            materialSharedAxisZIn(forward = action != NavAction.Pop) togetherWith
                    materialSharedAxisZOut(forward = action == NavAction.Pop)
        }
    ) { screen ->
        when (screen) {
            Screen.CreateCourse -> {
                CreateCourseScreen(
                    onGoBack = { navController.pop() },
                    onSave = { courseName, percentage, classes ->
                        DBOps.instance.createCourse(
                            name = courseName,
                            requiredAttendancePercentage = percentage.toDouble(),
                            schedule = classes
                        )
                        Toast.makeText(
                            context,
                            context.getString(R.string.course_created, courseName),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            Screen.Main -> {
                MainScreen(
                    onCreateCourse = { navController.navigate(Screen.CreateCourse) },
                    goToSettings = { navController.navigate(Screen.Settings) },
                    goToCourseDetails = { courseId ->
                        navController.navigate(
                            Screen.CourseDetails(
                                courseId
                            )
                        )
                    },
                    onSetClassStatus = onSetClassStatus
                )
            }

            Screen.Settings -> {
                SettingsScreen(
                    onGoBack = { navController.pop() }
                )
            }

            is Screen.CourseDetails -> {
                val courseDetails = DBOps.instance.getCoursesDetailsWithId(screen.courseId)
                    .collectAsStateWithLifecycle(null)
                val classes = DBOps.instance.getScheduleClassesForCourse(courseId = screen.courseId)
                    .collectAsStateWithLifecycle(initialValue = listOf())
                if (courseDetails.value != null) {
                    CourseDetailsScreen(
                        onGoBack = { navController.pop() },
                        courseDetails = courseDetails.value!!,
                        classes = classes.value,
                        goToClassRecords = { navController.navigate(Screen.CourseClassRecords(screen.courseId)) }
                    )
                }

            }

            is Screen.CourseClassRecords -> {
                val items = DBOps.instance.getMarkedAttendancesForCourse(screen.courseId)
                    .collectAsStateWithLifecycle(initialValue = listOf())
                val courseDetails = DBOps.instance.getCoursesDetailsWithId(screen.courseId)
                    .collectAsStateWithLifecycle(null)
                if (courseDetails.value != null) {
                    CourseAttendanceRecordScreen(
                        onGoBack = { navController.pop() },
                        records = items.value,
                        courseDetails = courseDetails.value!!,
                        onSetClassStatus = onSetClassStatus,
                        onDeleteRecord = { todayItem ->
                            if(todayItem.isExtraClass)
                                DBOps.instance.deleteExtraClass(todayItem.scheduleIdOrExtraClassId)
                            else
                                DBOps.instance.deleteScheduleAttendanceRecord(todayItem.scheduleIdOrExtraClassId, todayItem.date!!)
                        }
                    )
                }
            }
        }
    }
}


private const val AnimationDuration = 250

private const val ProgressThreshold = 0.35f
private val Int.ForOutgoing: Int
    get() = (this * ProgressThreshold).toInt()

private val Int.ForIncoming: Int
    get() = this - this.ForOutgoing

fun materialSharedAxisZIn(
    forward: Boolean,
    durationMillis: Int = AnimationDuration,
): EnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = durationMillis.ForIncoming,
        delayMillis = durationMillis.ForOutgoing,
        easing = LinearOutSlowInEasing
    )
) + scaleIn(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing
    ),
    initialScale = if (forward) 0.8f else 1.1f
)

fun materialSharedAxisZOut(
    forward: Boolean,
    durationMillis: Int = AnimationDuration,
): ExitTransition = fadeOut(
    animationSpec = tween(
        durationMillis = durationMillis.ForOutgoing,
        delayMillis = 0,
        easing = FastOutLinearInEasing
    )
) + scaleOut(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing
    ),
    targetScale = if (forward) 1.1f else 0.8f
)