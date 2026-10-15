package app.lutex.proteincalculator.feature

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.lutex.proteincalculator.core.navigation.BottomDestination
import app.lutex.proteincalculator.core.navigation.Routes
import app.lutex.proteincalculator.data.InMemoryUserPreferencesRepository
import app.lutex.proteincalculator.data.preferences.DataStoreUserPreferencesRepository
import app.lutex.proteincalculator.data.preferences.PreferenceCodec
import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.CalculationRequest
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.PlanningPreset
import app.lutex.proteincalculator.domain.model.SavedDailyGoal
import app.lutex.proteincalculator.domain.model.SexReference
import app.lutex.proteincalculator.domain.model.ThemePreference
import app.lutex.proteincalculator.domain.usecase.CalculateProteinUseCase
import app.lutex.proteincalculator.domain.usecase.CompleteOnboardingUseCase
import app.lutex.proteincalculator.domain.usecase.ObserveUserPreferencesUseCase
import app.lutex.proteincalculator.domain.usecase.SaveDailyGoalUseCase
import app.lutex.proteincalculator.domain.usecase.UpdateUserPreferencesUseCase
import app.lutex.proteincalculator.feature.calculator.CalculatorViewModel
import app.lutex.proteincalculator.feature.onboarding.OnboardingViewModel
import app.lutex.proteincalculator.feature.settings.SettingsViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PersistenceAndNavigationTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onboardingCompletionIsRemembered() = runTest {
        val repository = InMemoryUserPreferencesRepository()
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(repository))
        assertFalse(repository.preferences.first().onboardingComplete)
        viewModel.complete()
        assertTrue(repository.preferences.first().onboardingComplete)
        assertTrue(viewModel.completed.value)
    }

    @Test
    fun themeUnitsAndSavedGoalPersistInRepository() = runTest {
        val repository = InMemoryUserPreferencesRepository()
        val settings = SettingsViewModel(
            ObserveUserPreferencesUseCase(repository),
            UpdateUserPreferencesUseCase(repository),
        )
        val collecting = launch { settings.state.collect {} }
        advanceUntilIdle()
        settings.setTheme(ThemePreference.EMERALD_DARK)
        settings.setUnits(MeasurementSystem.US)
        val goal = sampleGoal()
        SaveDailyGoalUseCase(repository).invoke(goal)
        val prefs = repository.preferences.first()
        assertEquals(ThemePreference.EMERALD_DARK, prefs.theme)
        assertEquals(MeasurementSystem.US, prefs.units)
        assertEquals(112, prefs.savedGoal?.selectedGrams)
        advanceUntilIdle()
        assertEquals(112, settings.state.value.savedGoalGrams)
        collecting.cancel()
        assertEquals(goal, PreferenceCodec.decodeGoal(PreferenceCodec.encodeGoal(goal)))
    }

    @Test
    fun dataStoreReloadsThemeAndSavedGoal() = runTest {
        val directory = File("build/tmp/datastore-test").apply { mkdirs() }
        val file = File(directory, "protein-${System.nanoTime()}.preferences_pb")
        val repository = DataStoreUserPreferencesRepository(
            PreferenceDataStoreFactory.create(scope = backgroundScope) { file },
        )
        repository.setTheme(ThemePreference.LIGHT)
        repository.setOnboardingComplete(true)
        repository.saveDailyGoal(sampleGoal())
        val stored = repository.preferences.first { it.savedGoal != null && it.onboardingComplete }
        assertEquals(ThemePreference.LIGHT, stored.theme)
        assertEquals(112, stored.savedGoal?.selectedGrams)
        assertTrue(stored.onboardingComplete)
    }

    @Test
    fun navigationStartsOnOnboardingUntilItIsComplete() {
        assertEquals(Routes.ONBOARDING, Routes.startDestination(onboardingComplete = false))
        assertEquals(Routes.CALCULATOR, Routes.startDestination(onboardingComplete = true))
        assertEquals(listOf(Routes.CALCULATOR, Routes.SETTINGS), BottomDestination.entries.map { it.route })
        assertFalse(BottomDestination.entries.any { it.route == Routes.TRACKER || it.route == Routes.HISTORY })
    }

    @Test
    fun resultsRouteRoundTripsTheCalculationRequest() {
        val request = CalculationRequest(
            weightKg = 70.0,
            heightCm = 180.0,
            age = 34,
            sex = SexReference.UNSPECIFIED,
            activity = ActivityLevel.ACTIVE,
            goal = FitnessGoal.GENERAL_HEALTH,
            customFactor = null,
            showRange = true,
            useMidpoint = true,
            units = MeasurementSystem.METRIC,
        )
        assertEquals(request, CalculationRequest.decode(request.encode()))
        assertTrue(Routes.results(request).startsWith("results/"))
    }

    @Test
    fun validCalculatorInputNavigatesAndInvalidInputDoesNot() {
        val repository = InMemoryUserPreferencesRepository()
        val viewModel = CalculatorViewModel(repository, CalculateProteinUseCase())
        viewModel.onAgeChange("17")
        viewModel.onWeightPrimaryChange("0")
        viewModel.calculate()
        assertNull(viewModel.pendingResult.value)
        assertNotNull(viewModel.state.value.ageError)
        assertNotNull(viewModel.state.value.weightError)

        viewModel.onAgeChange("34")
        viewModel.onWeightPrimaryChange("70")
        viewModel.onActivityChange(ActivityLevel.ACTIVE)
        viewModel.onSexChange(SexReference.FEMALE)
        viewModel.calculate()
        val pending = viewModel.pendingResult.value
        assertNotNull(pending)
        assertEquals(70.0, pending!!.request.weightKg, 0.0)
        assertEquals(SexReference.FEMALE, pending.request.sex)
        assertEquals(112.0, CalculateProteinUseCase().invoke(pending.request.toProteinInput()).suggestedGrams, 0.0)
    }

    @Test
    fun changingUnitsPreservesCanonicalWeight() {
        val viewModel = CalculatorViewModel(InMemoryUserPreferencesRepository(), CalculateProteinUseCase())
        viewModel.onWeightPrimaryChange("70")
        viewModel.onHeightPrimaryChange("180")
        viewModel.onUnitsSelected(MeasurementSystem.US)
        assertEquals(70.0, viewModel.state.value.weightKg!!, 1e-9)
        assertEquals(180.0, viewModel.state.value.heightCm!!, 1e-6)
        assertEquals(MeasurementSystem.US, viewModel.state.value.units)
        viewModel.onUnitsSelected(MeasurementSystem.OTHER)
        assertEquals(70.0, viewModel.state.value.weightKg!!, 1e-9)
        viewModel.onUnitsSelected(MeasurementSystem.METRIC)
        assertEquals("70", viewModel.state.value.weightPrimaryText)
        assertEquals(70.0, viewModel.state.value.weightKg!!, 0.0)
    }

    private fun sampleGoal() = SavedDailyGoal(
        selectedGrams = 112,
        minimumGrams = 98,
        maximumGrams = 126,
        weightKg = 70.0,
        activity = ActivityLevel.ACTIVE,
        goal = FitnessGoal.GENERAL_HEALTH,
        preset = PlanningPreset.ACTIVE_TRAINING,
        factorLower = 1.4,
        factorUpper = 1.8,
        isCustomOverride = false,
        savedAtEpochMs = 1_700_000_000_000,
    )
}
