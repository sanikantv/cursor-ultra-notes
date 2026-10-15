package app.lutex.proteincalculator.data.preferences

import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.PlanningPreset
import app.lutex.proteincalculator.domain.model.SavedDailyGoal

internal object PreferenceCodec {
    private const val SEPARATOR = "|"

    fun encodeGoal(goal: SavedDailyGoal): String = listOf(
        goal.selectedGrams.toString(),
        goal.minimumGrams.toString(),
        goal.maximumGrams.toString(),
        goal.weightKg.toString(),
        goal.activity.name,
        goal.goal.name,
        goal.preset.name,
        goal.factorLower.toString(),
        goal.factorUpper.toString(),
        goal.isCustomOverride.toString(),
        goal.savedAtEpochMs.toString(),
    ).joinToString(SEPARATOR)

    fun decodeGoal(raw: String): SavedDailyGoal? {
        val parts = raw.split(SEPARATOR)
        if (parts.size != 11) return null
        return try {
            SavedDailyGoal(
                selectedGrams = parts[0].toInt(),
                minimumGrams = parts[1].toInt(),
                maximumGrams = parts[2].toInt(),
                weightKg = parts[3].toDouble(),
                activity = ActivityLevel.valueOf(parts[4]),
                goal = FitnessGoal.valueOf(parts[5]),
                preset = PlanningPreset.valueOf(parts[6]),
                factorLower = parts[7].toDouble(),
                factorUpper = parts[8].toDouble(),
                isCustomOverride = parts[9].toBooleanStrict(),
                savedAtEpochMs = parts[10].toLong(),
            )
        } catch (_: RuntimeException) {
            null
        }
    }
}
