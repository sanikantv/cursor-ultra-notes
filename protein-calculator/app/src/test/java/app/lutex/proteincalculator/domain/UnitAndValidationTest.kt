package app.lutex.proteincalculator.domain

import app.lutex.proteincalculator.domain.calculator.MeasurementFormatter
import app.lutex.proteincalculator.domain.calculator.UnitConverter
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.validation.InputValidator
import app.lutex.proteincalculator.domain.validation.ValidationMessages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitAndValidationTest {
    @Test
    fun kilogramsAndPoundsRoundTrip() {
        assertEquals(UnitConverter.KG_PER_LB, UnitConverter.lbToKg(1.0), 0.0)
        assertEquals(70.0, UnitConverter.lbToKg(UnitConverter.kgToLb(70.0)), 1e-9)
        assertEquals(154.32, MeasurementFormatter.formatWeight(70.0, MeasurementSystem.US).primary.toDouble(), 0.01)
    }

    @Test
    fun heightConvertsBetweenCentimetersFeetAndInches() {
        val (feet, inches) = UnitConverter.cmToFeetAndInches(180.0)
        assertEquals(5, feet)
        assertEquals(180.0, UnitConverter.feetAndInchesToCm(feet, inches), 1e-6)
        assertEquals(180.0, UnitConverter.inchesToCm(UnitConverter.cmToInches(180.0)), 1e-6)
        val fields = MeasurementFormatter.formatHeight(180.0, MeasurementSystem.US)
        assertEquals("5", fields.primary)
        val parsed = MeasurementFormatter.parseHeight(fields.primary, fields.secondary, MeasurementSystem.US)
        assertEquals(180.0, parsed!!, 0.2)
    }

    @Test
    fun stonePoundsRoundTrip() {
        val (stones, pounds) = UnitConverter.kgToStoneAndPounds(70.0)
        val kg = UnitConverter.stoneAndPoundsToKg(stones, pounds)
        assertEquals(70.0, kg, 1e-6)
        val fields = MeasurementFormatter.formatWeight(70.0, MeasurementSystem.OTHER)
        val parsed = MeasurementFormatter.parseWeight(fields.primary, fields.secondary, MeasurementSystem.OTHER)
        assertEquals(70.0, parsed!!, 0.02)
    }

    @Test
    fun switchingDisplayedUnitsKeepsCanonicalKilograms() {
        val canonicalKg = 70.0
        val canonicalCm = 175.0
        MeasurementSystem.entries.forEach { system ->
            val weight = MeasurementFormatter.formatWeight(canonicalKg, system)
            val height = MeasurementFormatter.formatHeight(canonicalCm, system)
            assertEquals(
                canonicalKg,
                MeasurementFormatter.parseWeight(weight.primary, weight.secondary, system)!!,
                0.05,
            )
            assertEquals(
                canonicalCm,
                MeasurementFormatter.parseHeight(height.primary, height.secondary, system)!!,
                0.2,
            )
        }
        assertEquals("70", MeasurementFormatter.formatWeight(canonicalKg, MeasurementSystem.METRIC).primary)
    }

    @Test
    fun invalidWeightsAgesHeightsAndFactorsAreRejected() {
        assertEquals(ValidationMessages.WEIGHT_REQUIRED, InputValidator.validateWeight(null, provided = false))
        assertEquals(ValidationMessages.WEIGHT_INVALID, InputValidator.validateWeight(0.0, provided = true))
        assertEquals(ValidationMessages.WEIGHT_INVALID, InputValidator.validateWeight(-4.0, provided = true))
        assertEquals(ValidationMessages.WEIGHT_INVALID, InputValidator.validateWeight(Double.NaN, provided = true))
        assertEquals(ValidationMessages.WEIGHT_INVALID, InputValidator.validateWeight(Double.POSITIVE_INFINITY, provided = true))
        assertEquals(ValidationMessages.WEIGHT_INVALID, InputValidator.validateWeight(null, provided = true))
        assertNull(InputValidator.validateWeight(70.0, provided = true))

        assertEquals(ValidationMessages.AGE_REQUIRED, InputValidator.validateAge(""))
        assertEquals(ValidationMessages.AGE_RANGE, InputValidator.validateAge("17"))
        assertEquals(ValidationMessages.AGE_RANGE, InputValidator.validateAge("81"))
        assertEquals(ValidationMessages.AGE_INVALID, InputValidator.validateAge("18.5"))
        assertNull(InputValidator.validateAge("18"))
        assertNull(InputValidator.validateAge("80"))

        assertNull(InputValidator.validateHeight(null, provided = false))
        assertEquals(ValidationMessages.HEIGHT_INVALID, InputValidator.validateHeight(89.0, provided = true))
        assertEquals(ValidationMessages.HEIGHT_INVALID, InputValidator.validateHeight(251.0, provided = true))
        assertEquals(ValidationMessages.HEIGHT_INVALID, InputValidator.validateHeight(null, provided = true))
        assertNull(InputValidator.validateHeight(180.0, provided = true))

        assertNull(InputValidator.validateCustomFactor(""))
        assertNull(InputValidator.validateCustomFactor("0.4"))
        assertNull(InputValidator.validateCustomFactor("3.5"))
        assertEquals(ValidationMessages.FACTOR_INVALID, InputValidator.validateCustomFactor("0"))
        assertEquals(ValidationMessages.FACTOR_INVALID, InputValidator.validateCustomFactor("-1"))
        assertEquals(ValidationMessages.FACTOR_INVALID, InputValidator.validateCustomFactor("NaN"))
        assertEquals(ValidationMessages.FACTOR_INVALID, InputValidator.validateCustomFactor("Infinity"))
        assertTrue(InputValidator.sanitizeDecimal("-5a").none { it == '-' || it.isLetter() })
    }
}
