package app.lutex.proteincalculator.domain.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.floor

/** Exact conversion constants. Calculations use kilograms and centimeters internally. */
object UnitConverter {
    const val KG_PER_LB: Double = 0.45359237
    const val CM_PER_INCH: Double = 2.54
    const val LB_PER_STONE: Double = 14.0

    fun kgToLb(kg: Double): Double = kg / KG_PER_LB

    fun lbToKg(lb: Double): Double = lb * KG_PER_LB

    fun cmToInches(cm: Double): Double = cm / CM_PER_INCH

    fun inchesToCm(inches: Double): Double = inches * CM_PER_INCH

    fun kgToStoneAndPounds(kg: Double): Pair<Int, Double> {
        val totalLb = kgToLb(kg)
        var stones = floor(totalLb / LB_PER_STONE).toInt()
        var pounds = totalLb - (stones * LB_PER_STONE)
        val roundedPounds = BigDecimal.valueOf(pounds).setScale(2, RoundingMode.HALF_UP)
        if (roundedPounds.toDouble() >= LB_PER_STONE) {
            stones += 1
            pounds = 0.0
        }
        return stones to pounds
    }

    fun stoneAndPoundsToKg(stones: Int, pounds: Double): Double {
        val totalLb = (stones * LB_PER_STONE) + pounds
        return lbToKg(totalLb)
    }

    fun cmToFeetAndInches(cm: Double): Pair<Int, Double> {
        val totalInches = cmToInches(cm)
        val feet = floor(totalInches / 12.0).toInt()
        val inches = totalInches - (feet * 12.0)
        return feet to inches
    }

    fun feetAndInchesToCm(feet: Int, inches: Double): Double = inchesToCm((feet * 12.0) + inches)
}
