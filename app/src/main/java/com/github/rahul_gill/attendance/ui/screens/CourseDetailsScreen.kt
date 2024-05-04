package com.github.rahul_gill.attendance.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.CourseDetailsOverallItem
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.ui.comps.BaseDialog
import com.github.rahul_gill.attendance.ui.comps.ClassStatusOptions
import com.github.rahul_gill.attendance.ui.comps.PopupMenu
import com.github.rahul_gill.attendance.ui.comps.SelectableMenuItem
import com.github.rahul_gill.attendance.ui.create.ClassDetail
import com.github.rahul_gill.attendance.util.dateFormatter
import com.github.rahul_gill.attendance.util.timeFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview(showBackground = true, showSystemUi = true)
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
    goToClassRecords: () -> Unit = {}
) {
    var scheduleToAddClassOn by remember {
        mutableStateOf<ClassDetail?>(null)
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        topBar = {
            LargeTopAppBar(title = {
                Text(
                    text = courseDetails.courseName,
                )
            }, navigationIcon = {
                IconButton(onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.go_back_screen)
                    )
                }
            }, scrollBehavior = scrollBehavior
            )
        },
    ) { paddings ->
        Column(
            Modifier
                .padding(paddings)
                .padding(horizontal = 16.dp)
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
            TextButton(onClick = goToClassRecords) {
                Text(text = "See attendance records")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.weekly_schedule),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(classes) { classDetail ->
                    var showPopup by remember {
                        mutableStateOf(false)
                    }
                    OutlinedCard(onClick = {
                        showPopup = true
                    }) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = classDetail.dayOfWeek.name)
                            Text(
                                text = stringResource(
                                    id = R.string.time_range,
                                    classDetail.startTime.format(timeFormatter),
                                    classDetail.endTime.format(timeFormatter)
                                )
                            )
                        }
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
                }
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
                        val b =  MaterialTheme.colorScheme.primary
                        val a = MaterialTheme.colorScheme.onSurface
                        Color.Black.copy(
                            red = (offSetNormalized * a.red + (1 - offSetNormalized) * b.red) / a.colorSpace.getMaxValue(1),
                            green = (offSetNormalized * a.green + (1 - offSetNormalized) * b.green) / a.colorSpace.getMaxValue(2),
                            blue = (offSetNormalized * a.blue + (1 - offSetNormalized) * b.blue) / a.colorSpace.getMaxValue(3)
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
                            .with(ChronoField.DAY_OF_WEEK,
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
