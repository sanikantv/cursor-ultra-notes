package app.lutex.proteincalculator.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.lutex.proteincalculator.data.repository.UserPreferencesRepository
import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.SavedDailyGoal
import app.lutex.proteincalculator.domain.model.ThemePreference
import app.lutex.proteincalculator.domain.model.UserPreferences
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DataStoreUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            onboardingComplete = prefs[Keys.ONBOARDING] ?: false,
            theme = prefs[Keys.THEME]?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
                ?: ThemePreference.SYSTEM,
            units = prefs[Keys.UNITS]?.let { runCatching { MeasurementSystem.valueOf(it) }.getOrNull() }
                ?: MeasurementSystem.METRIC,
            defaultActivity = prefs[Keys.ACTIVITY]?.let { runCatching { ActivityLevel.valueOf(it) }.getOrNull() }
                ?: ActivityLevel.MODERATE,
            defaultGoal = prefs[Keys.GOAL]?.let { runCatching { FitnessGoal.valueOf(it) }.getOrNull() }
                ?: FitnessGoal.GENERAL_HEALTH,
            customFactor = prefs[Keys.CUSTOM_FACTOR]?.let { runCatching { BigDecimal(it) }.getOrNull() },
            showRecommendedRange = prefs[Keys.SHOW_RANGE] ?: true,
            useMidpoint = prefs[Keys.USE_MIDPOINT] ?: true,
            savedGoal = prefs[Keys.SAVED_GOAL]?.let(PreferenceCodec::decodeGoal),
        )
    }

    override suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING] = complete }
    }

    override suspend fun setTheme(theme: ThemePreference) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    override suspend fun setUnits(units: MeasurementSystem) {
        dataStore.edit { it[Keys.UNITS] = units.name }
    }

    override suspend fun setDefaultActivity(activity: ActivityLevel) {
        dataStore.edit { it[Keys.ACTIVITY] = activity.name }
    }

    override suspend fun setDefaultGoal(goal: FitnessGoal) {
        dataStore.edit { it[Keys.GOAL] = goal.name }
    }

    override suspend fun setCustomFactor(factor: BigDecimal?) {
        dataStore.edit { prefs ->
            if (factor == null) prefs.remove(Keys.CUSTOM_FACTOR) else prefs[Keys.CUSTOM_FACTOR] = factor.toPlainString()
        }
    }

    override suspend fun setShowRecommendedRange(show: Boolean) {
        dataStore.edit { it[Keys.SHOW_RANGE] = show }
    }

    override suspend fun setUseMidpoint(use: Boolean) {
        dataStore.edit { it[Keys.USE_MIDPOINT] = use }
    }

    override suspend fun saveDailyGoal(goal: SavedDailyGoal) {
        dataStore.edit { it[Keys.SAVED_GOAL] = PreferenceCodec.encodeGoal(goal) }
    }

    private object Keys {
        val ONBOARDING = booleanPreferencesKey("onboarding_complete")
        val THEME = stringPreferencesKey("theme")
        val UNITS = stringPreferencesKey("units")
        val ACTIVITY = stringPreferencesKey("default_activity")
        val GOAL = stringPreferencesKey("default_goal")
        val CUSTOM_FACTOR = stringPreferencesKey("custom_factor")
        val SHOW_RANGE = booleanPreferencesKey("show_range")
        val USE_MIDPOINT = booleanPreferencesKey("use_midpoint")
        val SAVED_GOAL = stringPreferencesKey("saved_goal")
    }
}
