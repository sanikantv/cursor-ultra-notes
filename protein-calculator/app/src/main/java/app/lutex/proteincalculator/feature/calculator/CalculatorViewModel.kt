package app.lutex.proteincalculator.feature.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.lutex.proteincalculator.data.repository.UserPreferencesRepository
import app.lutex.proteincalculator.domain.calculator.MeasurementFormatter
import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.CalculationRequest
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.SexReference
import app.lutex.proteincalculator.domain.usecase.CalculateProteinUseCase
import app.lutex.proteincalculator.domain.validation.InputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PendingCalculation(
    val request: CalculationRequest,
    val nonce: Long,
)

data class CalculatorUiState(
    val ageText: String = "",
    val ageError: String? = null,
    val sex: SexReference = SexReference.UNSPECIFIED,
    val units: MeasurementSystem = MeasurementSystem.METRIC,
    val weightPrimaryText: String = "",
    val weightSecondaryText: String = "",
    val weightKg: Double? = null,
    val weightError: String? = null,
    val heightPrimaryText: String = "",
    val heightSecondaryText: String = "",
    val heightCm: Double? = null,
    val heightError: String? = null,
    val activity: ActivityLevel = ActivityLevel.MODERATE,
    val activityExpanded: Boolean = false,
    val goal: FitnessGoal = FitnessGoal.GENERAL_HEALTH,
    val advancedExpanded: Boolean = false,
    val customFactorText: String = "",
    val customFactorError: String? = null,
    val showRange: Boolean = true,
    val useMidpoint: Boolean = true,
    val formError: String? = null,
    val savedGoalGrams: Int? = null,
) {
    fun reformatFromCanonical(): CalculatorUiState {
        val weight = weightKg?.let { MeasurementFormatter.formatWeight(it, units) }
        val height = heightCm?.let { MeasurementFormatter.formatHeight(it, units) }
        return copy(
            weightPrimaryText = weight?.primary ?: weightPrimaryText,
            weightSecondaryText = when {
                weight != null -> weight.secondary
                units == MeasurementSystem.OTHER -> weightSecondaryText
                else -> ""
            },
            heightPrimaryText = height?.primary ?: heightPrimaryText,
            heightSecondaryText = when {
                height != null -> height.secondary
                units == MeasurementSystem.US -> heightSecondaryText
                else -> ""
            },
        )
    }
}

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val calculateProtein: CalculateProteinUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(CalculatorUiState())
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    private val _pendingResult = MutableStateFlow<PendingCalculation?>(null)
    val pendingResult: StateFlow<PendingCalculation?> = _pendingResult.asStateFlow()

    private var defaultsApplied = false

    init {
        viewModelScope.launch {
            repository.preferences.collect { prefs ->
                _state.update { current ->
                    val first = !defaultsApplied
                    if (first) defaultsApplied = true
                    val unitsChanged = current.units != prefs.units
                    val next = current.copy(
                        savedGoalGrams = prefs.savedGoal?.selectedGrams,
                        units = prefs.units,
                        activity = if (first) prefs.defaultActivity else current.activity,
                        goal = if (first) prefs.defaultGoal else current.goal,
                        customFactorText = if (first) {
                            prefs.customFactor?.stripTrailingZeros()?.toPlainString().orEmpty()
                        } else {
                            current.customFactorText
                        },
                        showRange = if (first) prefs.showRecommendedRange else current.showRange,
                        useMidpoint = if (first) prefs.useMidpoint else current.useMidpoint,
                    )
                    if (first || unitsChanged) next.reformatFromCanonical() else next
                }
            }
        }
    }

    fun onAgeChange(value: String) = _state.update {
        it.copy(ageText = InputValidator.sanitizeInteger(value, 3), ageError = null, formError = null)
    }

    fun onSexChange(value: SexReference) = _state.update { it.copy(sex = value) }

    fun onUnitsSelected(system: MeasurementSystem) {
        _state.update { current ->
            if (current.units == system) current else current.copy(units = system).reformatFromCanonical()
        }
        viewModelScope.launch { repository.setUnits(system) }
    }

    fun onWeightPrimaryChange(value: String) = updateWeight(primary = value, secondary = null)

    fun onWeightSecondaryChange(value: String) = updateWeight(primary = null, secondary = value)

    fun onHeightPrimaryChange(value: String) = updateHeight(primary = value, secondary = null)

    fun onHeightSecondaryChange(value: String) = updateHeight(primary = null, secondary = value)

    fun onActivityChange(activity: ActivityLevel) = _state.update { it.copy(activity = activity) }

    fun onActivityExpandedChange(expanded: Boolean) = _state.update { it.copy(activityExpanded = expanded) }

    fun onGoalChange(goal: FitnessGoal) = _state.update { it.copy(goal = goal) }

    fun onAdvancedExpandedChange(expanded: Boolean) = _state.update { it.copy(advancedExpanded = expanded) }

    fun onCustomFactorChange(value: String) {
        val sanitized = InputValidator.sanitizeDecimal(value)
        _state.update { it.copy(customFactorText = sanitized, customFactorError = null, formError = null) }
        if (InputValidator.validateCustomFactor(sanitized) == null) {
            viewModelScope.launch { repository.setCustomFactor(InputValidator.parseCustomFactor(sanitized)) }
        }
    }

    fun onShowRangeChange(show: Boolean) {
        _state.update { it.copy(showRange = show) }
        viewModelScope.launch { repository.setShowRecommendedRange(show) }
    }

    fun onUseMidpointChange(use: Boolean) {
        _state.update { it.copy(useMidpoint = use) }
        viewModelScope.launch { repository.setUseMidpoint(use) }
    }

    fun calculate() {
        val current = _state.value
        val weightProvided = current.weightPrimaryText.isNotBlank() ||
            (current.units == MeasurementSystem.OTHER && current.weightSecondaryText.isNotBlank())
        val heightProvided = current.heightPrimaryText.isNotBlank() || current.heightSecondaryText.isNotBlank()
        val errors = InputValidator.validate(
            ageText = current.ageText,
            weightKg = current.weightKg,
            weightProvided = weightProvided,
            heightCm = current.heightCm,
            heightProvided = heightProvided,
            customFactorText = current.customFactorText,
        )
        if (!errors.isValid || current.weightKg == null) {
            _state.update {
                it.copy(
                    ageError = errors.age,
                    weightError = errors.weight,
                    heightError = errors.height,
                    customFactorError = errors.customFactor,
                    formError = "Check the highlighted fields before calculating.",
                )
            }
            _pendingResult.value = null
            return
        }
        val age = InputValidator.parseAge(current.ageText) ?: return
        val request = CalculationRequest(
            weightKg = current.weightKg,
            heightCm = current.heightCm,
            age = age,
            sex = current.sex,
            activity = current.activity,
            goal = current.goal,
            customFactor = InputValidator.parseCustomFactor(current.customFactorText)?.toPlainString(),
            showRange = current.showRange,
            useMidpoint = current.useMidpoint,
            units = current.units,
        )
        calculateProtein(request.toProteinInput())
        _state.update { it.copy(formError = null, ageError = null, weightError = null, heightError = null, customFactorError = null) }
        _pendingResult.value = PendingCalculation(request, System.nanoTime())
    }

    fun consumePendingResult() {
        _pendingResult.value = null
    }

    private fun updateWeight(primary: String?, secondary: String?) {
        _state.update { current ->
            val nextPrimary = primary?.let { sanitizeWeightPrimary(it, current.units) } ?: current.weightPrimaryText
            val nextSecondary = secondary?.let(InputValidator::sanitizeDecimal) ?: current.weightSecondaryText
            val parsed = MeasurementFormatter.parseWeight(nextPrimary, nextSecondary, current.units)
            current.copy(
                weightPrimaryText = nextPrimary,
                weightSecondaryText = nextSecondary,
                weightKg = parsed,
                weightError = null,
                formError = null,
            )
        }
    }

    private fun updateHeight(primary: String?, secondary: String?) {
        _state.update { current ->
            val nextPrimary = primary?.let { sanitizeHeightPrimary(it, current.units) } ?: current.heightPrimaryText
            val nextSecondary = secondary?.let(InputValidator::sanitizeDecimal) ?: current.heightSecondaryText
            val parsed = MeasurementFormatter.parseHeight(nextPrimary, nextSecondary, current.units)
            current.copy(
                heightPrimaryText = nextPrimary,
                heightSecondaryText = nextSecondary,
                heightCm = parsed,
                heightError = null,
                formError = null,
            )
        }
    }

    private fun sanitizeWeightPrimary(value: String, system: MeasurementSystem): String = when (system) {
        MeasurementSystem.OTHER -> InputValidator.sanitizeInteger(value, 2)
        else -> InputValidator.sanitizeDecimal(value)
    }

    private fun sanitizeHeightPrimary(value: String, system: MeasurementSystem): String = when (system) {
        MeasurementSystem.US -> InputValidator.sanitizeInteger(value, 1)
        else -> InputValidator.sanitizeDecimal(value)
    }
}
