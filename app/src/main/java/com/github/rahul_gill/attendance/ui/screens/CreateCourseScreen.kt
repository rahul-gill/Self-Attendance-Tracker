package com.github.rahul_gill.attendance.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.DBOps
import com.github.rahul_gill.attendance.ui.comps.AddClassBottomSheet
import com.github.rahul_gill.attendance.db.ClassDetail
import com.github.rahul_gill.attendance.util.timeFormatter
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateCourseScreen(
    onGoBack: () -> Unit,
    onSave: (courseName: String, requiredPercentage: Int, classes: List<ClassDetail>) -> Unit = { courseName, percentage, classes ->
        DBOps.instance.createCourse(
            name = courseName,
            requiredAttendancePercentage = percentage.toDouble(),
            schedule = classes
        )
    }
) {
    var courseName by rememberSaveable {
        mutableStateOf("")
    }
    var requiredAttendancePercentage by rememberSaveable {
        mutableIntStateOf(75)
    }
    val classesForTheCourse = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf<ClassDetail>()
    }

    var showAddClassBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }
    var classToUpdateIndex: Int? by rememberSaveable {
        mutableStateOf(null)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = {
                Text(
                    text = stringResource(id = R.string.create_a_course),
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onClickSave@{
                    if (courseName.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.error_course_name_blank),
                                withDismissAction = true
                            )
                        }
                        return@onClickSave
                    }
                    if (classesForTheCourse.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.error_no_classes_for_course),
                                withDismissAction = true
                            )
                        }
                        return@onClickSave
                    }
                    onSave(courseName, requiredAttendancePercentage, classesForTheCourse.toList())
                    onGoBack()
                },
                modifier = Modifier.imePadding()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_save_24),
                    contentDescription = null
                )
                Text(text = stringResource(id = R.string.save))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                maxLines = 1,
                trailingIcon = {
                    if (courseName.isNotBlank()) {
                        IconButton(onClick = { courseName = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(id = R.string.clear_text)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = stringResource(id = R.string.course_name))
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(
                    id = R.string.required_attendance_text,
                    requiredAttendancePercentage
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = requiredAttendancePercentage.toFloat(),
                onValueChange = { requiredAttendancePercentage = it.toInt() },
                steps = 100,
                valueRange = 1f..100f
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.select_schedule_of_classes),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                classesForTheCourse.forEachIndexed { index, classDetail ->
                    ClassChip(
                        label = {
                            Text(
                                text = stringResource(
                                    id = R.string.schedule_class_weekday_and_start_end_time,
                                    classDetail.dayOfWeek.name,
                                    classDetail.startTime.format(timeFormatter),
                                    classDetail.endTime.format(timeFormatter)
                                )
                            )
                        },
                        onClick = {
                            classToUpdateIndex = index
                            showAddClassBottomSheet = true
                        },
                        onCloseClick = {
                            classesForTheCourse.removeAt(index)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = { showAddClassBottomSheet = true }) {
                Text(text = stringResource(id = R.string.add_class))
            }
        }
    }

    if (showAddClassBottomSheet) {
        AddClassBottomSheet(
            initialState = classToUpdateIndex?.run { classesForTheCourse[this] },
            onDismissRequest = { showAddClassBottomSheet = false },
            onCreateClass = { params ->
                classToUpdateIndex?.let {
                    classesForTheCourse[it] = params
                    classToUpdateIndex = null
                } ?: classesForTheCourse.add(params)
            }
        )
    }
}


@Composable
private fun ClassChip(
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(onClick = onClick, modifier = modifier) {
        Row(
            Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            label()
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onCloseClick) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.remove_class)
                )
            }
        }
    }
}


