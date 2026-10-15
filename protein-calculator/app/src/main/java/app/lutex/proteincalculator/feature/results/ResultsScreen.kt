package app.lutex.proteincalculator.feature.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.lutex.proteincalculator.R
import app.lutex.proteincalculator.core.designsystem.EmeraldButton
import app.lutex.proteincalculator.core.designsystem.EmeraldOutlinedButton
import app.lutex.proteincalculator.core.designsystem.GlassCard
import app.lutex.proteincalculator.core.designsystem.SectionLabel
import app.lutex.proteincalculator.core.designsystem.SelectableGlassCard
import app.lutex.proteincalculator.core.designsystem.emeraldColors
import app.lutex.proteincalculator.domain.calculator.MealDistribution
import kotlin.math.roundToInt

@Composable
fun ResultsScreen(
    state: ResultsUiState,
    onBack: () -> Unit,
    onSelectedGrams: (Int) -> Unit,
    onMealsChange: (Int) -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = emeraldColors()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = colors.textPrimary,
                )
            }
            Text(
                text = stringResource(R.string.results_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
        }
        if (!state.ready) {
            Text(
                text = state.error ?: stringResource(R.string.results_unavailable),
                color = colors.textPrimary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 24.dp),
            )
            Spacer(Modifier.height(16.dp))
            EmeraldButton(text = stringResource(R.string.action_recalculate), onClick = onBack)
            return
        }
        Spacer(Modifier.height(8.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.results_eyebrow),
                style = MaterialTheme.typography.labelLarge,
                color = colors.mint,
            )
            Text(
                text = stringResource(R.string.results_grams, state.selectedGrams),
                style = MaterialTheme.typography.displayLarge,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.results_caption),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
            )
            if (state.showRange) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.results_range, state.minimumGrams, state.maximumGrams),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                )
            }
            Text(
                text = stringResource(R.string.results_goal, state.goalLabel),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textSecondary,
            )
            if (state.isCustomOverride) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.results_manual_override),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.lime,
                )
            }
        }
        if (state.showRange) {
            Spacer(Modifier.height(16.dp))
            SectionLabel(stringResource(R.string.section_breakdown))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BreakdownCard(stringResource(R.string.breakdown_lower), state.minimumGrams, Modifier.weight(1f))
                BreakdownCard(stringResource(R.string.breakdown_target), state.suggestedGrams, Modifier.weight(1f))
                BreakdownCard(stringResource(R.string.breakdown_upper), state.maximumGrams, Modifier.weight(1f))
            }
        }
        if (state.sliderMax > state.sliderMin) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.slider_label),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Slider(
                value = state.selectedGrams.toFloat(),
                onValueChange = { onSelectedGrams(it.roundToInt()) },
                valueRange = state.sliderMin.toFloat()..state.sliderMax.toFloat(),
                steps = (state.sliderMax - state.sliderMin - 1).coerceAtLeast(0),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("results_slider"),
            )
            Text(
                text = stringResource(R.string.slider_value, state.selectedGrams),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
        Spacer(Modifier.height(8.dp))
        SectionLabel(stringResource(R.string.section_explanation))
        GlassCard {
            ExplanationLine(stringResource(R.string.explain_weight), state.weightLabel)
            ExplanationLine(stringResource(R.string.explain_activity), state.activityLabel)
            ExplanationLine(stringResource(R.string.explain_goal), state.goalLabel)
            ExplanationLine(stringResource(R.string.explain_preset), state.presetLabel)
            ExplanationLine(stringResource(R.string.explain_factor), state.factorLabel)
            ExplanationLine(stringResource(R.string.explain_equation), state.equation)
            ExplanationLine(stringResource(R.string.explain_height), state.heightLabel)
            ExplanationLine(stringResource(R.string.explain_age), state.ageLabel)
            ExplanationLine(stringResource(R.string.explain_sex), state.sexLabel)
            Spacer(Modifier.height(8.dp))
            Text(state.rangeMeaning, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
        Spacer(Modifier.height(16.dp))
        SectionLabel(stringResource(R.string.section_meals))
        Text(
            text = stringResource(R.string.meals_help),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealDistribution.supportedMeals.forEach { count ->
                SelectableGlassCard(
                    title = stringResource(R.string.meal_count, count),
                    body = "",
                    selected = state.meals == count,
                    onClick = { onMealsChange(count) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        GlassCard {
            state.mealPortions.forEachIndexed { index, grams ->
                Text(
                    text = stringResource(R.string.meal_portion, index + 1, grams),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        EmeraldButton(
            text = stringResource(R.string.action_save_goal),
            onClick = onSave,
            modifier = Modifier.testTag("save_goal"),
        )
        Spacer(Modifier.height(8.dp))
        EmeraldOutlinedButton(
            text = stringResource(R.string.action_recalculate),
            onClick = onBack,
            modifier = Modifier.testTag("recalculate"),
        )
        Spacer(Modifier.height(8.dp))
        EmeraldOutlinedButton(
            text = stringResource(R.string.action_share),
            onClick = onShare,
            modifier = Modifier.testTag("share_result"),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BreakdownCard(label: String, grams: Int, modifier: Modifier = Modifier) {
    val colors = emeraldColors()
    GlassCard(modifier) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        Text(
            text = stringResource(R.string.grams_compact, grams),
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun ExplanationLine(label: String, value: String) {
    val colors = emeraldColors()
    Column(Modifier.padding(bottom = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.mint)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
    }
}
