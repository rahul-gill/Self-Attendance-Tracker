package com.github.rahul_gill.attendance.ui.compose.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideIn
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.CourseDetailsOverallItem
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.TodayCourseItem
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.ui.compose.comps.TabItem
import com.github.rahul_gill.attendance.ui.compose.comps.Tabs
import com.github.rahul_gill.attendance.util.timeFormatter
import kotlinx.coroutines.launch
import java.time.LocalTime


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OverallCoursesScreen(
    onCreateCourse: () -> Unit,
    goToSettings: () -> Unit = {},
    goToCourseDetails: (courseId: Long) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = PreferenceManager.defaultHomeTabPref.value,
        pageCount = { 2 })
    val todayClasses = remember {
        mutableStateOf(lst1)
    }
//        DBOps.instance.getScheduleAndExtraClassesForToday().collectAsStateWithLifecycle(
//            initialValue = listOf()
//        )
    val scope = rememberCoroutineScope()
    val courses = remember {
        mutableStateOf(lst2)
    }
//        DBOps.instance.getCoursesDetailsList().collectAsStateWithLifecycle(initialValue = listOf())
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
                    )
                },
                actions = {
                    IconButton(onClick = goToSettings) {
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
                    onClick = onCreateCourse
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
                    if (todayClasses.value.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = stringResource(id = R.string.no_classes_today))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = scrollStateToday,
                            verticalArrangement = Arrangement.spacedBy(
                                8.dp,
                                alignment = Alignment.Top
                            )
                        ) {
                            items(
                                todayClasses.value,
                                key = { "" + it.scheduleIdOrExtraClassId + it.isExtraClass }
                            ) { classItem ->
                                TodayClassItem(
                                    item = classItem,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    onClick = {
                                        //TODO
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                } else if (page == 1) {
                    if (courses.value.isEmpty()) {
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
                            items(courses.value, key = { it.courseId }) { courseItem ->
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

val lst1 = List(5) {
    TodayCourseItem(
        startTime = LocalTime.now().withMinute(0),
        endTime = LocalTime.now().withMinute(0).plusHours(1),
        courseName = "Maths",
        scheduleIdOrExtraClassId = it.toLong(),
        classStatus = CourseClassStatus.Present,
        isExtraClass = true
    )
}

val lst2 = List(100) {
    CourseDetailsOverallItem(
        courseId = it.toLong(),
        courseName = "Maths",
        requiredAttendance = 75.0,
        currentAttendancePercentage = 86.0,
        presents = 100,
        absents = 20,
        cancels = 5
    )
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
    ElevatedCard(onClick = onClick, modifier = modifier) {
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
        }
    }
}


@Composable
@Preview
fun TodayClassItem(
    item: TodayCourseItem = TodayCourseItem(
        startTime = LocalTime.now().withMinute(0),
        endTime = LocalTime.now().withMinute(0).plusHours(1),
        courseName = "Maths",
        scheduleIdOrExtraClassId = 1,
        classStatus = CourseClassStatus.Present,
        isExtraClass = true
    ),
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    ElevatedCard(modifier = modifier, onClick = onClick) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
                if (item.isExtraClass) {
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
    }
}