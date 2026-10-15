package app.lutex.proteincalculator.domain.usecase

import app.lutex.proteincalculator.data.repository.UserPreferencesRepository
import app.lutex.proteincalculator.domain.calculator.ProteinCalculator
import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.ProteinInput
import app.lutex.proteincalculator.domain.model.ProteinRecommendation
import app.lutex.proteincalculator.domain.model.SavedDailyGoal
import app.lutex.proteincalculator.domain.model.ThemePreference
import app.lutex.proteincalculator.domain.model.UserPreferences
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class CalculateProteinUseCase @Inject constructor() {
    operator fun invoke(input: ProteinInput): ProteinRecommendation = ProteinCalculator.recommend(input)
}

class ObserveUserPreferencesUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<UserPreferences> = repository.preferences
}

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke() {
        repository.setOnboardingComplete(true)
    }
}

class SaveDailyGoalUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(goal: SavedDailyGoal) {
        repository.saveDailyGoal(goal)
    }
}

class UpdateUserPreferencesUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend fun setTheme(theme: ThemePreference) = repository.setTheme(theme)
    suspend fun setUnits(units: MeasurementSystem) = repository.setUnits(units)
    suspend fun setDefaultActivity(activity: ActivityLevel) = repository.setDefaultActivity(activity)
    suspend fun setDefaultGoal(goal: FitnessGoal) = repository.setDefaultGoal(goal)
    suspend fun setCustomFactor(factor: BigDecimal?) = repository.setCustomFactor(factor)
    suspend fun setShowRecommendedRange(show: Boolean) = repository.setShowRecommendedRange(show)
    suspend fun setUseMidpoint(use: Boolean) = repository.setUseMidpoint(use)
}
