package com.github.rahul_gill.attendance.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.AttendanceCounts
import com.github.rahul_gill.attendance.db.AttendanceRecordHybrid
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.CourseDetailsOverallItem
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.FutureThingCalculations
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.ui.comps.SetClassStatusSheet
import com.github.rahul_gill.attendance.ui.comps.TabItem
import com.github.rahul_gill.attendance.ui.comps.Tabs
import com.github.rahul_gill.attendance.util.timeFormatter
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    onCreateCourse: () -> Unit,
    goToSettings: () -> Unit = {},
    goToCourseDetails: (courseId: Long) -> Unit,
    onSetClassStatus: (item: AttendanceRecordHybrid, newStatus: CourseClassStatus) -> Unit,
    todayClasses: List<Pair<AttendanceRecordHybrid, AttendanceCounts>>,
    courses: List<CourseDetailsOverallItem>,
) {
    val pagerState = rememberPagerState(
        initialPage = PreferenceManager.defaultHomeTabPref.value,
        pageCount = { 2 })

    val scope = rememberCoroutineScope()
    val scrollStateToday = rememberLazyListState()
    val scrollStateOverall = rememberLazyListState()
    val isScrolling = if (pagerState.currentPage == 0) {
        scrollStateToday.isScrollInProgress
    } else {
        scrollStateOverall.isScrollInProgress
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                ),
                                tileMode = TileMode.Mirror
                            )
                        )
                    )
                },
                actions = {
                    IconButton(onClick = goToSettings, modifier = Modifier.testTag("go_to_settings")) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_settings_24),
                            contentDescription = stringResource(
                                id = R.string.settings
                            )
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isScrolling,
                enter = slideInHorizontally(initialOffsetX = { it / 2 }),
                exit = slideOutHorizontally(targetOffsetX = { it / 2 })
            ) {
                ExtendedFloatingActionButton(
                    onClick = onCreateCourse,
                    modifier = Modifier.testTag("create_course_button")
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = null
                    )
                    Text(text = stringResource(id = R.string.create_a_course))
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Tabs {
                var commonMinWidth by remember {
                    mutableIntStateOf(0)
                }
                TabItem(
                    modifier = Modifier
                        .onSizeChanged {
                            commonMinWidth = maxOf(commonMinWidth, it.width)
                        }
                        .widthIn(min = with(
                            LocalDensity.current
                        ) { commonMinWidth.toDp() }),
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = stringResource(id = R.string.today_classes),
                    selected = pagerState.currentPage == 0
                )
                TabItem(
                    modifier = Modifier
                        .testTag("courses_button")
                        .onSizeChanged {
                            commonMinWidth = maxOf(commonMinWidth, it.width)
                        }
                        .widthIn(min = with(
                            LocalDensity.current
                        ) { commonMinWidth.toDp() }),
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = stringResource(id = R.string.courses),
                    selected = pagerState.currentPage == 1
                )
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
            ) { page ->
                if (page == 0) {
                    if (todayClasses.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = stringResource(id = R.string.no_classes_today))
                        }
                    } else {
                        var setClassSheetItem: AttendanceRecordHybrid? by remember {
                            mutableStateOf(null)
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = scrollStateToday,
                            verticalArrangement = Arrangement.spacedBy(
                                8.dp,
                                alignment = Alignment.Top
                            )
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                            items(
                                todayClasses,
                            ) { classItem ->
                                TodayClassItem(
                                    item = classItem.first,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    onClick = {
                                        setClassSheetItem = classItem.first
                                    },
                                    attendanceCounts = classItem.second
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                        if (setClassSheetItem != null) {
                            SetClassStatusSheet(
                                todayCourseItem = setClassSheetItem!!,
                                onDismissRequest = { setClassSheetItem = null },
                                setClasStatus = { onSetClassStatus(setClassSheetItem!!, it) }
                            )
                        }
                    }
                } else if (page == 1) {
                    if (courses.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = stringResource(id = R.string.no_courses_added_yet))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = scrollStateOverall,
                            verticalArrangement = Arrangement.spacedBy(
                                8.dp,
                                alignment = Alignment.Top
                            )
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                            items(courses, key = { it.courseId }) { courseItem ->
                                OverallCourseItem(
                                    item = courseItem,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    onClick = {
                                        goToCourseDetails(courseItem.courseId)
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun OverallCourseItem(
    item: CourseDetailsOverallItem = CourseDetailsOverallItem(
        courseId = 1,
        courseName = "Maths",
        requiredAttendance = 75.0,
        currentAttendancePercentage = 86.0,
        presents = 100,
        absents = 20,
        cancels = 5
    ),
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    OutlinedCard(onClick = onClick, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = item.courseName,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = "Current: ${item.currentAttendancePercentage.toInt()}% Goal: ${item.requiredAttendance.toInt()}%",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                var commonMaxWidth by remember {
                    mutableIntStateOf(0)
                }
                val density = LocalDensity.current
                LaunchedEffect(density) {
                    commonMaxWidth =
                        maxOf(commonMaxWidth, with(density) { 60.dp.toPx().toInt() })
                }
                val sizeModifier = Modifier
                    .onSizeChanged {
                        commonMaxWidth = maxOf(commonMaxWidth, it.width)
                    }
                    .widthIn(min = with(density) { commonMaxWidth.toDp() })

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = sizeModifier
                        .background(
                            shape = RoundedCornerShape(25),
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(text = "P", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = item.presents.toString())
                }
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = sizeModifier
                        .background(
                            shape = RoundedCornerShape(25),
                            color = MaterialTheme.colorScheme.errorContainer
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(text = "A", color = MaterialTheme.colorScheme.onErrorContainer)
                    Text(text = item.absents.toString())
                }

                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = sizeModifier
                        .background(
                            shape = RoundedCornerShape(25),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(text = "C", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = item.cancels.toString())
                }
            }
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(
                text = FutureThingCalculations.getMessageForFuture(
                    item.presents, item.absents, item.requiredAttendance.toInt()
                ),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}


@Composable
fun TodayClassItem(
    item: AttendanceRecordHybrid,
    attendanceCounts: AttendanceCounts?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    OutlinedCard(
        modifier = Modifier
            .animateContentSize()
            .then(modifier),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.startTime.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = item.endTime.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = item.courseName, style = MaterialTheme.typography.titleLarge)
                    if (item is AttendanceRecordHybrid.ExtraClass) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(25),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                text = stringResource(id = R.string.extra_class),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                val surfaceColor = when (item.classStatus) {
                    CourseClassStatus.Present -> MaterialTheme.colorScheme.primaryContainer
                    CourseClassStatus.Absent -> MaterialTheme.colorScheme.errorContainer
                    CourseClassStatus.Cancelled -> MaterialTheme.colorScheme.surfaceVariant
                    CourseClassStatus.Unset -> MaterialTheme.colorScheme.surfaceVariant
                }
                Surface(
                    modifier = Modifier
                        .background(shape = RoundedCornerShape(25), color = surfaceColor),
                    shape = RoundedCornerShape(25),
                    color = surfaceColor
                ) {
                    Text(
                        modifier = Modifier
                            .minimumInteractiveComponentSize(),
                        text = when (item.classStatus) {
                            CourseClassStatus.Present -> "P"
                            CourseClassStatus.Absent -> "A"
                            CourseClassStatus.Cancelled -> "C"
                            CourseClassStatus.Unset -> "~"
                        },
                        color = when (item.classStatus) {
                            CourseClassStatus.Present -> MaterialTheme.colorScheme.onPrimaryContainer
                            CourseClassStatus.Absent -> MaterialTheme.colorScheme.onErrorContainer
                            CourseClassStatus.Cancelled -> MaterialTheme.colorScheme.onSurfaceVariant
                            CourseClassStatus.Unset -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(
                text = if (attendanceCounts != null)
                    FutureThingCalculations.getMessageForFuture(
                        attendanceCounts.present.toInt(),
                        attendanceCounts.absents.toInt(),
                        attendanceCounts.requiredPercentage.toInt()
                    ) else "",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}