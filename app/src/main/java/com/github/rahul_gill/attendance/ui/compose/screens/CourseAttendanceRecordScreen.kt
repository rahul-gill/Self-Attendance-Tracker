package com.github.rahul_gill.attendance.ui.compose.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.CourseDetailsOverallItem
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.db.TodayCourseItem
import com.github.rahul_gill.attendance.ui.compose.comps.AlertDialog
import com.github.rahul_gill.attendance.ui.compose.comps.SetClassStatusSheet
import com.github.rahul_gill.attendance.util.dateFormatter
import com.github.rahul_gill.attendance.util.timeFormatter
import com.github.rahulgill.attendance.MarkedAttendancesForCourse
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoField

sealed class HeaderListItem {
    class Header(val value: LocalDate) : HeaderListItem()
    class Item(val value: MarkedAttendancesForCourse) : HeaderListItem()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseAttendanceRecordScreen(
    courseDetails: CourseDetailsOverallItem = CourseDetailsOverallItem(
        courseId = 1,
        courseName = "Mathematics",
        currentAttendancePercentage = 90.0,
        presents = 10,
        absents = 1,
        cancels = 0,
        requiredAttendance = 75.0
    ),
    records: List<MarkedAttendancesForCourse> = listOf(),
    onGoBack: () -> Unit = {},
    onSetClassStatus: (item: TodayCourseItem, newStatus: CourseClassStatus) -> Unit,
    onDeleteRecord: (item: TodayCourseItem) -> Unit
) {
    var setClassSheetItem: TodayCourseItem? by remember {
        mutableStateOf(null)
    }
    var itemToBeDeleted: TodayCourseItem? by remember {
        mutableStateOf(null)
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
                    text = stringResource(
                        R.string.course_x_attendance_records,
                        courseDetails.courseName
                    ),
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
        Box(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize()
        ) {
            val state = rememberLazyListState()
            val shouldHeaderBeVisible = remember {
                mutableStateOf(false)
            }
            LaunchedEffect(state.isScrollInProgress) {
                if (state.isScrollInProgress) {
                    shouldHeaderBeVisible.value = true
                } else {
                    delay(2000)
                    shouldHeaderBeVisible.value = false
                }
            }
            if (records.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No records")
                }
            } else {
                val lists = remember(records) {
                    val finalList = mutableListOf<HeaderListItem>()
                    var prevDateTime: LocalDate? = null
                    records
                        .sortedWith { first, second ->
                            first.date.compareTo(second.date)
                        }.forEach { item ->
                            if (prevDateTime == null || prevDateTime!! != item.date) {
                                finalList.add(HeaderListItem.Header(item.date))
                            }
                            finalList.add(HeaderListItem.Item(item))
                            prevDateTime = item.date
                        }
                    finalList
                }
                LazyColumn(
                    state = state,
                    modifier = Modifier.testTag("dashboard:transaction_list"),
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp,
                        alignment = Alignment.Top
                    )
                ) {
                    itemsIndexed(
                        items = lists,
                        contentType = { _, item ->
                            when (item) {
                                is HeaderListItem.Header -> "Header"
                                is HeaderListItem.Item -> "Item"
                            }
                        }
                    ) { index, item ->
                        when (item) {
                            is HeaderListItem.Header -> {
                                Text(
                                    text = remember(item.value) {
                                        dateFormatter.format(item.value)
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )

                            }

                            is HeaderListItem.Item -> {
                                val txn = item.value
                                AttendanceItem(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .fillMaxWidth(),
                                    item = txn,
                                    onClick = {
                                        setClassSheetItem =
                                            TodayCourseItem.fromMarkedAttendancesForCourse(
                                                item = txn,
                                                courseName = courseDetails.courseName
                                            )
                                    }
                                )

                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(100.dp))
                    }
                }

                val header = remember(lists) {
                    derivedStateOf {
                        when (val item = lists.getOrNull(state.firstVisibleItemIndex)) {
                            is HeaderListItem.Header -> {
                                (lists[state.firstVisibleItemIndex + 1] as HeaderListItem.Item).value.date.format(
                                    dateFormatter
                                )
                            }

                            is HeaderListItem.Item -> {
                                item.value.date.format(dateFormatter)
                            }

                            null -> "Err"
                        }
                    }
                }
                AnimatedVisibility(
                    visible = shouldHeaderBeVisible.value,
                    enter = fadeIn() + slideInVertically(),
                    exit = slideOutVertically() + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(50)
                            )
                            .clip(RoundedCornerShape(50))
                            .animateContentSize(),
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = header.value!!,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
    if (setClassSheetItem != null) {
        SetClassStatusSheet(
            todayCourseItem = setClassSheetItem!!,
            onDismissRequest = { setClassSheetItem = null },
            setClasStatus = { onSetClassStatus(setClassSheetItem!!, it) },
            onDeleteItem = {
                itemToBeDeleted = setClassSheetItem!!
            }
        )
    }
    if (itemToBeDeleted != null) {
        ConfirmRecordDeletionDialog(
            todayCourseItem = itemToBeDeleted!!,
            onDismissRequest = { itemToBeDeleted = null },
            onDeleteItem = {
                onDeleteRecord(itemToBeDeleted!!)
            }
        )
    }
}

@Composable
fun ConfirmRecordDeletionDialog(
    todayCourseItem: TodayCourseItem,
    onDismissRequest: () -> Unit,
    onDeleteItem: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Confirm deleting this attendance record on ${
                    todayCourseItem.date!!.format(
                        dateFormatter
                    )
                } for ${todayCourseItem.courseName}"
            )
        },
        buttonBar = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
            TextButton(onClick = {
                onDeleteItem()
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    )
}

@Composable
fun AttendanceItem(modifier: Modifier, item: MarkedAttendancesForCourse, onClick: () -> Unit) {
    OutlinedCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.padding(8.dp),
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
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(
                    id = if (item.isExtraCLass == 0L) R.string.scheduled_class
                    else R.string.extra_class
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
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
