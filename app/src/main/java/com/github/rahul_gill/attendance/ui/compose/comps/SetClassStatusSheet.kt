package com.github.rahul_gill.attendance.ui.compose.comps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.db.TodayCourseItem
import com.github.rahul_gill.attendance.util.dateFormatter
import com.github.rahul_gill.attendance.util.timeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetClassStatusSheet(
    todayCourseItem: TodayCourseItem,
    onDismissRequest: () -> Unit,
    setClasStatus: (CourseClassStatus) -> Unit
) {
    var newStatus by remember {
        mutableStateOf(todayCourseItem.classStatus)
    }
    BaseDialog(
        onDismissRequest = onDismissRequest,
        contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        //sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Text(text = stringResource(id = R.string.attendance_status_setter_info,
            todayCourseItem.courseName,
            todayCourseItem.startTime.format(timeFormatter),
            todayCourseItem.endTime.format(timeFormatter),
            if (todayCourseItem.isExtraClass)
                stringResource(R.string.attendance_status_setter_info_extra_class)
            else "",
            if (todayCourseItem.date != null)
                stringResource(R.string.attendance_status_setter_info_on_date,
                    todayCourseItem.date.format(dateFormatter))
            else ""
        ), style = MaterialTheme.typography.titleLarge, )//modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Column(Modifier.selectableGroup()) {
            CourseClassStatus.entries.forEach { dayOfWeek ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (dayOfWeek == newStatus),
                            onClick = { newStatus = dayOfWeek },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (dayOfWeek == newStatus),
                        onClick = null // null recommended for accessibility with screenreaders
                    )
                    Text(
                        text = stringResource(
                            id = when (dayOfWeek) {
                                CourseClassStatus.Present -> R.string.present
                                CourseClassStatus.Absent -> R.string.absent
                                CourseClassStatus.Cancelled -> R.string.cancelled
                                CourseClassStatus.Unset -> R.string.not_set
                            }
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
            TextButton(onClick = {
                setClasStatus(newStatus)
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}