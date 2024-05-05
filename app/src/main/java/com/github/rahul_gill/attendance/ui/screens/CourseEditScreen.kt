package com.github.rahul_gill.attendance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.CourseDetailsOverallItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditScreen(
    courseDetails: CourseDetailsOverallItem,
    onGoBack: () -> Unit,
    onSave: (courseName: String, requiredPercentage: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current


    var newCourseName by rememberSaveable {
        mutableStateOf(courseDetails.courseName)
    }
    var newRequiredAttendancePercentage by rememberSaveable {
        mutableIntStateOf(courseDetails.requiredAttendance.toInt())
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = {
                Text(
                    text = stringResource(
                        id = R.string.edit_course_screen_title,
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onClickSave@{
                    if (newCourseName.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.error_course_name_blank),
                                withDismissAction = true
                            )
                        }
                        return@onClickSave
                    }
                    onSave(newCourseName, newRequiredAttendancePercentage)
                    Toast.makeText(context, R.string.course_updated, Toast.LENGTH_SHORT).show()
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
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = newCourseName,
                onValueChange = { newCourseName = it },
                maxLines = 1,
                trailingIcon = {
                    if (newCourseName.isNotBlank()) {
                        IconButton(onClick = { newCourseName = "" }) {
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
                    newRequiredAttendancePercentage
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = newRequiredAttendancePercentage.toFloat(),
                onValueChange = { newRequiredAttendancePercentage = it.toInt() },
                steps = 100,
                valueRange = 1f..100f
            )
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}