package app.lutex.proteincalculator.data.repository

import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.SavedDailyGoal
import app.lutex.proteincalculator.domain.model.ThemePreference
import app.lutex.proteincalculator.domain.model.UserPreferences
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setOnboardingComplete(complete: Boolean)
    suspend fun setTheme(theme: ThemePreference)
    suspend fun setUnits(units: MeasurementSystem)
    suspend fun setDefaultActivity(activity: ActivityLevel)
    suspend fun setDefaultGoal(goal: FitnessGoal)
    suspend fun setCustomFactor(factor: BigDecimal?)
    suspend fun setShowRecommendedRange(show: Boolean)
    suspend fun setUseMidpoint(use: Boolean)
    suspend fun saveDailyGoal(goal: SavedDailyGoal)
}
