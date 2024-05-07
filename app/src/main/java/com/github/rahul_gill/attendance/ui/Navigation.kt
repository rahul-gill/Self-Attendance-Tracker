package com.github.rahul_gill.attendance.ui

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.AttendanceRecordHybrid
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.CourseDetailsOverallItem
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.ui.screens.CourseAttendanceRecordScreen
import com.github.rahul_gill.attendance.ui.screens.CourseDetailsScreen
import com.github.rahul_gill.attendance.ui.screens.CourseEditScreen
import com.github.rahul_gill.attendance.ui.screens.CreateCourseScreen
import com.github.rahul_gill.attendance.ui.screens.MainScreen
import com.github.rahul_gill.attendance.ui.screens.SettingsScreen
import dev.olshevski.navigation.reimagined.AnimatedNavHost
import dev.olshevski.navigation.reimagined.NavAction
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import dev.olshevski.navigation.reimagined.rememberNavController
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

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

    @Parcelize
    class EditCourse(val courseDetailsOverallItem: CourseDetailsOverallItem) : Screen
}

@Composable
fun RootNavHost(
    dbOps: DBOps = DBOps.instance
) {
    val navController = rememberNavController<Screen>(Screen.Main)
    val context = LocalContext.current
    val onSetClassStatus = remember {
        { item: AttendanceRecordHybrid, status: CourseClassStatus ->
            when (item) {
                is AttendanceRecordHybrid.ExtraClass -> {
                    dbOps.markAttendanceForExtraClass(
                        item.extraClassId,
                        status
                    )
                }

                is AttendanceRecordHybrid.ScheduledClass -> {
                    dbOps.markAttendanceForScheduleClass(
                        attendanceId = item.attendanceId,
                        classStatus = status,
                        scheduleId = item.scheduleId,
                        date = item.date,
                        courseId = item.courseId
                    )
                }
            }
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
                        dbOps.createCourse(
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
                val todayItems = dbOps.getScheduleAndExtraClassesForToday().collectAsStateWithLifecycle(
                    initialValue = listOf()
                ).value
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
                    onSetClassStatus = onSetClassStatus,
                    todayClasses = todayItems
                    ,
                    courses = dbOps.getCoursesDetailsList()
                        .collectAsStateWithLifecycle(initialValue = listOf()).value
                )
            }

            Screen.Settings -> {
                SettingsScreen(
                    onGoBack = { navController.pop() }
                )
            }

            is Screen.CourseDetails -> {
                val courseDetails = dbOps.getCoursesDetailsWithId(screen.courseId)
                    .collectAsStateWithLifecycle(null)
                val classes = dbOps.getScheduleClassesForCourse(courseId = screen.courseId)
                    .collectAsStateWithLifecycle(initialValue = listOf())
                if (courseDetails.value != null) {
                    val scope = rememberCoroutineScope()
                    CourseDetailsScreen(
                        onGoBack = { navController.pop() },
                        courseDetails = courseDetails.value!!,
                        classes = classes.value,
                        goToClassRecords = { navController.navigate(Screen.CourseClassRecords(screen.courseId)) },
                        onCreateExtraClass = { timings ->
                            dbOps.createExtraClasses(
                                courseId = courseDetails.value!!.courseId,
                                timings = timings
                            )
                        },
                        goToCourseEdit = {
                            navController.navigate(Screen.EditCourse(it))
                        },
                        onDeleteCourse = { courseId ->
                            dbOps.deleteCourse(courseId)
                        },
                        onAddScheduleClass = { classDetail ->
                            dbOps.addScheduleClassForCourse(
                                courseId = courseDetails.value!!.courseId,
                                classDetails = classDetail
                            )
                        },
                        onDeleteScheduleItem = { classDetails ->
                            if (classDetails.scheduleId != null) {
                                dbOps.deleteScheduleWithId(
                                    classDetails.scheduleId,
                                )
                                Toast.makeText(
                                    context,
                                    R.string.deleted_schedule_item,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        changeActivateStatusOfScheduleItem = { classDetails, activate ->
                            if(classDetails.scheduleId != null){
                                dbOps.changeActivateStatusOfScheduleItem(classDetails.scheduleId, activate)
                            }
                        },
                        createAttendanceRecordOnSchedule = { scheduleId, classStatus, date ->
                            DBOps.instance.markAttendanceForScheduleClass(
                                scheduleId = scheduleId,
                                classStatus = classStatus,
                                attendanceId = null,
                                date = date,
                                courseId = courseDetails.value!!.courseId
                            )
                        }
                    )
                } else {
                    Box(Modifier.fillMaxSize())
                }

            }

            is Screen.CourseClassRecords -> {
                val items = dbOps.getMarkedAttendancesForCourse(screen.courseId)
                    .collectAsStateWithLifecycle(initialValue = listOf())
                val courseDetails = dbOps.getCoursesDetailsWithId(screen.courseId)
                    .collectAsStateWithLifecycle(null)
                if (courseDetails.value != null) {
                    CourseAttendanceRecordScreen(
                        onGoBack = { navController.pop() },
                        records = items.value,
                        courseDetails = courseDetails.value!!,
                        onSetClassStatus = onSetClassStatus
                    ) { todayItem ->
                        when (todayItem) {
                            is AttendanceRecordHybrid.ExtraClass -> {
                                dbOps.deleteExtraClass(todayItem.extraClassId)
                            }

                            is AttendanceRecordHybrid.ScheduledClass -> {
                                if (todayItem.attendanceId != null) {
                                    dbOps.deleteScheduleAttendanceRecord(todayItem.attendanceId)
                                }
                            }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize())
                }
            }

            is Screen.EditCourse -> {
                CourseEditScreen(
                    courseDetails = screen.courseDetailsOverallItem,
                    onGoBack = { navController.pop() },
                    onSave = { newName, newRequiredPercentage ->
                        dbOps.updateCourseDetails(
                            id = screen.courseDetailsOverallItem.courseId,
                            name = newName,
                            requiredAttendancePercentage = newRequiredPercentage.toDouble(),
                        )
                    }
                )
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