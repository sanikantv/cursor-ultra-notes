package app.lutex.proteincalculator.feature

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.lutex.proteincalculator.core.designsystem.ProteinTheme
import app.lutex.proteincalculator.domain.model.ThemePreference
import app.lutex.proteincalculator.feature.calculator.CalculatorScreen
import app.lutex.proteincalculator.feature.calculator.CalculatorUiState
import app.lutex.proteincalculator.feature.onboarding.OnboardingScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w320dp-h640dp")
class SmallScreenLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun calculatorFitsASmallScreenAndKeepsTheActionReachable() {
        composeRule.setContent {
            ProteinTheme(preference = ThemePreference.EMERALD_DARK) {
                CalculatorScreen(state = CalculatorUiState())
            }
        }
        composeRule.onNodeWithText("Protein Calculator").assertIsDisplayed()
        composeRule.onNodeWithText("Age").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Calculate Protein").performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        composeRule.onAllNodesWithText("Tracker").fetchSemanticsNodes().let { nodes ->
            org.junit.Assert.assertTrue(nodes.isEmpty())
        }
    }

    @Test
    fun largeFontStillShowsThePrimaryAction() {
        composeRule.setContent {
            ProteinTheme(preference = ThemePreference.LIGHT) {
                val current = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(current.density, fontScale = 2f),
                ) {
                    Box(Modifier.requiredSize(width = 320.dp, height = 700.dp)) {
                        CalculatorScreen(state = CalculatorUiState())
                    }
                }
            }
        }
        composeRule.onNodeWithText("Age").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Calculate Protein").performScrollTo().assertIsDisplayed()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w800dp-h360dp-land")
class LandscapeLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun landscapeCalculatorStillShowsTheTitleAndAction() {
        composeRule.setContent {
            ProteinTheme(preference = ThemePreference.DARK) {
                CalculatorScreen(state = CalculatorUiState())
            }
        }
        composeRule.onNodeWithText("Find Your Daily Protein Target").assertIsDisplayed()
        composeRule.onNodeWithText("Calculate Protein").performScrollTo().assertIsDisplayed()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class OnboardingScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun onboardingShowsTheLogoAndCompletesFromTheLastPage() {
        var finished = false
        composeRule.setContent {
            ProteinTheme(preference = ThemePreference.EMERALD_DARK) {
                OnboardingScreen(onFinished = { finished = true })
            }
        }
        composeRule.onNodeWithText("Know Your Protein Needs").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Protein Calculator app logo").assertIsDisplayed()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Personalized Calculation").assertIsDisplayed()
        composeRule.onNodeWithText("Back").assertIsDisplayed()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Daily Nutrition Planning").assertIsDisplayed()
        composeRule.onNodeWithText("Get Started").performClick()
        assertTrue(finished)
    }
}
