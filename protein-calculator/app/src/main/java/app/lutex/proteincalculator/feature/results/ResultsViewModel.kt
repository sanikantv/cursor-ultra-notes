package app.lutex.proteincalculator.feature.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.lutex.proteincalculator.domain.calculator.DisplayNumbers
import app.lutex.proteincalculator.domain.calculator.MealDistribution
import app.lutex.proteincalculator.domain.calculator.shareMessage
import app.lutex.proteincalculator.domain.model.CalculationRequest
import app.lutex.proteincalculator.domain.model.SavedDailyGoal
import app.lutex.proteincalculator.domain.usecase.CalculateProteinUseCase
import app.lutex.proteincalculator.domain.usecase.SaveDailyGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResultsUiState(
    val ready: Boolean = false,
    val error: String? = null,
    val selectedGrams: Int = 0,
    val minimumGrams: Int = 0,
    val maximumGrams: Int = 0,
    val suggestedGrams: Int = 0,
    val sliderMin: Int = 0,
    val sliderMax: Int = 0,
    val showRange: Boolean = true,
    val goalLabel: String = "",
    val activityLabel: String = "",
    val presetLabel: String = "",
    val factorLabel: String = "",
    val weightLabel: String = "",
    val heightLabel: String = "",
    val sexLabel: String = "",
    val ageLabel: String = "",
    val equation: String = "",
    val isCustomOverride: Boolean = false,
    val rangeMeaning: String = "",
    val meals: Int = 4,
    val mealPortions: List<Int> = emptyList(),
    val savedMessage: String? = null,
    val shareText: String = "",
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    calculateProtein: CalculateProteinUseCase,
    private val saveDailyGoal: SaveDailyGoalUseCase,
) : ViewModel() {
    private val request = savedStateHandle.get<String>("payload")?.let(CalculationRequest::decode)
    private val recommendation = request?.let { calculateProtein(it.toProteinInput()) }

    private val _state = MutableStateFlow(buildState(request, recommendation, meals = 4, selected = null, savedMessage = null))
    val state: StateFlow<ResultsUiState> = _state.asStateFlow()

    fun onSelectedGrams(grams: Int) {
        val current = _state.value
        val clamped = grams.coerceIn(current.sliderMin, current.sliderMax)
        _state.value = rebuild(clamped, current.meals, savedMessage = null)
    }

    fun onMealsChange(meals: Int) {
        if (meals !in MealDistribution.supportedMeals) return
        val current = _state.value
        _state.value = rebuild(current.selectedGrams, meals, current.savedMessage)
    }

    fun save() {
        val snapshot = recommendation ?: return
        val current = _state.value
        viewModelScope.launch {
            saveDailyGoal(
                SavedDailyGoal(
                    selectedGrams = current.selectedGrams,
                    minimumGrams = current.minimumGrams,
                    maximumGrams = current.maximumGrams,
                    weightKg = snapshot.weightKg,
                    activity = snapshot.activity,
                    goal = snapshot.goal,
                    preset = snapshot.preset,
                    factorLower = snapshot.range.lowerFactor,
                    factorUpper = snapshot.range.upperFactor,
                    isCustomOverride = snapshot.isCustomOverride,
                    savedAtEpochMs = System.currentTimeMillis(),
                ),
            )
            _state.update { it.copy(savedMessage = "Daily goal saved on this device.") }
        }
    }

    fun consumeSavedMessage() {
        _state.update { it.copy(savedMessage = null) }
    }

    private fun rebuild(selected: Int, meals: Int, savedMessage: String?): ResultsUiState =
        buildState(request, recommendation, meals, selected, savedMessage)

    private fun buildState(
        request: CalculationRequest?,
        recommendation: app.lutex.proteincalculator.domain.model.ProteinRecommendation?,
        meals: Int,
        selected: Int?,
        savedMessage: String?,
    ): ResultsUiState {
        if (request == null || recommendation == null) {
            return ResultsUiState(ready = false, error = "This result is unavailable. Calculate again to continue.")
        }
        val minimum = DisplayNumbers.roundGrams(recommendation.minimumGrams)
        val maximum = DisplayNumbers.roundGrams(recommendation.maximumGrams)
        val suggested = DisplayNumbers.roundGrams(recommendation.suggestedGrams)
        val sliderMin = min(minimum, suggested)
        val sliderMax = max(maximum, suggested)
        val chosen = (selected ?: suggested).coerceIn(sliderMin, sliderMax)
        val weightFields = app.lutex.proteincalculator.domain.calculator.MeasurementFormatter
            .formatWeight(recommendation.weightKg, request.units)
        val weightLabel = when (request.units) {
            app.lutex.proteincalculator.domain.model.MeasurementSystem.METRIC -> "${weightFields.primary} kg"
            app.lutex.proteincalculator.domain.model.MeasurementSystem.US -> "${weightFields.primary} lb"
            app.lutex.proteincalculator.domain.model.MeasurementSystem.OTHER ->
                "${weightFields.primary} st ${weightFields.secondary} lb"
        }
        val heightLabel = request.heightCm?.let { cm ->
            val fields = app.lutex.proteincalculator.domain.calculator.MeasurementFormatter
                .formatHeight(cm, request.units)
            when (request.units) {
                app.lutex.proteincalculator.domain.model.MeasurementSystem.METRIC -> "${fields.primary} cm"
                app.lutex.proteincalculator.domain.model.MeasurementSystem.US -> "${fields.primary} ft ${fields.secondary} in"
                app.lutex.proteincalculator.domain.model.MeasurementSystem.OTHER -> "${fields.primary} in"
            }
        } ?: "Not provided"
        return ResultsUiState(
            ready = true,
            selectedGrams = chosen,
            minimumGrams = minimum,
            maximumGrams = maximum,
            suggestedGrams = suggested,
            sliderMin = sliderMin,
            sliderMax = sliderMax,
            showRange = request.showRange,
            goalLabel = recommendation.goal.label,
            activityLabel = recommendation.activity.label,
            presetLabel = recommendation.preset.label,
            factorLabel = recommendation.preset.rangeLabel,
            weightLabel = weightLabel,
            heightLabel = heightLabel,
            sexLabel = request.sex.label,
            ageLabel = request.age.toString(),
            equation = recommendation.equation,
            isCustomOverride = recommendation.isCustomOverride,
            rangeMeaning = "The range is the same body weight multiplied by the lower and upper factors of one planning preset.",
            meals = meals,
            mealPortions = MealDistribution.distribute(chosen, meals),
            savedMessage = savedMessage,
            shareText = recommendation.shareMessage(
                selectedGrams = chosen,
                age = request.age,
                sex = request.sex,
                heightCm = request.heightCm,
                units = request.units,
            ),
        )
    }
}
