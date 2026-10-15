package app.lutex.proteincalculator.domain.calculator

import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.PlanningPreset
import app.lutex.proteincalculator.domain.model.ProteinInput
import app.lutex.proteincalculator.domain.model.ProteinRange
import app.lutex.proteincalculator.domain.model.ProteinRecommendation
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * General adult protein planner.
 *
 * Daily protein = body weight in kilograms × one protein factor.
 * The recommended range is weight × lower factor through weight × upper factor.
 * The suggested planning target is the midpoint unless the user turns that
 * preference off or enters a manual factor.
 *
 * Mapping (one range is chosen; factors are never stacked or multiplied):
 *
 * Goal presets that name a dedicated range always win:
 * - Build muscle → Muscle building, 1.4–2.0 g/kg/day
 * - Fat loss while preserving muscle → 1.2–2.0 g/kg/day
 * - Endurance training → 1.2–1.8 g/kg/day
 *
 * General health and Maintain muscle follow the activity baseline:
 * - Sedentary → General health, 0.8–1.0
 * - Light → Light fitness, 1.0–1.2
 * - Moderate → Moderate training, 1.2–1.6
 * - Active, Very Active, and Extra Active → Active training, 1.4–1.8
 *
 * Very Active and Extra Active share the active-training preset because this
 * planner does not publish a higher general-wellness range. A clinician-provided
 * factor can be entered as a manual override. Overrides are not described as
 * automatic recommendations.
 *
 * Age, sex, and height are not protein multipliers. This calculator does not
 * set targets for kidney disease, liver disease, pregnancy, breastfeeding, or
 * other conditions that need medically supervised nutrition.
 */
object ProteinCalculator {

    fun grams(weightKg: Double, factor: Double): Double {
        require(weightKg.isFinite()) { "Weight must be finite." }
        require(factor.isFinite()) { "Factor must be finite." }
        return grams(weightKg, BigDecimal.valueOf(factor)).toDouble()
    }

    fun grams(weightKg: Double, factor: BigDecimal): BigDecimal {
        require(weightKg.isFinite() && weightKg > 0.0) { "Weight must be a positive finite number of kilograms." }
        require(factor > BigDecimal.ZERO) { "Factor must be positive." }
        return BigDecimal.valueOf(weightKg)
            .multiply(factor)
            .setScale(4, RoundingMode.HALF_UP)
            .stripTrailingZeros()
    }

    fun selectPreset(activity: ActivityLevel, goal: FitnessGoal): PlanningPreset = when (goal) {
        FitnessGoal.BUILD_MUSCLE -> PlanningPreset.MUSCLE_BUILDING
        FitnessGoal.FAT_LOSS -> PlanningPreset.FAT_LOSS
        FitnessGoal.ENDURANCE -> PlanningPreset.ENDURANCE
        FitnessGoal.GENERAL_HEALTH, FitnessGoal.MAINTAIN_MUSCLE -> when (activity) {
            ActivityLevel.SEDENTARY -> PlanningPreset.GENERAL_HEALTH
            ActivityLevel.LIGHT -> PlanningPreset.LIGHT_FITNESS
            ActivityLevel.MODERATE -> PlanningPreset.MODERATE_TRAINING
            ActivityLevel.ACTIVE,
            ActivityLevel.VERY_ACTIVE,
            ActivityLevel.EXTRA_ACTIVE,
            -> PlanningPreset.ACTIVE_TRAINING
        }
    }

    fun recommend(input: ProteinInput): ProteinRecommendation {
        require(input.weightKg.isFinite() && input.weightKg > 0.0) {
            "Weight must be a positive finite number of kilograms."
        }
        val custom = input.customFactor
        if (custom != null) {
            require(custom > BigDecimal.ZERO) { "Custom factor must be positive." }
        }

        val preset = selectPreset(input.activity, input.goal)
        val lower = preset.lowerFactor
        val upper = preset.upperFactor
        val midpoint = lower.add(upper).divide(BigDecimal("2"), 4, RoundingMode.HALF_UP)
        val minimum = grams(input.weightKg, lower)
        val maximum = grams(input.weightKg, upper)
        val suggested = when {
            custom != null -> grams(input.weightKg, custom)
            input.preferMidpoint -> grams(input.weightKg, midpoint.stripTrailingZeros())
            else -> minimum
        }
        val range = ProteinRange(lower.toDouble(), upper.toDouble())
        val equation = if (custom != null) {
            "${formatKg(input.weightKg)} kg × ${formatFactor(custom)} g/kg = ${formatGrams(suggested)} g/day"
        } else {
            "${formatKg(input.weightKg)} kg × ${preset.lowerFactorText}–${preset.upperFactorText} g/kg = " +
                "${formatGrams(minimum)}–${formatGrams(maximum)} g/day"
        }
        return ProteinRecommendation(
            weightKg = input.weightKg,
            activity = input.activity,
            goal = input.goal,
            preset = preset,
            range = range,
            minimumGrams = minimum.toDouble(),
            maximumGrams = maximum.toDouble(),
            suggestedGrams = suggested.toDouble(),
            isCustomOverride = custom != null,
            customFactor = custom?.toDouble(),
            equation = equation,
        )
    }

    fun methodology(): List<String> = listOf(
        "Daily protein is body weight in kilograms multiplied by one protein factor.",
        "The range runs from weight × the lower factor to weight × the upper factor. The suggested target is the midpoint of that range unless you turn the midpoint preference off.",
        "Activity and goal choose a single published preset. They are not stacked and they are not multiplied together.",
        "Build muscle uses 1.4–2.0 g/kg/day. Fat loss while preserving muscle uses 1.2–2.0. Endurance training uses 1.2–1.8.",
        "General health and Maintain muscle follow activity: Sedentary 0.8–1.0, Light 1.0–1.2, Moderate 1.2–1.6, and Active, Very Active, or Extra Active 1.4–1.8.",
        "Very Active and Extra Active share the active-training preset. This general planner does not publish a higher wellness range.",
        "A custom factor is a manual override for a number you already have, such as one from a clinician. It is not an automatic recommendation.",
        "Age confirms the general adult range of 18–80. Sex-based reference and height are recorded only. Neither one changes the protein math.",
        "These are general adult planning estimates, not universal medical prescriptions, and not targets for kidney disease, liver disease, pregnancy, breastfeeding, or other medically supervised nutrition.",
    )

    private fun formatKg(weightKg: Double): String = BigDecimal.valueOf(weightKg)
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()

    private fun formatFactor(factor: BigDecimal): String {
        val plain = factor.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        return if ('.' in plain) plain else "$plain.0"
    }

    private fun formatGrams(grams: BigDecimal): String = grams
        .setScale(0, RoundingMode.HALF_UP)
        .toPlainString()
}
