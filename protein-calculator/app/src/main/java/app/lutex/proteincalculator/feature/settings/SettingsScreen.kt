package app.lutex.proteincalculator.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.lutex.proteincalculator.R
import app.lutex.proteincalculator.core.designsystem.GlassCard
import app.lutex.proteincalculator.core.designsystem.GlassTextField
import app.lutex.proteincalculator.core.designsystem.SectionLabel
import app.lutex.proteincalculator.core.designsystem.SelectableGlassCard
import app.lutex.proteincalculator.core.designsystem.emeraldColors
import app.lutex.proteincalculator.domain.calculator.ProteinCalculator
import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.ThemePreference
import app.lutex.proteincalculator.domain.validation.InputValidator

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    versionName: String,
    onTheme: (ThemePreference) -> Unit,
    onUnits: (MeasurementSystem) -> Unit,
    onActivity: (ActivityLevel) -> Unit,
    onGoal: (FitnessGoal) -> Unit,
    onCustomFactor: (String) -> Unit,
    onShowRange: (Boolean) -> Unit,
    onUseMidpoint: (Boolean) -> Unit,
    onDisclaimer: () -> Unit,
    onPrivacy: () -> Unit,
    onAbout: () -> Unit,
    onMethodology: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = emeraldColors()
    var factorText by rememberSaveable { mutableStateOf(state.customFactorText) }
    var factorError by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(state.customFactorText) {
        if (factorText != state.customFactorText && factorError == null) {
            factorText = state.customFactorText
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
        )
        Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        state.savedGoalGrams?.let { grams ->
            Text(
                text = stringResource(R.string.saved_goal_banner, grams),
                color = colors.mint,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        SectionLabel(stringResource(R.string.section_appearance))
        ThemePreference.entries.forEach { theme ->
            SelectableGlassCard(
                title = theme.label,
                body = "",
                selected = state.theme == theme,
                onClick = { onTheme(theme) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        SectionLabel(stringResource(R.string.section_units))
        MeasurementSystem.entries.forEach { system ->
            SelectableGlassCard(
                title = system.label,
                body = system.detail,
                selected = state.units == system,
                onClick = { onUnits(system) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        SectionLabel(stringResource(R.string.section_calculation))
        Text(stringResource(R.string.label_default_activity), color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
        ActivityLevel.entries.forEach { level ->
            SelectableGlassCard(
                title = level.label,
                body = level.description,
                selected = state.activity == level,
                onClick = { onActivity(level) },
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        Text(
            text = stringResource(R.string.label_default_goal),
            color = colors.textPrimary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )
        FitnessGoal.entries.forEach { goal ->
            SelectableGlassCard(
                title = goal.label,
                body = "",
                selected = state.goal == goal,
                onClick = { onGoal(goal) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        GlassTextField(
            value = factorText,
            onValueChange = { value ->
                val sanitized = InputValidator.sanitizeDecimal(value)
                factorText = sanitized
                factorError = InputValidator.validateCustomFactor(sanitized)
                if (factorError == null) onCustomFactor(sanitized)
            },
            label = stringResource(R.string.label_custom_factor),
            error = factorError,
            supporting = stringResource(R.string.support_custom_factor),
        )
        ToggleRow(stringResource(R.string.label_show_range), state.showRange, onShowRange)
        ToggleRow(stringResource(R.string.label_midpoint), state.useMidpoint, onUseMidpoint)
        TextButton(onClick = onMethodology, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.action_methodology))
        }
        SectionLabel(stringResource(R.string.section_app))
        GlassCard {
            SettingsLink(stringResource(R.string.action_disclaimer), onDisclaimer)
            SettingsLink(stringResource(R.string.action_privacy), onPrivacy)
            SettingsLink(stringResource(R.string.action_about), onAbout)
            Text(
                text = stringResource(R.string.version_label, versionName),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
fun InfoScreen(
    title: String,
    paragraphs: List<String>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = emeraldColors()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.action_back))
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
        paragraphs.forEach { paragraph ->
            Text(
                text = paragraph,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
fun methodologyParagraphs(): List<String> = ProteinCalculator.methodology()

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f), color = emeraldColors().textPrimary)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsLink(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(label)
    }
}
