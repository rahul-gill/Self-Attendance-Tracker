package com.github.rahul_gill.attendance.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.prefs.UnsetClassesBehavior
import com.github.rahul_gill.attendance.ui.comps.AlertDialog
import com.github.rahul_gill.attendance.ui.comps.AttendanceAppTheme
import com.github.rahul_gill.attendance.ui.comps.ColorSchemeType
import com.github.rahul_gill.attendance.ui.comps.DarkThemeType
import com.github.rahul_gill.attendance.ui.comps.GenericPreference
import com.github.rahul_gill.attendance.ui.comps.ListPreference
import com.github.rahul_gill.attendance.ui.comps.PreferenceGroupHeader
import com.github.rahul_gill.attendance.ui.comps.SwitchPreference
import com.github.rahul_gill.attendance.ui.comps.ThemeConfig
import com.github.rahul_gill.attendance.util.Constants
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onGoBack: () -> Unit
) {

    val followSystemColor = PreferenceManager.followSystemColors.asState()
    val seedColor = PreferenceManager.colorSchemeSeed.asState()
    val theme = PreferenceManager.themeConfig.asState()
    val unsetClassBehaviour = PreferenceManager.unsetClassesBehavior.asState()
    val darkThemeType = PreferenceManager.darkThemeType.asState()
    val dateFormatOption = PreferenceManager.defaultDateFormatPref.asState()
    val timeFormatOption = PreferenceManager.defaultTimeFormatPref.asState()
    val defaultHomeTabOption = PreferenceManager.defaultHomeTabPref.asState()


    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onGoBack, modifier = Modifier.testTag("go_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.go_back_screen)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        val screenHeight = LocalConfiguration.current.screenHeightDp
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .heightIn(min = screenHeight.dp)
        ) {
            PreferenceGroupHeader(title = stringResource(id = R.string.look_and_feel))
            Spacer(modifier = Modifier.height(8.dp))

            val themeValues = remember {
                ThemeConfig.entries.toList()
            }
            ListPreference(
                title = stringResource(id = R.string.app_theme),
                items = themeValues,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.baseline_palette_24), contentDescription = null)
                },
                selectedItemIndex = themeValues.indexOf(theme.value),
                onItemSelection = { PreferenceManager.themeConfig.setValue(themeValues[it]) },
                itemToDescription = { themeIndex ->
                    stringResource(
                        id = when (themeValues[themeIndex]) {
                            ThemeConfig.FollowSystem -> R.string.follow_system
                            ThemeConfig.Light -> R.string.light
                            ThemeConfig.Dark -> R.string.dark
                        }
                    )
                }
            )
            //Spacer(modifier = Modifier.height(8.dp))
            SwitchPreference(
                title = stringResource(R.string.pure_black_background),
                isChecked = darkThemeType.value == DarkThemeType.Black,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.baseline_contrast_24), contentDescription = null)
                },
                onCheckedChange = { checked ->
                    PreferenceManager.darkThemeType.setValue(if (checked) DarkThemeType.Black else DarkThemeType.Dark)
                }
            )

            //Spacer(modifier = Modifier.height(8.dp))
            SwitchPreference(
                title = stringResource(R.string.follow_system_colors),
                isChecked = followSystemColor.value,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.baseline_format_color_fill_24), contentDescription = null)
                },
                onCheckedChange = {
                    PreferenceManager.followSystemColors.setValue(it)
                }
            )
            AnimatedVisibility(visible = !followSystemColor.value) {
                //Spacer(modifier = Modifier.height(8.dp))
                val isColorPickerDialogShowing = remember {
                    mutableStateOf(false)
                }
                GenericPreference(
                    title = stringResource(R.string.custom_color_scheme_seed),
                    summary = stringResource(id = R.string.custom_color_scheme_seed_summary),
                    onClick = {
                        isColorPickerDialogShowing.value = true
                    },
                    leadingIcon = {
                        Icon(painterResource(id = R.drawable.baseline_colorize_24), contentDescription = null)
                    },
                    trailingContent = {
                        Surface(
                            modifier = Modifier
                                .background(
                                    color = seedColor.value,
                                    shape = CircleShape
                                )
                                .size(24.dp),
                            color = seedColor.value,
                            shape = CircleShape,
                            content = {}
                        )
                    }
                )

                if (isColorPickerDialogShowing.value) {
                    val pickedColor = remember {
                        mutableStateOf(PreferenceManager.colorSchemeSeed.value)
                    }
                    val colorController = rememberColorPickerController()
                    AttendanceAppTheme(
                        colorSchemeType = if (followSystemColor.value) ColorSchemeType.Dynamic else ColorSchemeType.WithSeed(
                            pickedColor.value
                        ),
                        themeConfig = theme.value,
                        darkThemeType = darkThemeType.value
                    ) {
                        AlertDialog(
                            onDismissRequest = { isColorPickerDialogShowing.value = false },
                            title = {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = stringResource(id = R.string.custom_color_scheme_seed))
                                    AlphaTile(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape),
                                        controller = colorController
                                    )
                                }
                            },
                            body = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    HsvColorPicker(
                                        modifier = Modifier.height(450.dp),
                                        controller = colorController,
                                        initialColor = pickedColor.value,
                                        onColorChanged = { envelope ->
                                            pickedColor.value = envelope.color
                                        }
                                    )
                                }
                            },
                            buttonBar = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = {
                                        isColorPickerDialogShowing.value = false
                                    }) {
                                        Text(text = stringResource(id = R.string.cancel))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        PreferenceManager.colorSchemeSeed.setValue(pickedColor.value)
                                        isColorPickerDialogShowing.value = false
                                    }) {
                                        Text(text = stringResource(id = R.string.ok))
                                    }
                                }
                            }
                        )
                    }
                }
            }

            PreferenceGroupHeader(title = stringResource(id = R.string.behaviour))
            val unsetClassesBehaviorValues = UnsetClassesBehavior.entries.toTypedArray().toList()
            ListPreference(
                title = stringResource(id = R.string.unset_classes_behaviour),
                items = unsetClassesBehaviorValues,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                },
                selectedItemIndex = unsetClassesBehaviorValues.indexOf(unsetClassBehaviour.value),
                onItemSelection = { PreferenceManager.unsetClassesBehavior.setValue(unsetClassesBehaviorValues[it]) },
                itemToDescription = { index ->
                    stringResource(
                        id = when (unsetClassesBehaviorValues[index]) {
                            UnsetClassesBehavior.ConsiderPresent -> R.string.consider_as_presents
                            UnsetClassesBehavior.ConsiderAbsent -> R.string.consider_as_absents
                            UnsetClassesBehavior.None -> R.string.do_nothing
                        }
                    )
                }
            )


            PreferenceGroupHeader(title = stringResource(id = R.string.date_time_formatting))

            val timeFormatOptions = stringArrayResource(id = R.array.time_format_choices).toList()
            ListPreference(
                title = stringResource(id = R.string.time_format),
                items = timeFormatOptions,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.baseline_access_time_24), contentDescription = null)
                },
                selectedItemIndex = timeFormatOptions.indexOf(timeFormatOption.value),
                onItemSelection = { selected -> PreferenceManager.defaultTimeFormatPref.setValue(timeFormatOptions[selected]) },
                itemToDescription = { tabOptionIndex ->
                    DateTimeFormatter.ofPattern(timeFormatOptions[tabOptionIndex]).format(LocalTime.now())
                }
            )
            val dateFormatOptions = stringArrayResource(id = R.array.date_format_choices).toList()
            ListPreference(
                title = stringResource(id = R.string.date_format),
                items = dateFormatOptions,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.baseline_calendar_today_24), contentDescription = null)
                },
                selectedItemIndex = dateFormatOptions.indexOf(dateFormatOption.value),
                onItemSelection = { selected -> PreferenceManager.defaultDateFormatPref.setValue(dateFormatOptions[selected]) },
                itemToDescription = { tabOptionIndex ->
                    DateTimeFormatter.ofPattern(dateFormatOptions[tabOptionIndex]).format(LocalDate.now())
                }
            )

            PreferenceGroupHeader(title = stringResource(id = R.string.other_ui_options))

            val homeTabOptions = stringArrayResource(id = R.array.default_main_pager_tab_entries_values).toList()
            ListPreference(
                title = stringResource(id = R.string.default_home_tab),
                items = homeTabOptions,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.baseline_table_chart_24), contentDescription = null)
                },
                selectedItemIndex = defaultHomeTabOption.value,
                onItemSelection = { selected -> PreferenceManager.defaultHomeTabPref.setValue(selected) },
                itemToDescription = { tabOptionIndex ->
                    homeTabOptions[tabOptionIndex]
                }
            )

            PreferenceGroupHeader(title = stringResource(id = R.string.about))
            val context = LocalContext.current

            GenericPreference(
                title = stringResource(R.string.privacy_policy),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(Constants.PRIVACY_POLICY_LINK)
                    context.startActivity(intent)
                },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.baseline_privacy_tip_24), contentDescription = null)
                },
            )

            GenericPreference(
                title = stringResource(R.string.source_code),
                summary = Constants.GITHUB_APP_LINK,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(Constants.GITHUB_APP_LINK)
                    context.startActivity(intent)
                },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.github), contentDescription = null)
                },
            )

            GenericPreference(
                title = stringResource(R.string.author_info),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(Constants.GITHUB_USER_LINK)
                    context.startActivity(intent)
                },
                leadingIcon = {
                    Icon(painterResource(id = R.drawable.baseline_person_24), contentDescription = null)
                },
            )
        }
    }

}