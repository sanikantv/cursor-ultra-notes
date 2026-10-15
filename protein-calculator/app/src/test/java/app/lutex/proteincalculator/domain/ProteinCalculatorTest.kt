package app.lutex.proteincalculator.domain

import app.lutex.proteincalculator.domain.calculator.DisplayNumbers
import app.lutex.proteincalculator.domain.calculator.MealDistribution
import app.lutex.proteincalculator.domain.calculator.ProteinCalculator
import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.CalculationRequest
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.PlanningPreset
import app.lutex.proteincalculator.domain.model.ProteinInput
import app.lutex.proteincalculator.domain.model.SexReference
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProteinCalculatorTest {
    @Test
    fun seventyKilogramsTimesPointEightIsFiftySixGrams() {
        assertEquals(56.0, ProteinCalculator.grams(70.0, 0.8), 0.0)
    }

    @Test
    fun seventyKilogramsAtActiveTrainingIsNinetyEightToOneTwentySix() {
        val result = ProteinCalculator.recommend(
            ProteinInput(70.0, ActivityLevel.ACTIVE, FitnessGoal.GENERAL_HEALTH),
        )
        assertEquals(PlanningPreset.ACTIVE_TRAINING, result.preset)
        assertEquals(98.0, result.minimumGrams, 0.0)
        assertEquals(126.0, result.maximumGrams, 0.0)
        assertEquals(112.0, result.suggestedGrams, 0.0)
        assertEquals(112, DisplayNumbers.roundGrams(result.suggestedGrams))
        assertTrue(result.isAutomaticRecommendation)
    }

    @Test
    fun fiftyFiveKilogramsAtModerateTrainingIsSixtySixToEightyEight() {
        val result = ProteinCalculator.recommend(
            ProteinInput(55.0, ActivityLevel.MODERATE, FitnessGoal.MAINTAIN_MUSCLE),
        )
        assertEquals(66.0, result.minimumGrams, 0.0)
        assertEquals(88.0, result.maximumGrams, 0.0)
        assertEquals(77.0, result.suggestedGrams, 0.0)
    }

    @Test
    fun goalPresetReplacesActivityBaselineAndIsNotStacked() {
        val sedentaryMuscle = ProteinCalculator.recommend(
            ProteinInput(70.0, ActivityLevel.SEDENTARY, FitnessGoal.BUILD_MUSCLE),
        )
        val extraActiveMuscle = ProteinCalculator.recommend(
            ProteinInput(70.0, ActivityLevel.EXTRA_ACTIVE, FitnessGoal.BUILD_MUSCLE),
        )
        assertEquals(PlanningPreset.MUSCLE_BUILDING, sedentaryMuscle.preset)
        assertEquals(sedentaryMuscle.range, extraActiveMuscle.range)
        assertEquals(98.0, sedentaryMuscle.minimumGrams, 0.0)
        assertEquals(140.0, sedentaryMuscle.maximumGrams, 0.0)
    }

    @Test
    fun activityBaselineMappingIsExplicit() {
        assertEquals(
            PlanningPreset.GENERAL_HEALTH,
            ProteinCalculator.selectPreset(ActivityLevel.SEDENTARY, FitnessGoal.GENERAL_HEALTH),
        )
        assertEquals(
            PlanningPreset.LIGHT_FITNESS,
            ProteinCalculator.selectPreset(ActivityLevel.LIGHT, FitnessGoal.MAINTAIN_MUSCLE),
        )
        assertEquals(
            PlanningPreset.MODERATE_TRAINING,
            ProteinCalculator.selectPreset(ActivityLevel.MODERATE, FitnessGoal.GENERAL_HEALTH),
        )
        assertEquals(
            PlanningPreset.ACTIVE_TRAINING,
            ProteinCalculator.selectPreset(ActivityLevel.VERY_ACTIVE, FitnessGoal.GENERAL_HEALTH),
        )
        assertEquals(
            PlanningPreset.ACTIVE_TRAINING,
            ProteinCalculator.selectPreset(ActivityLevel.EXTRA_ACTIVE, FitnessGoal.MAINTAIN_MUSCLE),
        )
        assertEquals(
            PlanningPreset.ENDURANCE,
            ProteinCalculator.selectPreset(ActivityLevel.SEDENTARY, FitnessGoal.ENDURANCE),
        )
        assertEquals(
            PlanningPreset.FAT_LOSS,
            ProteinCalculator.selectPreset(ActivityLevel.LIGHT, FitnessGoal.FAT_LOSS),
        )
    }

    @Test
    fun sameInputIsDeterministic() {
        val input = ProteinInput(70.0, ActivityLevel.ACTIVE, FitnessGoal.GENERAL_HEALTH)
        assertEquals(ProteinCalculator.recommend(input), ProteinCalculator.recommend(input))
    }

    @Test
    fun midpointPreferenceOffUsesLowerBound() {
        val result = ProteinCalculator.recommend(
            ProteinInput(
                weightKg = 70.0,
                activity = ActivityLevel.ACTIVE,
                goal = FitnessGoal.GENERAL_HEALTH,
                preferMidpoint = false,
            ),
        )
        assertEquals(98.0, result.suggestedGrams, 0.0)
    }

    @Test
    fun customFactorIsManualOverrideAndDoesNotReplaceThePresetRange() {
        val result = ProteinCalculator.recommend(
            ProteinInput(
                weightKg = 70.0,
                activity = ActivityLevel.ACTIVE,
                goal = FitnessGoal.BUILD_MUSCLE,
                customFactor = BigDecimal("1.5"),
            ),
        )
        assertTrue(result.isCustomOverride)
        assertFalse(result.isAutomaticRecommendation)
        assertEquals(105.0, result.suggestedGrams, 0.0)
        assertEquals(98.0, result.minimumGrams, 0.0)
        assertEquals(140.0, result.maximumGrams, 0.0)
        assertTrue(result.equation.contains("manual").not())
        assertTrue(ProteinCalculator.methodology().any { it.contains("not an automatic recommendation") })
        assertTrue(ProteinCalculator.methodology().any { it.contains("not stacked") })
    }

    @Test
    fun sexAndHeightAreNotProteinInputs() {
        val base = CalculationRequest(
            weightKg = 70.0,
            heightCm = 180.0,
            age = 30,
            sex = SexReference.MALE,
            activity = ActivityLevel.ACTIVE,
            goal = FitnessGoal.GENERAL_HEALTH,
            customFactor = null,
            showRange = true,
            useMidpoint = true,
            units = MeasurementSystem.METRIC,
        )
        val other = base.copy(sex = SexReference.FEMALE, heightCm = 150.0, age = 70)
        assertEquals(
            ProteinCalculator.recommend(base.toProteinInput()),
            ProteinCalculator.recommend(other.toProteinInput()),
        )
    }

    @Test
    fun mealPortionsAddUpToTheSelectedTarget() {
        listOf(3, 4, 5).forEach { meals ->
            val portions = MealDistribution.distribute(112, meals)
            assertEquals(meals, portions.size)
            assertEquals(112, portions.sum())
        }
    }
}
