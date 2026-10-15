package app.lutex.proteincalculator.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

private val GlassShape = RoundedCornerShape(24.dp)
private val PillShape = RoundedCornerShape(percent = 50)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = emeraldColors()
    Column(
        modifier = modifier
            .clip(GlassShape)
            .background(
                Brush.linearGradient(
                    listOf(colors.glassHighlight.copy(alpha = 0.96f), colors.glass.copy(alpha = 0.92f)),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(colors.borderBright, colors.border)),
                shape = GlassShape,
            )
            .padding(20.dp),
        content = content,
    )
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(bottom = 8.dp, top = 4.dp),
        style = MaterialTheme.typography.titleMedium,
        color = emeraldColors().textPrimary,
    )
}

@Composable
fun EmeraldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = emeraldColors()
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accent,
            contentColor = colors.onAccent,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun EmeraldOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = emeraldColors()
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = PillShape,
        border = BorderStroke(1.dp, colors.accent),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    supporting: String? = null,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    imeAction: ImeAction = ImeAction.Next,
) {
    val colors = emeraldColors()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
        label = { Text(label) },
        isError = error != null,
        supportingText = if (error != null || supporting != null) {
            { Text(error ?: supporting.orEmpty()) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.borderBright,
            focusedLabelColor = colors.mint,
            unfocusedLabelColor = colors.textSecondary,
            cursorColor = colors.accent,
            errorBorderColor = colors.danger,
            errorLabelColor = colors.danger,
            errorSupportingTextColor = colors.danger,
            focusedSupportingTextColor = colors.textSecondary,
            unfocusedSupportingTextColor = colors.textSecondary,
            focusedContainerColor = colors.glass.copy(alpha = 0.35f),
            unfocusedContainerColor = colors.glass.copy(alpha = 0.2f),
        ),
    )
}

@Composable
fun SelectableGlassCard(
    title: String,
    body: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = emeraldColors()
    val border by animateColorAsState(
        if (selected) colors.accentBright else colors.border,
        label = "selection-border",
    )
    val fill by animateColorAsState(
        if (selected) colors.accent.copy(alpha = if (colors.isDark) 0.28f else 0.14f) else Color.Transparent,
        label = "selection-fill",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(fill)
            .border(1.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (selected) colors.accent else Color.Transparent)
                .border(2.dp, if (selected) colors.accent else colors.textSecondary, CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            if (body.isNotEmpty()) {
                Text(text = body, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }
    }
}
