package app.lutex.proteincalculator.domain.calculator

import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.ProteinRecommendation
import app.lutex.proteincalculator.domain.model.SexReference
import java.math.BigDecimal
import java.math.RoundingMode

object MealDistribution {
    val supportedMeals: List<Int> = listOf(3, 4, 5)

    /** Splits a daily gram target so the meals add back to the same total. */
    fun distribute(totalGrams: Int, meals: Int): List<Int> {
        require(meals in supportedMeals) { "Meal examples support 3, 4, or 5 meals." }
        require(totalGrams >= 0) { "Grams cannot be negative." }
        val base = totalGrams / meals
        val remainder = totalGrams % meals
        return List(meals) { index -> base + if (index < remainder) 1 else 0 }
    }
}

object DisplayNumbers {
    fun roundGrams(value: Double): Int = BigDecimal.valueOf(value)
        .setScale(0, RoundingMode.HALF_UP)
        .intValueExact()
}

fun ProteinRecommendation.shareMessage(
    selectedGrams: Int,
    age: Int,
    sex: SexReference,
    heightCm: Double?,
    units: MeasurementSystem,
): String = buildString {
    appendLine("Daily protein target: $selectedGrams g")
    appendLine(
        "Recommended range: ${DisplayNumbers.roundGrams(minimumGrams)}–${DisplayNumbers.roundGrams(maximumGrams)} g/day",
    )
    appendLine("Goal: ${goal.label}")
    appendLine("Activity: ${activity.label}")
    appendLine("Planning preset: ${preset.label} (${preset.rangeLabel})")
    appendLine("Body weight: ${weightSummary(weightKg, units)}")
    if (heightCm != null) {
        appendLine("Height: ${heightSummary(heightCm, units)} (not used in the calculation)")
    }
    appendLine("Age: $age (adult check only)")
    appendLine("Sex-based reference: ${sex.label} (not used in the calculation)")
    if (isCustomOverride) {
        appendLine("Manual override: not an automatic recommendation")
    }
    appendLine("Equation: $equation")
    append("General adult planning estimate. Not medical advice.")
}

private fun weightSummary(weightKg: Double, units: MeasurementSystem): String {
    val fields = MeasurementFormatter.formatWeight(weightKg, units)
    val kg = MeasurementFormatter.formatDecimal(weightKg, 2)
    return when (units) {
        MeasurementSystem.METRIC -> "$kg kg"
        MeasurementSystem.US -> "${fields.primary} lb ($kg kg)"
        MeasurementSystem.OTHER -> "${fields.primary} st ${fields.secondary} lb ($kg kg)"
    }
}

private fun heightSummary(heightCm: Double, units: MeasurementSystem): String {
    val fields = MeasurementFormatter.formatHeight(heightCm, units)
    return when (units) {
        MeasurementSystem.METRIC -> "${fields.primary} cm"
        MeasurementSystem.US -> "${fields.primary} ft ${fields.secondary} in"
        MeasurementSystem.OTHER -> "${fields.primary} in"
    }
}
