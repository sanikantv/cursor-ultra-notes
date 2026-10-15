package app.lutex.proteincalculator.core.navigation

import app.lutex.proteincalculator.domain.model.CalculationRequest

/**
 * Calculator and Settings are the Phase 1 bottom-bar destinations.
 * Tracker and History are reserved for Phase 2 and are not shown.
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val CALCULATOR = "calculator"
    const val SETTINGS = "settings"
    const val RESULTS = "results/{payload}"
    const val DISCLAIMER = "disclaimer"
    const val PRIVACY = "privacy"
    const val ABOUT = "about"
    const val METHODOLOGY = "methodology"

    const val TRACKER = "tracker"
    const val HISTORY = "history"

    fun results(request: CalculationRequest): String = "results/${request.encode()}"

    fun startDestination(onboardingComplete: Boolean): String =
        if (onboardingComplete) CALCULATOR else ONBOARDING
}

enum class BottomDestination(
    val route: String,
    val label: String,
) {
    CALCULATOR(Routes.CALCULATOR, "Calculator"),
    SETTINGS(Routes.SETTINGS, "Settings"),
}
