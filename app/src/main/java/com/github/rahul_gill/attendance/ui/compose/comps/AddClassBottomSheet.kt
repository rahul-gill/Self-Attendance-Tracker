package com.github.rahul_gill.attendance.ui.compose.comps

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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.ui.create.ClassDetail
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale


@Composable
@Preview(showBackground = true)
private fun AddClassBottomSheetPreview() {
    MaterialTheme {
        AddClassBottomSheet(
            initialState = ClassDetail(),
            onDismissRequest = {},
            onCreateClass = { }
        )
    }
}

private fun defaultClassDetailWithTimeAdjusted(): ClassDetail {
    val start = LocalTime.now().withMinute(0)
    return ClassDetail(
        startTime = start,
        endTime = start.plusHours(1)
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddClassBottomSheet(
    initialState: ClassDetail? = null,
    onDismissRequest: () -> Unit,
    onCreateClass: (ClassDetail) -> Unit
) {
    var state by rememberSaveable {
        mutableStateOf(initialState ?: defaultClassDetailWithTimeAdjusted())
    }
    BaseDialog(
        onDismissRequest = onDismissRequest,
        dialogPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = "Select weekday, start time and end time for the new class",
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
                                        selected = (dayOfWeek == state.dayOfWeek),
                                        onClick = { state = state.copy(dayOfWeek = dayOfWeek) },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (dayOfWeek == state.dayOfWeek),
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
                        LaunchedEffect(key1 = timePickerState) {
                            state = if (page == 1) {
                                state.copy(
                                    startTime = state.startTime.withHour(timePickerState.hour)
                                        .withMinute(timePickerState.minute)
                                )
                            } else {
                                state.copy(
                                    endTime = state.endTime.withHour(timePickerState.hour)
                                        .withMinute(timePickerState.minute)
                                )
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
                onCreateClass(state)
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}
