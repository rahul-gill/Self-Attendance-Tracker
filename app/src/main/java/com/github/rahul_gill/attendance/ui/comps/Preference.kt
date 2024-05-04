package com.github.rahul_gill.attendance.ui.comps


//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.rahul_gill.attendance.R


private val GroupHeaderStartPadding = 16.dp
private const val GroupHeaderFontSizeMultiplier = 0.85f
private val PrefItemMinHeight = 72.dp

@Composable
fun PreferenceGroupHeader(
    modifier: Modifier = Modifier,
    title: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        Modifier
            .padding(start = GroupHeaderStartPadding)
            .fillMaxWidth()
            .then(modifier),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            title,
            color = color,
            fontSize = LocalTextStyle.current.fontSize.times(GroupHeaderFontSizeMultiplier),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun GenericPreference(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    summary: String? = null,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    placeholderSpaceForLeadingIcon: Boolean = true
) {
    Row(
        modifier = Modifier

            .clip(RoundedCornerShape(20))
            .clickable(
                onClick = onClick ?: {}
            )
            .heightIn(min = PrefItemMinHeight)
            .fillMaxWidth()
            .padding(contentPadding)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon == null && placeholderSpaceForLeadingIcon) {
            Box(Modifier.minimumInteractiveComponentSize()) {}
        } else if (leadingIcon != null) {
            Box(
                modifier = Modifier.minimumInteractiveComponentSize().align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                leadingIcon()
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = title,
                fontSize = 19.sp
            )
            if (summary != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary,
                    fontSize = 15.sp,
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }
        if (trailingContent != null) {
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )
            trailingContent()
        }
    }
}


@Composable
fun <T> ListPreference(
    title: String,
    items: List<T>,
    selectedItemIndex: Int,
    onItemSelection: (Int) -> Unit,
    itemToDescription: @Composable (Int) -> String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholderForIcon: Boolean = true,
    selectItemOnClick: Boolean = true
) {
    val isShowingSelectionDialog = remember {
        mutableStateOf(false)
    }
    GenericPreference(
        title = title,
        leadingIcon = leadingIcon,
        summary = itemToDescription(selectedItemIndex),
        modifier = modifier,
        placeholderSpaceForLeadingIcon = placeholderForIcon,
        onClick = {
            isShowingSelectionDialog.value = true
        },
    )
    if (isShowingSelectionDialog.value) {
        var dialogSelectedItemIndex by remember {
            mutableIntStateOf(selectedItemIndex)
        }
        AlertDialog(
            onDismissRequest = {
                isShowingSelectionDialog.value = false
            },
            title = {
                Text(text = title)
            },
            body = {
                Column(
                    Modifier
                        .selectableGroup()
                        .verticalScroll(rememberScrollState())
                ) {
                    items.forEachIndexed { index, choice ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (index == dialogSelectedItemIndex),
                                    onClick = {
                                        dialogSelectedItemIndex = index
                                        if (selectItemOnClick) {
                                            onItemSelection(index)
                                        }
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (index == dialogSelectedItemIndex),
                                onClick = null
                            )
                            Text(
                                text = itemToDescription(index),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            buttonBar = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { isShowingSelectionDialog.value = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        if (!selectItemOnClick) {
                            onItemSelection(dialogSelectedItemIndex)
                        }
                        isShowingSelectionDialog.value = false
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        )
    }
}

@Composable
fun SwitchPreference(
    title: String,
    isChecked: Boolean,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    isEnabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholderForIcon: Boolean = true,
) {
    println(" 4523423 SwitchPreference isChecked: $isChecked")
    GenericPreference(
        title = title,
        onClick = {
            if (onCheckedChange != null) {

                println(" 4523423 SwitchPreference set isChecked: ${!isChecked}")
                onCheckedChange(!isChecked)
            }
        },
        modifier = modifier,
        summary = summary,
        leadingIcon = leadingIcon,
        placeholderSpaceForLeadingIcon = placeholderForIcon,
        trailingContent = {

            println("4523423 calling Switch isChecked: $isChecked")
            Switch(
                modifier = modifier,
                checked = isChecked,
                onCheckedChange = null,
                enabled = isEnabled
            )
        })
}

