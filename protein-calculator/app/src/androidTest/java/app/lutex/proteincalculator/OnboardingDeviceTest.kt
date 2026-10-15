package app.lutex.proteincalculator

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingDeviceTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun welcomeLeadsToTheCalculator() {
        composeRule.onNodeWithText("Know Your Protein Needs").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Protein Calculator app logo").assertIsDisplayed()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Get Started").performClick()
        composeRule.onNodeWithText("Protein Calculator").assertIsDisplayed()
        composeRule.onNodeWithText("Calculate Protein").assertIsDisplayed()
    }
}
