package app.lutex.proteincalculator.domain.calculator

import app.lutex.proteincalculator.domain.model.MeasurementSystem
import java.math.BigDecimal
import java.math.RoundingMode

data class QuantityFields(
    val primary: String,
    val secondary: String = "",
)

/**
 * Formats canonical kilograms and centimeters for the active unit system.
 * Switching systems reformats these canonical values, so the underlying
 * measurement does not drift through display rounding.
 */
object MeasurementFormatter {
    private val decimalPattern = Regex("""\d+(\.\d+)?""")

    fun formatWeight(kg: Double, system: MeasurementSystem): QuantityFields = when (system) {
        MeasurementSystem.METRIC -> QuantityFields(formatDecimal(kg, 2))
        MeasurementSystem.US -> QuantityFields(formatDecimal(UnitConverter.kgToLb(kg), 2))
        MeasurementSystem.OTHER -> {
            val (stones, pounds) = UnitConverter.kgToStoneAndPounds(kg)
            QuantityFields(stones.toString(), formatDecimal(pounds, 2))
        }
    }

    fun formatHeight(cm: Double, system: MeasurementSystem): QuantityFields = when (system) {
        MeasurementSystem.METRIC -> QuantityFields(formatDecimal(cm, 1))
        MeasurementSystem.US -> {
            val (feet, inches) = UnitConverter.cmToFeetAndInches(cm)
            QuantityFields(feet.toString(), formatDecimal(inches, 1))
        }
        MeasurementSystem.OTHER -> QuantityFields(formatDecimal(UnitConverter.cmToInches(cm), 1))
    }

    fun parseWeight(primary: String, secondary: String, system: MeasurementSystem): Double? {
        if (primary.isBlank() && secondary.isBlank()) return null
        return when (system) {
            MeasurementSystem.METRIC -> parseDecimal(primary)
            MeasurementSystem.US -> parseDecimal(primary)?.let(UnitConverter::lbToKg)
            MeasurementSystem.OTHER -> {
                val stones = primary.toIntOrNull() ?: return null
                if (stones < 0) return null
                val pounds = if (secondary.isBlank()) 0.0 else parseDecimal(secondary) ?: return null
                if (pounds < 0) return null
                UnitConverter.stoneAndPoundsToKg(stones, pounds)
            }
        }
    }

    fun parseHeight(primary: String, secondary: String, system: MeasurementSystem): Double? {
        if (primary.isBlank() && secondary.isBlank()) return null
        return when (system) {
            MeasurementSystem.METRIC -> parseDecimal(primary)
            MeasurementSystem.OTHER -> parseDecimal(primary)?.let(UnitConverter::inchesToCm)
            MeasurementSystem.US -> {
                val feet = if (primary.isBlank()) 0 else primary.toIntOrNull() ?: return null
                if (feet < 0) return null
                val inches = if (secondary.isBlank()) 0.0 else parseDecimal(secondary) ?: return null
                if (inches < 0) return null
                UnitConverter.feetAndInchesToCm(feet, inches)
            }
        }
    }

    fun formatDecimal(value: Double, maxDecimals: Int): String = BigDecimal.valueOf(value)
        .setScale(maxDecimals, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()

    fun parseDecimal(text: String): Double? {
        val trimmed = text.trim()
        if (!decimalPattern.matches(trimmed)) return null
        val value = trimmed.toDouble()
        if (!value.isFinite()) return null
        return value
    }
}
