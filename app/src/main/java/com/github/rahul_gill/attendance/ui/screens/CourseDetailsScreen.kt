package com.github.rahul_gill.attendance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.ClassDetail
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.CourseDetailsOverallItem
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.ExtraClassTimings
import com.github.rahul_gill.attendance.ui.comps.AlertDialog
import com.github.rahul_gill.attendance.ui.comps.BaseDialog
import com.github.rahul_gill.attendance.ui.comps.ClassStatusOptions
import com.github.rahul_gill.attendance.ui.comps.PopupMenu
import com.github.rahul_gill.attendance.ui.comps.ScheduleItem
import com.github.rahul_gill.attendance.ui.comps.SelectableMenuItem
import com.github.rahul_gill.attendance.ui.comps.TabItem
import com.github.rahul_gill.attendance.ui.comps.Tabs
import com.github.rahul_gill.attendance.util.dateFormatter
import com.github.rahul_gill.attendance.util.timeFormatter
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun CourseDetailsScreen(
    onGoBack: () -> Unit = {},
    courseDetails: CourseDetailsOverallItem = CourseDetailsOverallItem(
        courseId = 1,
        courseName = "Mathematics",
        currentAttendancePercentage = 90.0,
        presents = 10,
        absents = 1,
        cancels = 0,
        requiredAttendance = 75.0
    ),
    classes: List<ClassDetail> = listOf(
        ClassDetail(), ClassDetail(), ClassDetail()
    ),
    goToClassRecords: () -> Unit = {},
    goToCourseEdit: (CourseDetailsOverallItem) -> Unit = {},
    onCreateExtraClass: (ExtraClassTimings) -> Unit,
    onDeleteCourse: (Long) -> Unit
) {
    var scheduleToAddClassOn by remember {
        mutableStateOf<ClassDetail?>(null)
    }
    var showAddExtraSheet by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = courseDetails.courseName,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.go_back_screen)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { goToCourseEdit(courseDetails) }) {
                        Icon(
                            imageVector = Icons.Default.Edit, contentDescription = stringResource(
                                id = R.string.delete_course_dialog_title
                            )
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete, contentDescription = stringResource(
                                id = R.string.delete_course_dialog_title
                            )
                        )
                    }
                }
            )
        },
    ) { paddings ->
        Column(
            Modifier
                .padding(paddings)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.current_attendance_percentage))
                Text(
                    text = stringResource(
                        id = R.string.double_repr,
                        courseDetails.currentAttendancePercentage
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (courseDetails.requiredAttendance <= courseDetails.currentAttendancePercentage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.required_attendance_percentage))
                Text(
                    text = stringResource(
                        id = R.string.double_repr,
                        courseDetails.requiredAttendance
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.presents_count, courseDetails.presents),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(id = R.string.absents_count, courseDetails.absents),
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(
                            id = R.string.cancelled_classes_count,
                            courseDetails.cancels
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = goToClassRecords) {
                    Text(text = "Attendance records")
                }
                OutlinedButton(onClick = { showAddExtraSheet = true }) {
                    Text(text = "Create extra class")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.weekly_schedule),
                    style = MaterialTheme.typography.titleLarge
                )
                var showTip by remember {
                    mutableStateOf(false)
                }
                IconButton(onClick = { showTip = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(id = R.string.tip)
                    )
                    if (showTip) {
                        PopupMenu(
                            onDismissRequest = { showTip = false }
                        ) {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = "Clicking on the schedule items, you can create attendance records on that schedule"
                            )
                        }
                    }

                }
            }
            classes.forEach { classDetail ->
                var showPopup by remember {
                    mutableStateOf(false)
                }
                Spacer(modifier = Modifier.height(8.dp))
                ScheduleItem(
                    item = classDetail,
                    onClick = { showPopup = true },
                    popupContent = {
                        if (showPopup) {
                            PopupMenu(
                                onDismissRequest = { showPopup = false }
                            ) {
                                val context = LocalContext.current
                                SelectableMenuItem(
                                    label = stringResource(id = R.string.add_class_on_this_scheduele),
                                    onSelect = {
                                        scheduleToAddClassOn = classDetail
                                        showPopup = false
                                    },
                                )
                                SelectableMenuItem(
                                    label = stringResource(id = R.string.delete_schedule_item),
                                    onSelect = {
                                        //TODO: ask to want to delete attendance records too?
                                        if (classDetail.scheduleId != null) {
                                            DBOps.instance.deleteScheduleWithId(classDetail.scheduleId)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = context.getString(R.string.deleted_schedule_item),
                                                    withDismissAction = true
                                                )
                                            }
                                        }
                                        showPopup = false
                                    },
                                )
                            }
                        }
                    }
                )
            }
        }
    }
    if (scheduleToAddClassOn != null) {
        BaseDialog(
            onDismissRequest = { scheduleToAddClassOn = null },
            dialogPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Select date to add class on ${scheduleToAddClassOn!!.dayOfWeek.name} from ${
                    scheduleToAddClassOn!!.startTime.format(
                        timeFormatter
                    )
                } to ${scheduleToAddClassOn!!.endTime.format(timeFormatter)}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            val epochDay0 = remember {
                LocalDate.ofEpochDay(0)
            }
            val weeksSinceEpoch = remember {
                weeksSinceEpoch().toInt()
            }
            val pagerState = rememberPagerState(
                initialPage = weeksSinceEpoch - 1,
                pageCount = { weeksSinceEpoch })
            var classStatus by remember {
                mutableStateOf(CourseClassStatus.Unset)
            }
            VerticalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                state = pagerState,
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 32.dp),
            ) { pageNum ->
                val offSetNormalized = pagerState.pageOffsetCoerced(pageNum)
                val scaleFactor = calculateScale(pagerState, pageNum)
                Text(
                    text = epochDay0.plusWeeks((pageNum + 1).toLong()).format(dateFormatter),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize.times(scaleFactor)
                    ),
                    color = run {
                        val b = MaterialTheme.colorScheme.primary
                        val a = MaterialTheme.colorScheme.onSurface
                        Color.Black.copy(
                            red = (offSetNormalized * a.red + (1 - offSetNormalized) * b.red) / a.colorSpace.getMaxValue(
                                1
                            ),
                            green = (offSetNormalized * a.green + (1 - offSetNormalized) * b.green) / a.colorSpace.getMaxValue(
                                2
                            ),
                            blue = (offSetNormalized * a.blue + (1 - offSetNormalized) * b.blue) / a.colorSpace.getMaxValue(
                                3
                            )
                        )
                    }
                )
            }
            ClassStatusOptions(classStatus) { classStatus = it }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { scheduleToAddClassOn = null }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                TextButton(onClick = {
                    DBOps.instance.markAttendanceForScheduleClass(
                        scheduleId = scheduleToAddClassOn!!.scheduleId!!,
                        classStatus = classStatus,
                        attendanceId = 0,//TODO
                        date = epochDay0.plusWeeks(pagerState.currentPage.toLong() + 1)
                            .with(
                                ChronoField.DAY_OF_WEEK,
                                scheduleToAddClassOn!!.dayOfWeek.value.toLong()
                            )
                    )
                    scheduleToAddClassOn = null
                }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            }
        }
    }
    if (showAddExtraSheet) {
        AddExtraBottomSheet(
            courseName = courseDetails.courseName,
            onDismissRequest = { showAddExtraSheet = false },
            onCreateExtraClass = { timings ->
                onCreateExtraClass(timings)
                scope.launch {
                    snackbarHostState.showSnackbar("Extra class created", withDismissAction = true)
                }
            }
        )
    }
    if (showDeleteDialog) {
        DeleteCourseDialog(
            courseName = courseDetails.courseName,
            onDismissRequest = { showDeleteDialog = false },
            onDeleteCourse = {
                onDeleteCourse(courseDetails.courseId)
            }
        )
    }
}

@Composable
fun DeleteCourseDialog(
    onDismissRequest: () -> Unit,
    courseName: String,
    onDeleteCourse: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(
                    id = R.string.delete_course_dialog_title,
                    courseName
                )
            )
        },
        body = {
            Text(text = stringResource(id = R.string.delete_course_dialog_description))
        },
        buttonBar = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    onDeleteCourse()
                    onDismissRequest()
                }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddExtraBottomSheet(
    courseName: String,
    onDismissRequest: () -> Unit,
    onCreateExtraClass: (ExtraClassTimings) -> Unit
) {
    var state by rememberSaveable {
        mutableStateOf(ExtraClassTimings.defaultTimeAdjusted())
    }
    BaseDialog(
        onDismissRequest = onDismissRequest,
        dialogPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = "Select date, start time and end time for the new extra class for $courseName",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        val tabs = stringArrayResource(id = R.array.add_extra_class_bottom_sheet_tabs)
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        val scope = rememberCoroutineScope()

        Tabs {
            tabs.forEachIndexed { index, tabName ->
                TabItem(
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = tabName,
                    selected = pagerState.currentPage == index
                )
            }
        }
        var pagerMinSize by remember {
            mutableIntStateOf(0)
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.heightIn(min = with(LocalDensity.current) { pagerMinSize.toDp() })
        ) { page ->
            when (page) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { pagerMinSize = maxOf(pagerMinSize, it.height) },
                        contentAlignment = Alignment.Center
                    ) {
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = state.date.atStartOfDay().toInstant(
                                ZoneOffset.UTC
                            ).toEpochMilli()
                        )
                        LaunchedEffect(datePickerState.selectedDateMillis) {
                            val millis = datePickerState.selectedDateMillis
                            if (millis != null) {
                                state = state.copy(
                                    date = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneOffset.systemDefault())
                                        .toLocalDate()
                                )
                            }
                        }
                        DatePicker(state = datePickerState)

                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { pagerMinSize = maxOf(pagerMinSize, it.height) },
                        contentAlignment = Alignment.Center
                    ) {
                        val timePickerState = rememberTimePickerState(
                            initialHour = if (page == 1) state.startTime.hour else state.endTime.hour,
                            initialMinute = if (page == 1) state.startTime.minute else state.endTime.minute
                        )
                        val context = LocalContext.current
                        LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                            state = if (page == 1) {
                                val newStart = state.startTime.withHour(timePickerState.hour)
                                    .withMinute(timePickerState.minute)
                                state.copy(
                                    startTime = newStart,
                                    endTime = newStart.plusHours(1)
                                )
                            } else {
                                val newEnd = state.endTime.withHour(timePickerState.hour)
                                    .withMinute(timePickerState.minute)
                                if (newEnd > state.startTime) {
                                    state.copy(
                                        endTime = newEnd
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.err_end_time_should_be_after_start_time),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    state
                                }
                            }
                        }
                        TimePicker(state = timePickerState)
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
            TextButton(onClick = {
                onCreateExtraClass(state)
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

fun weeksSinceEpoch(): Long {
    val now = LocalDate.now()
    val epoch = LocalDate.ofEpochDay(0)
    return ChronoUnit.WEEKS.between(epoch, now)
}


@OptIn(ExperimentalFoundationApi::class)
fun PagerState.pageOffsetCoerced(page: Int) =
    ((currentPage - page) + currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)

@OptIn(ExperimentalFoundationApi::class)
fun calculateScale(pagerState: PagerState, page: Int): Float {
    val distanceFromCenter = pagerState.pageOffsetCoerced(page)
    val offset = 0.4f // Adjust for desired scale difference
    return 1f - (distanceFromCenter * offset)
}
