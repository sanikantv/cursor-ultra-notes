package app.lutex.proteincalculator.core.navigation

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.lutex.proteincalculator.BuildConfig
import app.lutex.proteincalculator.R
import app.lutex.proteincalculator.core.designsystem.emeraldColors
import app.lutex.proteincalculator.feature.calculator.CalculatorScreen
import app.lutex.proteincalculator.feature.calculator.CalculatorViewModel
import app.lutex.proteincalculator.feature.onboarding.OnboardingScreen
import app.lutex.proteincalculator.feature.onboarding.OnboardingViewModel
import app.lutex.proteincalculator.feature.results.ResultsScreen
import app.lutex.proteincalculator.feature.results.ResultsViewModel
import app.lutex.proteincalculator.feature.settings.InfoScreen
import app.lutex.proteincalculator.feature.settings.SettingsScreen
import app.lutex.proteincalculator.feature.settings.SettingsViewModel
import app.lutex.proteincalculator.feature.settings.methodologyParagraphs

@Composable
fun ProteinNavHost(
    onboardingComplete: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val colors = emeraldColors()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBottomBar = route == Routes.CALCULATOR || route == Routes.SETTINGS
    Scaffold(
        modifier = modifier,
        containerColor = colors.canvas,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = colors.canvasSecondary) {
                    BottomDestination.entries.forEach { destination ->
                        val selected = route == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(Routes.CALCULATOR) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (destination == BottomDestination.CALCULATOR) {
                                        Icons.Filled.Calculate
                                    } else {
                                        Icons.Filled.Settings
                                    },
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.onAccent,
                                selectedTextColor = colors.textPrimary,
                                indicatorColor = colors.accent,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.startDestination(onboardingComplete),
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.ONBOARDING) {
                val viewModel: OnboardingViewModel = hiltViewModel()
                val completed by viewModel.completed.collectAsStateWithLifecycle()
                LaunchedEffect(completed) {
                    if (completed) {
                        navController.navigate(Routes.CALCULATOR) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                }
                OnboardingScreen(onFinished = viewModel::complete)
            }
            composable(Routes.CALCULATOR) {
                CalculatorRoute(
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS) {
                            launchSingleTop = true
                        }
                    },
                    onCalculate = { request ->
                        navController.navigate(Routes.results(request))
                    },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsRoute(
                    onDisclaimer = { navController.navigate(Routes.DISCLAIMER) },
                    onPrivacy = { navController.navigate(Routes.PRIVACY) },
                    onAbout = { navController.navigate(Routes.ABOUT) },
                    onMethodology = { navController.navigate(Routes.METHODOLOGY) },
                )
            }
            composable(
                route = Routes.RESULTS,
                arguments = listOf(navArgument("payload") { type = NavType.StringType }),
            ) {
                ResultsRoute(onBack = { navController.popBackStack() })
            }
            composable(Routes.DISCLAIMER) {
                InfoScreen(
                    title = stringResource(R.string.disclaimer_title),
                    paragraphs = listOf(
                        stringResource(R.string.disclaimer_body_1),
                        stringResource(R.string.disclaimer_body_2),
                        stringResource(R.string.disclaimer_body_3),
                    ),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.PRIVACY) {
                InfoScreen(
                    title = stringResource(R.string.privacy_title),
                    paragraphs = listOf(
                        stringResource(R.string.privacy_body_1),
                        stringResource(R.string.privacy_body_2),
                    ),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.ABOUT) {
                InfoScreen(
                    title = stringResource(R.string.about_title),
                    paragraphs = listOf(
                        stringResource(R.string.about_body, BuildConfig.VERSION_NAME),
                    ),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.METHODOLOGY) {
                InfoScreen(
                    title = stringResource(R.string.methodology_title),
                    paragraphs = methodologyParagraphs(),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun CalculatorRoute(
    onOpenSettings: () -> Unit,
    onCalculate: (app.lutex.proteincalculator.domain.model.CalculationRequest) -> Unit,
) {
    val viewModel: CalculatorViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pending by viewModel.pendingResult.collectAsStateWithLifecycle()
    LaunchedEffect(pending?.nonce) {
        val request = pending?.request ?: return@LaunchedEffect
        onCalculate(request)
        viewModel.consumePendingResult()
    }
    CalculatorScreen(
        state = state,
        onOpenSettings = onOpenSettings,
        onAgeChange = viewModel::onAgeChange,
        onSexChange = viewModel::onSexChange,
        onUnitsSelected = viewModel::onUnitsSelected,
        onWeightPrimaryChange = viewModel::onWeightPrimaryChange,
        onWeightSecondaryChange = viewModel::onWeightSecondaryChange,
        onHeightPrimaryChange = viewModel::onHeightPrimaryChange,
        onHeightSecondaryChange = viewModel::onHeightSecondaryChange,
        onActivityChange = viewModel::onActivityChange,
        onActivityExpandedChange = viewModel::onActivityExpandedChange,
        onGoalChange = viewModel::onGoalChange,
        onAdvancedExpandedChange = viewModel::onAdvancedExpandedChange,
        onCustomFactorChange = viewModel::onCustomFactorChange,
        onShowRangeChange = viewModel::onShowRangeChange,
        onUseMidpointChange = viewModel::onUseMidpointChange,
        onCalculate = viewModel::calculate,
    )
}

@Composable
private fun SettingsRoute(
    onDisclaimer: () -> Unit,
    onPrivacy: () -> Unit,
    onAbout: () -> Unit,
    onMethodology: () -> Unit,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        versionName = BuildConfig.VERSION_NAME,
        onTheme = viewModel::setTheme,
        onUnits = viewModel::setUnits,
        onActivity = viewModel::setActivity,
        onGoal = viewModel::setGoal,
        onCustomFactor = viewModel::setCustomFactor,
        onShowRange = viewModel::setShowRange,
        onUseMidpoint = viewModel::setUseMidpoint,
        onDisclaimer = onDisclaimer,
        onPrivacy = onPrivacy,
        onAbout = onAbout,
        onMethodology = onMethodology,
    )
}

@Composable
private fun ResultsRoute(onBack: () -> Unit) {
    val viewModel: ResultsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val shareTitle = stringResource(R.string.action_share)
    LaunchedEffect(state.savedMessage) {
        val message = state.savedMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeSavedMessage()
    }
    Scaffold(
        containerColor = emeraldColors().canvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ResultsScreen(
            state = state,
            onBack = onBack,
            onSelectedGrams = viewModel::onSelectedGrams,
            onMealsChange = viewModel::onMealsChange,
            onSave = viewModel::save,
            onShare = {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, state.shareText)
                }
                context.startActivity(Intent.createChooser(send, shareTitle))
            },
            modifier = Modifier.padding(padding),
        )
    }
}
