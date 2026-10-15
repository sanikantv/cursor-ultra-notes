package app.lutex.proteincalculator.feature.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.lutex.proteincalculator.R
import app.lutex.proteincalculator.core.designsystem.EmeraldButton
import app.lutex.proteincalculator.core.designsystem.GlassCard
import app.lutex.proteincalculator.core.designsystem.GlassTextField
import app.lutex.proteincalculator.core.designsystem.SectionLabel
import app.lutex.proteincalculator.core.designsystem.SelectableGlassCard
import app.lutex.proteincalculator.core.designsystem.emeraldColors
import app.lutex.proteincalculator.domain.model.ActivityLevel
import app.lutex.proteincalculator.domain.model.FitnessGoal
import app.lutex.proteincalculator.domain.model.MeasurementSystem
import app.lutex.proteincalculator.domain.model.SexReference

@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    onOpenSettings: () -> Unit = {},
    onAgeChange: (String) -> Unit = {},
    onSexChange: (SexReference) -> Unit = {},
    onUnitsSelected: (MeasurementSystem) -> Unit = {},
    onWeightPrimaryChange: (String) -> Unit = {},
    onWeightSecondaryChange: (String) -> Unit = {},
    onHeightPrimaryChange: (String) -> Unit = {},
    onHeightSecondaryChange: (String) -> Unit = {},
    onActivityChange: (ActivityLevel) -> Unit = {},
    onActivityExpandedChange: (Boolean) -> Unit = {},
    onGoalChange: (FitnessGoal) -> Unit = {},
    onAdvancedExpandedChange: (Boolean) -> Unit = {},
    onCustomFactorChange: (String) -> Unit = {},
    onShowRangeChange: (Boolean) -> Unit = {},
    onUseMidpointChange: (Boolean) -> Unit = {},
    onCalculate: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val wide = maxWidth >= 720.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Header(onOpenSettings)
            state.savedGoalGrams?.let { grams ->
                Text(
                    text = stringResource(R.string.saved_goal_banner, grams),
                    style = MaterialTheme.typography.bodyMedium,
                    color = emeraldColors().mint,
                )
            }
            HeroCard()
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        UnitAndDetails(state, onUnitsSelected, onAgeChange, onSexChange, onWeightPrimaryChange, onWeightSecondaryChange, onHeightPrimaryChange, onHeightSecondaryChange)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ActivityAndGoal(state, onActivityChange, onActivityExpandedChange, onGoalChange, onAdvancedExpandedChange, onCustomFactorChange, onShowRangeChange, onUseMidpointChange, onCalculate)
                    }
                }
            } else {
                UnitAndDetails(state, onUnitsSelected, onAgeChange, onSexChange, onWeightPrimaryChange, onWeightSecondaryChange, onHeightPrimaryChange, onHeightSecondaryChange)
                ActivityAndGoal(state, onActivityChange, onActivityExpandedChange, onGoalChange, onAdvancedExpandedChange, onCustomFactorChange, onShowRangeChange, onUseMidpointChange, onCalculate)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit) {
    val colors = emeraldColors()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_app_logo),
            contentDescription = stringResource(R.string.logo_content_description),
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.calculator_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.calculator_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings_title),
                tint = colors.textPrimary,
            )
        }
    }
}

@Composable
private fun HeroCard() {
    val colors = emeraldColors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(listOf(colors.heroStart, colors.heroEnd)),
            )
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.hero_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onAccent,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.hero_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onAccent.copy(alpha = 0.86f),
        )
    }
}

@Composable
private fun UnitAndDetails(
    state: CalculatorUiState,
    onUnitsSelected: (MeasurementSystem) -> Unit,
    onAgeChange: (String) -> Unit,
    onSexChange: (SexReference) -> Unit,
    onWeightPrimaryChange: (String) -> Unit,
    onWeightSecondaryChange: (String) -> Unit,
    onHeightPrimaryChange: (String) -> Unit,
    onHeightSecondaryChange: (String) -> Unit,
) {
    val colors = emeraldColors()
    SectionLabel(stringResource(R.string.section_units))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        MeasurementSystem.entries.forEach { system ->
            val selected = state.units == system
            Text(
                text = system.label,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (selected) colors.accent else colors.glass)
                    .clickable { onUnitsSelected(system) }
                    .padding(horizontal = 8.dp, vertical = 14.dp)
                    .semantics { contentDescription = system.label }
                    .testTag("unit_${system.name}"),
                color = if (selected) colors.onAccent else colors.textPrimary,
                style = MaterialTheme.typography.labelLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
    Text(
        text = state.units.detail,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textSecondary,
    )
    SectionLabel(stringResource(R.string.section_personal))
    GlassTextField(
        value = state.ageText,
        onValueChange = onAgeChange,
        label = stringResource(R.string.label_age),
        error = state.ageError,
        supporting = stringResource(R.string.support_age),
        keyboardType = KeyboardType.Number,
    )
    Spacer(Modifier.height(8.dp))
    Text(stringResource(R.string.label_sex), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
    Text(
        text = stringResource(R.string.support_sex),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textSecondary,
    )
    Spacer(Modifier.height(8.dp))
    SexReference.entries.forEach { option ->
        SelectableGlassCard(
            title = option.label,
            body = "",
            selected = state.sex == option,
            onClick = { onSexChange(option) },
            modifier = Modifier.padding(bottom = 8.dp),
        )
    }
    WeightFields(state, onWeightPrimaryChange, onWeightSecondaryChange)
    Spacer(Modifier.height(8.dp))
    HeightFields(state, onHeightPrimaryChange, onHeightSecondaryChange)
}

@Composable
private fun WeightFields(
    state: CalculatorUiState,
    onPrimary: (String) -> Unit,
    onSecondary: (String) -> Unit,
) {
    when (state.units) {
        MeasurementSystem.METRIC -> GlassTextField(
            value = state.weightPrimaryText,
            onValueChange = onPrimary,
            label = stringResource(R.string.label_weight_kg),
            error = state.weightError,
            supporting = stringResource(R.string.support_weight),
        )
        MeasurementSystem.US -> GlassTextField(
            value = state.weightPrimaryText,
            onValueChange = onPrimary,
            label = stringResource(R.string.label_weight_lb),
            error = state.weightError,
            supporting = stringResource(R.string.support_weight),
        )
        MeasurementSystem.OTHER -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassTextField(
                value = state.weightPrimaryText,
                onValueChange = onPrimary,
                label = stringResource(R.string.label_weight_stone),
                error = state.weightError,
                keyboardType = KeyboardType.Number,
            )
            GlassTextField(
                value = state.weightSecondaryText,
                onValueChange = onSecondary,
                label = stringResource(R.string.label_weight_pounds_remainder),
                supporting = stringResource(R.string.support_weight),
            )
        }
    }
}

@Composable
private fun HeightFields(
    state: CalculatorUiState,
    onPrimary: (String) -> Unit,
    onSecondary: (String) -> Unit,
) {
    when (state.units) {
        MeasurementSystem.METRIC -> GlassTextField(
            value = state.heightPrimaryText,
            onValueChange = onPrimary,
            label = stringResource(R.string.label_height_cm),
            error = state.heightError,
            supporting = stringResource(R.string.support_height),
            imeAction = ImeAction.Done,
        )
        MeasurementSystem.US -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassTextField(
                value = state.heightPrimaryText,
                onValueChange = onPrimary,
                label = stringResource(R.string.label_height_feet),
                error = state.heightError,
                keyboardType = KeyboardType.Number,
            )
            GlassTextField(
                value = state.heightSecondaryText,
                onValueChange = onSecondary,
                label = stringResource(R.string.label_height_inches),
                supporting = stringResource(R.string.support_height),
                imeAction = ImeAction.Done,
            )
        }
        MeasurementSystem.OTHER -> GlassTextField(
            value = state.heightPrimaryText,
            onValueChange = onPrimary,
            label = stringResource(R.string.label_height_inches_total),
            error = state.heightError,
            supporting = stringResource(R.string.support_height),
            imeAction = ImeAction.Done,
        )
    }
}

@Composable
private fun ActivityAndGoal(
    state: CalculatorUiState,
    onActivityChange: (ActivityLevel) -> Unit,
    onActivityExpandedChange: (Boolean) -> Unit,
    onGoalChange: (FitnessGoal) -> Unit,
    onAdvancedExpandedChange: (Boolean) -> Unit,
    onCustomFactorChange: (String) -> Unit,
    onShowRangeChange: (Boolean) -> Unit,
    onUseMidpointChange: (Boolean) -> Unit,
    onCalculate: () -> Unit,
) {
    val colors = emeraldColors()
    SectionLabel(stringResource(R.string.section_activity))
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable { onActivityExpandedChange(!state.activityExpanded) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(state.activity.label, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text(state.activity.description, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
            Icon(
                imageVector = if (state.activityExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = stringResource(
                    if (state.activityExpanded) R.string.collapse_activity else R.string.expand_activity,
                ),
                tint = colors.textPrimary,
            )
        }
        AnimatedVisibility(state.activityExpanded) {
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityLevel.entries.forEach { level ->
                    SelectableGlassCard(
                        title = level.label,
                        body = level.description,
                        selected = state.activity == level,
                        onClick = { onActivityChange(level) },
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.activity_help),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
    SectionLabel(stringResource(R.string.section_goal))
    FitnessGoal.entries.forEach { goal ->
        SelectableGlassCard(
            title = goal.label,
            body = "",
            selected = state.goal == goal,
            onClick = { onGoalChange(goal) },
            modifier = Modifier.padding(bottom = 8.dp),
        )
    }
    SectionLabel(stringResource(R.string.section_advanced))
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable { onAdvancedExpandedChange(!state.advancedExpanded) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.advanced_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Icon(
                imageVector = if (state.advancedExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = stringResource(
                    if (state.advancedExpanded) R.string.collapse_advanced else R.string.expand_advanced,
                ),
                tint = colors.textPrimary,
            )
        }
        AnimatedVisibility(state.advancedExpanded) {
            Column(Modifier.padding(top = 8.dp)) {
                GlassTextField(
                    value = state.customFactorText,
                    onValueChange = onCustomFactorChange,
                    label = stringResource(R.string.label_custom_factor),
                    error = state.customFactorError,
                    supporting = stringResource(R.string.support_custom_factor),
                )
                ToggleRow(
                    title = stringResource(R.string.label_show_range),
                    checked = state.showRange,
                    onCheckedChange = onShowRangeChange,
                )
                ToggleRow(
                    title = stringResource(R.string.label_midpoint),
                    checked = state.useMidpoint,
                    onCheckedChange = onUseMidpointChange,
                )
                Text(
                    text = stringResource(R.string.advanced_methodology_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        }
    }
    state.formError?.let { message ->
        Text(text = message, color = colors.danger, style = MaterialTheme.typography.bodyMedium)
    }
    EmeraldButton(
        text = stringResource(R.string.action_calculate),
        onClick = onCalculate,
        modifier = Modifier.testTag("calculate_button"),
    )
    Text(
        text = stringResource(R.string.calculator_footnote),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textSecondary,
    )
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = emeraldColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = colors.textPrimary,
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
