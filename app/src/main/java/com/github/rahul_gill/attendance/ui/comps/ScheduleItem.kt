package com.github.rahul_gill.attendance.ui.comps

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.ClassDetail
import com.github.rahul_gill.attendance.util.timeFormatter

@Composable
fun ScheduleItem(
    item: ClassDetail,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    popupContent: (@Composable () -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .then(modifier)
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = item.dayOfWeek.name)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(
                    id = R.string.time_range,
                    item.startTime.format(timeFormatter),
                    item.endTime.format(timeFormatter)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (onCloseClick != null) {
                IconButton(onClick = onCloseClick) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.remove_class)
                    )
                }
            } else {
                Box(Modifier.height(48.dp)) {}
            }
            if (popupContent != null) {
                popupContent()
            }
        }
        if(!item.includedInSchedule){
            Surface(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(25),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    text = stringResource(id = R.string.excluded_from_schedule),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}