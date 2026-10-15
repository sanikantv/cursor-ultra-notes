package app.lutex.proteincalculator.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.ThemePreference
import app.lutex.proteincalculator.domain.model.UserPreferences
import app.lutex.proteincalculator.domain.usecase.ObserveUserPreferencesUseCase
import app.lutex.proteincalculator.domain.usecase.UpdateUserPreferencesUseCase
import app.lutex.proteincalculator.domain.validation.InputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loaded: Boolean = false,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val units: MeasurementSystem = MeasurementSystem.METRIC,
    val activity: ActivityLevel = ActivityLevel.MODERATE,
    val goal: FitnessGoal = FitnessGoal.GENERAL_HEALTH,
    val customFactorText: String = "",
    val customFactorError: String? = null,
    val showRange: Boolean = true,
    val useMidpoint: Boolean = true,
    val savedGoalGrams: Int? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observePreferences: ObserveUserPreferencesUseCase,
    private val updatePreferences: UpdateUserPreferencesUseCase,
) : ViewModel() {
    val state: StateFlow<SettingsUiState> = observePreferences()
        .map { it.toSettingsState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTheme(theme: ThemePreference) = launch { updatePreferences.setTheme(theme) }
    fun setUnits(units: MeasurementSystem) = launch { updatePreferences.setUnits(units) }
    fun setActivity(activity: ActivityLevel) = launch { updatePreferences.setDefaultActivity(activity) }
    fun setGoal(goal: FitnessGoal) = launch { updatePreferences.setDefaultGoal(goal) }
    fun setShowRange(show: Boolean) = launch { updatePreferences.setShowRecommendedRange(show) }
    fun setUseMidpoint(use: Boolean) = launch { updatePreferences.setUseMidpoint(use) }

    fun setCustomFactor(value: String) {
        val sanitized = InputValidator.sanitizeDecimal(value)
        val error = InputValidator.validateCustomFactor(sanitized)
        if (error == null) {
            launch { updatePreferences.setCustomFactor(InputValidator.parseCustomFactor(sanitized)) }
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}

private fun UserPreferences.toSettingsState(): SettingsUiState = SettingsUiState(
    loaded = true,
    theme = theme,
    units = units,
    activity = defaultActivity,
    goal = defaultGoal,
    customFactorText = customFactor?.stripTrailingZeros()?.toPlainString().orEmpty(),
    showRange = showRecommendedRange,
    useMidpoint = useMidpoint,
    savedGoalGrams = savedGoal?.selectedGrams,
)
