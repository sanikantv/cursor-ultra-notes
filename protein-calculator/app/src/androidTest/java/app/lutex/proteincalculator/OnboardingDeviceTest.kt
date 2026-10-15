package app.lutex.proteincalculator

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingDeviceTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    companion object {
        @BeforeClass
        @JvmStatic
        fun clearPersistedOnboarding() {
            val stream = InstrumentationRegistry.getInstrumentation()
                .uiAutomation
                .executeShellCommand("pm clear app.lutex.proteincalculator")
            stream.use { FileInputStream(it.fileDescriptor).readBytes() }
        }
    }

    @Test
    fun welcomeLeadsToTheCalculator() {
        composeRule.waitUntilDisplayed("Know Your Protein Needs")
        composeRule.onNodeWithContentDescription("Protein Calculator app logo").assertIsDisplayed()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.waitUntilDisplayed("Personalized Calculation")
        composeRule.onNodeWithText("Next").performClick()
        composeRule.waitUntilDisplayed("Get Started")
        composeRule.onNodeWithText("Get Started").performClick()
        composeRule.waitUntilDisplayed("Protein Calculator")
        composeRule.onNodeWithText("Calculate Protein").performScrollTo().assertIsDisplayed()
    }
}

private fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.waitUntilDisplayed(
    text: String,
) {
    waitUntil(timeoutMillis = 20_000) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() &&
            try {
                onNodeWithText(text).assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
    }
}
