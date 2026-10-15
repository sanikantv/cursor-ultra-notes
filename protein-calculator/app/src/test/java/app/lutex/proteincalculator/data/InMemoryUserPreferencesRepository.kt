package app.lutex.proteincalculator.data

import app.lutex.proteincalculator.data.repository.UserPreferencesRepository
import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.SavedDailyGoal
import app.lutex.proteincalculator.domain.model.ThemePreference
import app.lutex.proteincalculator.domain.model.UserPreferences
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryUserPreferencesRepository(
    initial: UserPreferences = UserPreferences(),
) : UserPreferencesRepository {
    private val state = MutableStateFlow(initial)
    override val preferences: Flow<UserPreferences> = state.asStateFlow()

    override suspend fun setOnboardingComplete(complete: Boolean) {
        state.update { it.copy(onboardingComplete = complete) }
    }

    override suspend fun setTheme(theme: ThemePreference) {
        state.update { it.copy(theme = theme) }
    }

    override suspend fun setUnits(units: MeasurementSystem) {
        state.update { it.copy(units = units) }
    }

    override suspend fun setDefaultActivity(activity: ActivityLevel) {
        state.update { it.copy(defaultActivity = activity) }
    }

    override suspend fun setDefaultGoal(goal: FitnessGoal) {
        state.update { it.copy(defaultGoal = goal) }
    }

    override suspend fun setCustomFactor(factor: BigDecimal?) {
        state.update { it.copy(customFactor = factor) }
    }

    override suspend fun setShowRecommendedRange(show: Boolean) {
        state.update { it.copy(showRecommendedRange = show) }
    }

    override suspend fun setUseMidpoint(use: Boolean) {
        state.update { it.copy(useMidpoint = use) }
    }

    override suspend fun saveDailyGoal(goal: SavedDailyGoal) {
        state.update { it.copy(savedGoal = goal) }
    }
}
