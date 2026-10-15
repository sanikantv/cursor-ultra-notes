package app.lutex.proteincalculator.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * How often and how hard someone trains. Activity selects a baseline planning
 * range only when the fitness goal does not name its own range.
 */
enum class ActivityLevel(
    val label: String,
    val description: String,
) {
    SEDENTARY("Sedentary", "Little or no exercise"),
    LIGHT("Light", "Exercise 1–3 times/week"),
    MODERATE("Moderate", "Exercise 4–5 times/week"),
    ACTIVE("Active", "Daily exercise or intense exercise 3–4 times/week"),
    VERY_ACTIVE("Very Active", "Intense exercise 6–7 times/week"),
    EXTRA_ACTIVE("Extra Active", "Very intense exercise daily or physical job"),
}

/**
 * The user's planning intent. Dedicated goal ranges replace the activity
 * baseline. They are never multiplied by it.
 */
enum class FitnessGoal(val label: String) {
    GENERAL_HEALTH("General health"),
    BUILD_MUSCLE("Build muscle"),
    MAINTAIN_MUSCLE("Maintain muscle"),
    FAT_LOSS("Fat loss while preserving muscle"),
    ENDURANCE("Endurance training"),
}

/**
 * Published general-planning presets. Factors are grams of protein per
 * kilogram of body weight per day.
 */
enum class PlanningPreset(
    val label: String,
    val lowerFactorText: String,
    val upperFactorText: String,
) {
    GENERAL_HEALTH("General health", "0.8", "1.0"),
    LIGHT_FITNESS("Light fitness", "1.0", "1.2"),
    MODERATE_TRAINING("Moderate training", "1.2", "1.6"),
    ACTIVE_TRAINING("Active training", "1.4", "1.8"),
    MUSCLE_BUILDING("Muscle building", "1.4", "2.0"),
    ENDURANCE("Endurance", "1.2", "1.8"),
    FAT_LOSS("Fat loss / muscle preservation", "1.2", "2.0"),
    ;

    val lowerFactor: BigDecimal get() = BigDecimal(lowerFactorText)
    val upperFactor: BigDecimal get() = BigDecimal(upperFactorText)
    val rangeLabel: String get() = "$lowerFactorText–$upperFactorText g/kg/day"
}

enum class MeasurementSystem(val label: String, val detail: String) {
    METRIC("Metric", "Kilograms and centimeters"),
    US("US", "Pounds, feet, and inches"),
    OTHER("Other", "Stones, pounds, and inches"),
}

/** Recorded for context. It does not change the protein math. */
enum class SexReference(val label: String) {
    MALE("Male reference"),
    FEMALE("Female reference"),
    UNSPECIFIED("Prefer not to specify"),
}

enum class ThemePreference(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
    EMERALD_DARK("Emerald Dark"),
}

data class ProteinRange(
    val lowerFactor: Double,
    val upperFactor: Double,
) {
    val midpointFactor: Double
        get() = BigDecimal.valueOf(lowerFactor)
            .add(BigDecimal.valueOf(upperFactor))
            .divide(BigDecimal("2"), 4, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toDouble()
}

/**
 * Inputs that affect the protein estimate. Age, height, and sex are validated
 * or displayed elsewhere and are intentionally absent here.
 */
data class ProteinInput(
    val weightKg: Double,
    val activity: ActivityLevel,
    val goal: FitnessGoal,
    val customFactor: BigDecimal? = null,
    val preferMidpoint: Boolean = true,
)

data class ProteinRecommendation(
    val weightKg: Double,
    val activity: ActivityLevel,
    val goal: FitnessGoal,
    val preset: PlanningPreset,
    val range: ProteinRange,
    val minimumGrams: Double,
    val maximumGrams: Double,
    val suggestedGrams: Double,
    val isCustomOverride: Boolean,
    val customFactor: Double?,
    val equation: String,
) {
    /** Custom targets are stored numbers, not automatic recommendations. */
    val isAutomaticRecommendation: Boolean get() = !isCustomOverride
}

data class SavedDailyGoal(
    val selectedGrams: Int,
    val minimumGrams: Int,
    val maximumGrams: Int,
    val weightKg: Double,
    val activity: ActivityLevel,
    val goal: FitnessGoal,
    val preset: PlanningPreset,
    val factorLower: Double,
    val factorUpper: Double,
    val isCustomOverride: Boolean,
    val savedAtEpochMs: Long,
)

data class UserPreferences(
    val onboardingComplete: Boolean = false,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val units: MeasurementSystem = MeasurementSystem.METRIC,
    val defaultActivity: ActivityLevel = ActivityLevel.MODERATE,
    val defaultGoal: FitnessGoal = FitnessGoal.GENERAL_HEALTH,
    val customFactor: BigDecimal? = null,
    val showRecommendedRange: Boolean = true,
    val useMidpoint: Boolean = true,
    val savedGoal: SavedDailyGoal? = null,
)

/**
 * Everything the results screen needs to recompute a deterministic estimate
 * after navigation or process recreation.
 */
data class CalculationRequest(
    val weightKg: Double,
    val heightCm: Double?,
    val age: Int,
    val sex: SexReference,
    val activity: ActivityLevel,
    val goal: FitnessGoal,
    val customFactor: String?,
    val showRange: Boolean,
    val useMidpoint: Boolean,
    val units: MeasurementSystem,
) {
    fun toProteinInput(): ProteinInput = ProteinInput(
        weightKg = weightKg,
        activity = activity,
        goal = goal,
        customFactor = customFactor?.let(::BigDecimal),
        preferMidpoint = useMidpoint,
    )

    fun encode(): String = listOf(
        weightKg.toString(),
        heightCm?.toString().orEmpty(),
        age.toString(),
        sex.name,
        activity.name,
        goal.name,
        customFactor.orEmpty(),
        showRange.toString(),
        useMidpoint.toString(),
        units.name,
    ).joinToString(SEPARATOR)

    companion object {
        const val SEPARATOR = "~"

        fun decode(payload: String): CalculationRequest? {
            val parts = payload.split(SEPARATOR)
            if (parts.size != 10) return null
            return try {
                CalculationRequest(
                    weightKg = parts[0].toDouble(),
                    heightCm = parts[1].toDoubleOrNull(),
                    age = parts[2].toInt(),
                    sex = SexReference.valueOf(parts[3]),
                    activity = ActivityLevel.valueOf(parts[4]),
                    goal = FitnessGoal.valueOf(parts[5]),
                    customFactor = parts[6].ifBlank { null },
                    showRange = parts[7].toBooleanStrict(),
                    useMidpoint = parts[8].toBooleanStrict(),
                    units = MeasurementSystem.valueOf(parts[9]),
                )
            } catch (_: RuntimeException) {
                null
            }
        }
    }
}
