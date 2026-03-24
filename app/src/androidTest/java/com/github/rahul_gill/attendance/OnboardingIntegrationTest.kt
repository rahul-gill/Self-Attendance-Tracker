package com.github.rahul_gill.attendance

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.UiObjectNotFoundException
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.ui.RootNavHost
import com.github.rahul_gill.attendance.ui.comps.AttendanceAppTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
class OnboardingIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Reset onboarding preference before each test
        PreferenceManager.onboardingCompleted.setValue(false)
        PreferenceManager.notificationsEnabled.setValue(true) // default
    }

    @Test
    fun onboarding_is_shown_on_first_run() {
        composeTestRule.setContent {
            AttendanceAppTheme {
                RootNavHost()
            }
        }

        // Check if onboarding elements are displayed
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val welcomeText = context.getString(R.string.welcome_to_attendance_tracker)
        composeTestRule.onNodeWithText(welcomeText).assertIsDisplayed()
    }

    @Test
    fun skipping_onboarding_updates_preference_and_navigates_to_main() {
        composeTestRule.setContent {
            AttendanceAppTheme {
                RootNavHost()
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val skipText = context.getString(R.string.skip)
        val todayClassesText = context.getString(R.string.today_classes)

        // Perform skip
        composeTestRule.onNodeWithText(skipText).performClick()

        // Verify preference update
        composeTestRule.waitUntil(20000) {
            PreferenceManager.onboardingCompleted.value
        }
        
        assertTrue(PreferenceManager.onboardingCompleted.value)
        assertFalse(PreferenceManager.notificationsEnabled.value)

        // Verify navigation to MainScreen
        composeTestRule.onNodeWithText(todayClassesText).assertIsDisplayed()
    }

    @Test
    fun enabling_notifications_in_onboarding_handles_permission_and_completes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Revoke permission specifically for this test to ensure the dialog shows up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "pm revoke ${context.packageName} android.permission.POST_NOTIFICATIONS"
            )
        }

        composeTestRule.setContent {
            AttendanceAppTheme {
                RootNavHost()
            }
        }

        val enableText = context.getString(R.string.enable_notifications)
        val todayClassesText = context.getString(R.string.today_classes)
        
        composeTestRule.onNodeWithText(enableText).performClick()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val allowPermissions = device.findObject(UiSelector().text("Allow"))
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click()
                } catch (e: UiObjectNotFoundException) {
                    Timber.e(e, "There is no permissions dialog to interact with")
                }
            }
        }

        // Verify onboarding is marked as completed
        composeTestRule.waitUntil(20000) {
            PreferenceManager.onboardingCompleted.value
        }
        
        assertTrue(PreferenceManager.onboardingCompleted.value)
        
        // Verify navigation to MainScreen
        composeTestRule.onNodeWithText(todayClassesText).assertIsDisplayed()
    }

    @Test
    fun onboarding_is_not_shown_if_already_completed() {
        PreferenceManager.onboardingCompleted.setValue(true)
        
        composeTestRule.setContent {
            AttendanceAppTheme {
                RootNavHost()
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val todayClassesText = context.getString(R.string.today_classes)
        composeTestRule.onNodeWithText(todayClassesText).assertIsDisplayed()
        
        val welcomeText = context.getString(R.string.welcome_to_attendance_tracker)
        composeTestRule.onNodeWithText(welcomeText).assertDoesNotExist()
    }
}
