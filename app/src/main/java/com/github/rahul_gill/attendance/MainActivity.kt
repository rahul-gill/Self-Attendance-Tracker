package com.github.rahul_gill.attendance

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.ui.compose.RootNavHost
import com.github.rahul_gill.attendance.ui.compose.comps.AttendanceAppTheme
import com.github.rahul_gill.attendance.ui.compose.comps.ColorSchemeType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //WindowCompat.setDecorFitsSystemWindows(window, false)
        //setContentView(R.layout.activity_main)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )
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
                    RootNavHost()
                }
            }
        }
    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
//    }
}
