package com.arindam.camerax

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arindam.camerax.ui.settings.SettingsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Instrumented Compose UI test verifying Settings when launched in Video Mode. */
@RunWith(AndroidJUnit4::class)
class SettingsVideoModeInstrumentedTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun videoModeLaunchShowsPhotoAspectDisabled() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            SettingsActivity::class.java
        ).apply {
            putExtra(SettingsActivity.EXTRA_IS_VIDEO_MODE, true)
        }
        ActivityScenario.launch<SettingsActivity>(intent).use {
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Photo aspect").assertIsDisplayed()
            composeTestRule.onNodeWithText("Fixed to 16:9 for video").assertIsDisplayed()
        }
    }
}
