package com.github.rahul_gill.attendance.ui.comps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.github.rahul_gill.attendance.db.AttendanceRecordHybrid
import com.github.rahul_gill.attendance.db.CourseClassStatus
import com.github.rahul_gill.attendance.util.dateFormatter
import com.github.rahul_gill.attendance.util.timeFormatter

@Composable
fun SetClassStatusSheet(
    todayCourseItem: AttendanceRecordHybrid,
    onDismissRequest: () -> Unit,
    setClasStatus: (CourseClassStatus) -> Unit,
    onDeleteItem: (() -> Unit)? = null
) {
    var newStatus by remember {
        mutableStateOf(todayCourseItem.classStatus)
    }
    BaseDialog(
        onDismissRequest = onDismissRequest,
        dialogPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = stringResource(
                id = R.string.attendance_status_setter_info,
                todayCourseItem.courseName,
                todayCourseItem.startTime.format(timeFormatter),
                todayCourseItem.endTime.format(timeFormatter),
                if (todayCourseItem is AttendanceRecordHybrid.ExtraClass)
                    stringResource(R.string.attendance_status_setter_info_extra_class)
                else "",
                stringResource(
                    R.string.attendance_status_setter_info_on_date,
                    todayCourseItem.date.format(dateFormatter)
                )
            ),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        ClassStatusOptions(newStatus) { newStatus = it }
        Row(modifier = Modifier.fillMaxWidth()) {
            if (onDeleteItem != null) {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        onDeleteItem()
                        onDismissRequest()
                    }
                ) {
                    Text(text = stringResource(id = R.string.delete_record))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
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

@Composable
fun ClassStatusOptions(
    initialStatus: CourseClassStatus,
    setClassStatus: (CourseClassStatus) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        CourseClassStatus.entries.forEach { dayOfWeek ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (dayOfWeek == initialStatus),
                        onClick = { setClassStatus(dayOfWeek) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (dayOfWeek == initialStatus),
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
}