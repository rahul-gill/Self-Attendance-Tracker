package com.github.rahul_gill.attendance

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.ui.compose.comps.AddClassBottomSheet
import com.github.rahul_gill.attendance.ui.compose.comps.AttendanceAppTheme
import com.github.rahul_gill.attendance.ui.compose.comps.ColorSchemeType
import com.github.rahul_gill.attendance.ui.compose.screens.CreateCourseScreen
import com.github.rahul_gill.attendance.ui.compose.screens.OverallCourseItem
import com.github.rahul_gill.attendance.ui.compose.screens.OverallCoursesScreen
import com.github.rahul_gill.attendance.ui.compose.screens.SettingsScreen
import com.github.rahul_gill.attendance.ui.compose.screens.TodayClassItem

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        //setContentView(R.layout.activity_main)

        setContent {
            val followSystemColor = PreferenceManager.followSystemColors.asState()
            val seedColor = PreferenceManager.colorSchemeSeed.asState()
            val theme = PreferenceManager.themeConfig.asState()
            val darkThemeType = PreferenceManager.darkThemeType.asState()
            AttendanceAppTheme(
                colorSchemeType = if (followSystemColor.value) ColorSchemeType.Dynamic else ColorSchemeType.WithSeed(
                    seedColor.value
                ),
                themeConfig = theme.value,
                darkThemeType = darkThemeType.value
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OverallCoursesScreen(
                        onCreateCourse = {},
                        goToCourseDetails = {}
                    )
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
