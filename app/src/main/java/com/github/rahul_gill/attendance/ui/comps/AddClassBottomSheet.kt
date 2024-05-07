package com.github.rahul_gill.attendance.ui.comps

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.ClassDetail
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale


private fun defaultClassDetailWithTimeAdjusted(): ClassDetail {
    val start = LocalTime.now().withMinute(0)
    return ClassDetail(
        startTime = start,
        endTime = start.plusHours(1)
    )
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddClassBottomSheet(
    initialState: ClassDetail? = null,
    onDismissRequest: () -> Unit,
    onCreateClass: (ClassDetail) -> Unit
) {
    var dayOfWeekSelected by rememberSaveable {
        mutableStateOf(initialState?.dayOfWeek ?: LocalDate.now().dayOfWeek)
    }
    val context = LocalContext.current
    var startTimeState = rememberSaveable(
        saver = TimePickerState.Saver()
    ) {
        TimePickerState(
            initialHour = initialState?.startTime?.hour ?: 0,
            initialMinute = initialState?.startTime?.minute ?: 0,
            is24Hour = DateFormat.is24HourFormat(context),
        )
    }
    var endTimeState by rememberSaveable(
        saver = Saver(
            save = {
                listOf(
                    it.value.hour,
                    it.value.minute,
                    it.value.is24hour
                )
            },
            restore = { value ->
                mutableStateOf(
                    TimePickerState(
                        initialHour = value[0] as Int,
                        initialMinute = value[1] as Int,
                        is24Hour = value[2] as Boolean
                    )
                )
            }
        )
    ) {
        mutableStateOf(
            TimePickerState(
                initialHour = initialState?.endTime?.hour ?: 0,
                initialMinute = initialState?.endTime?.minute ?: 0,
                is24Hour = DateFormat.is24HourFormat(context),
            )
        )
    }
    LaunchedEffect(startTimeState.hour, startTimeState.minute) {
        endTimeState = TimePickerState(
            initialHour = (startTimeState.hour + 1) % 24,
            initialMinute = startTimeState.minute,
            is24Hour = DateFormat.is24HourFormat(context),
        )
    }
    LaunchedEffect(endTimeState.hour, endTimeState.minute) {
        val newEnd = LocalTime.of(endTimeState.hour, endTimeState.minute)
        val start = LocalTime.of(startTimeState.hour, startTimeState.minute)
        if (newEnd <= start) {
            Toast.makeText(
                context,
                context.getString(R.string.err_end_time_should_be_after_start_time),
                Toast.LENGTH_SHORT
            ).show()
            endTimeState = TimePickerState(
                initialHour = (startTimeState.hour + 1) % 24,
                initialMinute = startTimeState.minute,
                is24Hour = DateFormat.is24HourFormat(context),
            )
        }
    }
    BaseDialog(
        onDismissRequest = onDismissRequest,
        dialogPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = stringResource(R.string.select_weekday_start_time_and_end_time_for_the_new_class),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        val tabs = stringArrayResource(id = R.array.add_schedule_class_bottom_sheet_tabs)
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
                    Column(
                        Modifier
                            .selectableGroup()
                            .onSizeChanged { pagerMinSize = maxOf(pagerMinSize, it.height) }) {
                        DayOfWeek.entries.forEach { dayOfWeek ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (dayOfWeekSelected == dayOfWeek),
                                        onClick = { dayOfWeekSelected = dayOfWeek },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (dayOfWeek == dayOfWeekSelected),
                                    onClick = null // null recommended for accessibility with screenreaders
                                )
                                Text(
                                    text = dayOfWeek.getDisplayName(
                                        TextStyle.FULL,
                                        Locale.getDefault()
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
                1 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { pagerMinSize = maxOf(pagerMinSize, it.height) },
                        contentAlignment = Alignment.Center
                    ) {
                        TimePicker(state = startTimeState)
                    }
                }
                2 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { pagerMinSize = maxOf(pagerMinSize, it.height) },
                        contentAlignment = Alignment.Center
                    ) {
                        TimePicker(state = endTimeState)
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
            TextButton(onClick = {
                onCreateClass(
                    ClassDetail(
                        dayOfWeek = dayOfWeekSelected,
                        startTime = LocalTime.of(startTimeState.hour, startTimeState.minute),
                        endTime = LocalTime.of(endTimeState.hour, endTimeState.minute)
                    )
                )
                onDismissRequest()
            }, modifier = Modifier.testTag("sheet_add_class_button")
            ) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}