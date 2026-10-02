package com.github.rahul_gill.attendance.ui.comps

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.notification.TestAttendanceApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S], application = TestAttendanceApp::class)
class ThemeConfigTest {

    private lateinit var context: Context
    private lateinit var uiModeManager: UiModeManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        uiModeManager = context.getSystemService(UiModeManager::class.java)
    }

    private fun appThemeIsLight(): Boolean {
        val attrs = ContextThemeWrapper(context, R.style.AppTheme)
            .obtainStyledAttributes(intArrayOf(android.R.attr.isLightTheme))
        try {
            assertTrue("AppTheme should define isLightTheme", attrs.hasValue(0))
            return attrs.getBoolean(0, true)
        } finally {
            attrs.recycle()
        }
    }

    @Test
    @Config(qualifiers = "notnight")
    fun `app theme is light in day mode`() {
        assertTrue(appThemeIsLight())
    }

    // The splash screen is drawn from AppTheme, so it must be dark in night mode
    @Test
    @Config(qualifiers = "night")
    fun `app theme is dark in night mode`() {
        assertEquals(false, appThemeIsLight())
    }

    @Test
    fun `theme setting is persisted as the app night mode`() {
        val expected = mapOf(
            ThemeConfig.Dark to UiModeManager.MODE_NIGHT_YES,
            ThemeConfig.Light to UiModeManager.MODE_NIGHT_NO,
            ThemeConfig.FollowSystem to UiModeManager.MODE_NIGHT_AUTO,
        )
        for ((themeConfig, nightMode) in expected) {
            applyThemeConfigToSystem(context, themeConfig)
            assertEquals(
                "night mode for $themeConfig",
                nightMode,
                shadowOf(uiModeManager).applicationNightMode
            )
        }
    }

    // setApplicationNightMode doesn't exist before Android 12 and would crash
    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `app night mode is left alone before Android 12`() {
        applyThemeConfigToSystem(context, ThemeConfig.Dark)

        assertEquals(UiModeManager.MODE_NIGHT_AUTO, shadowOf(uiModeManager).applicationNightMode)
    }
}
