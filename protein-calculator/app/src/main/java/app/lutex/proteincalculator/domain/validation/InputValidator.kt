package app.lutex.proteincalculator.domain.validation

import app.lutex.proteincalculator.domain.calculator.MeasurementFormatter
import java.math.BigDecimal

object ValidationMessages {
    const val AGE_REQUIRED = "Enter your age."
    const val AGE_INVALID = "Enter a whole number of years."
    const val AGE_RANGE = "This general calculator supports ages 18–80."
    const val WEIGHT_REQUIRED = "Enter your body weight."
    const val WEIGHT_INVALID = "Enter a valid weight. Zero, negative, and non-numeric values are not used."
    const val HEIGHT_INVALID = "Enter a height between 90 and 250 cm, or leave height blank."
    const val FACTOR_INVALID = "Enter a factor between 0.4 and 3.5 g/kg, or leave this blank."
}

data class FieldErrors(
    val age: String? = null,
    val weight: String? = null,
    val height: String? = null,
    val customFactor: String? = null,
) {
    val isValid: Boolean
        get() = age == null && weight == null && height == null && customFactor == null
}

object InputValidator {
    private val agePattern = Regex("""\d{1,3}""")
    val minimumCustomFactor: BigDecimal = BigDecimal("0.4")
    val maximumCustomFactor: BigDecimal = BigDecimal("3.5")
    const val MIN_AGE = 18
    const val MAX_AGE = 80
    const val MIN_HEIGHT_CM = 90.0
    const val MAX_HEIGHT_CM = 250.0

    fun sanitizeDecimal(raw: String): String {
        val cleaned = raw.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
        val dot = cleaned.indexOf('.')
        if (dot < 0) return cleaned
        val fraction = cleaned.substring(dot + 1).replace(".", "")
        return cleaned.substring(0, dot) + "." + fraction
    }

    fun sanitizeInteger(raw: String, maxDigits: Int): String = raw.filter(Char::isDigit).take(maxDigits)

    fun parseAge(text: String): Int? {
        val trimmed = text.trim()
        if (!agePattern.matches(trimmed)) return null
        return trimmed.toIntOrNull()
    }

    fun parseCustomFactor(text: String): BigDecimal? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        if (MeasurementFormatter.parseDecimal(trimmed) == null) return null
        return try {
            BigDecimal(trimmed)
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun validate(
        ageText: String,
        weightKg: Double?,
        weightProvided: Boolean,
        heightCm: Double?,
        heightProvided: Boolean,
        customFactorText: String,
    ): FieldErrors = FieldErrors(
        age = validateAge(ageText),
        weight = validateWeight(weightKg, weightProvided),
        height = validateHeight(heightCm, heightProvided),
        customFactor = validateCustomFactor(customFactorText),
    )

    fun validateAge(text: String): String? {
        if (text.isBlank()) return ValidationMessages.AGE_REQUIRED
        val age = parseAge(text) ?: return ValidationMessages.AGE_INVALID
        if (age !in MIN_AGE..MAX_AGE) return ValidationMessages.AGE_RANGE
        return null
    }

    fun validateWeight(weightKg: Double?, provided: Boolean): String? = when {
        !provided -> ValidationMessages.WEIGHT_REQUIRED
        weightKg == null || !weightKg.isFinite() || weightKg <= 0.0 -> ValidationMessages.WEIGHT_INVALID
        else -> null
    }

    fun validateHeight(heightCm: Double?, provided: Boolean): String? = when {
        !provided -> null
        heightCm == null || !heightCm.isFinite() -> ValidationMessages.HEIGHT_INVALID
        heightCm < MIN_HEIGHT_CM || heightCm > MAX_HEIGHT_CM -> ValidationMessages.HEIGHT_INVALID
        else -> null
    }

    fun validateCustomFactor(text: String): String? {
        if (text.isBlank()) return null
        val factor = parseCustomFactor(text) ?: return ValidationMessages.FACTOR_INVALID
        if (factor < minimumCustomFactor || factor > maximumCustomFactor) {
            return ValidationMessages.FACTOR_INVALID
        }
        return null
    }
}
