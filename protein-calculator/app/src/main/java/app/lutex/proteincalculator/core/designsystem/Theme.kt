package app.lutex.proteincalculator.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.lutex.proteincalculator.domain.model.ThemePreference

val EmeraldCanvas = Color(0xFF002E28)
val EmeraldSecondary = Color(0xFF064C3F)
val GlassTint = Color(0xFF075F4D)
val PrimaryEmerald = Color(0xFF10B981)
val GreenHighlight = Color(0xFF34D399)
val BrightMint = Color(0xFFA7F3D0)
val LimeAccent = Color(0xFF8FEF78)
val PrimaryWhite = Color(0xFFF2FFF8)
val SecondaryText = Color(0xFFB3D5C9)
val LightCanvas = Color(0xFFF5FAF7)
val Ink = Color(0xFF062E28)

@Immutable
data class EmeraldColors(
    val canvas: Color,
    val canvasSecondary: Color,
    val glass: Color,
    val glassHighlight: Color,
    val border: Color,
    val borderBright: Color,
    val accent: Color,
    val accentBright: Color,
    val mint: Color,
    val lime: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val onAccent: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val danger: Color,
    val isDark: Boolean,
)

private val EmeraldDarkColors = EmeraldColors(
    canvas = EmeraldCanvas,
    canvasSecondary = EmeraldSecondary,
    glass = GlassTint,
    glassHighlight = Color(0xFF0E7A64),
    border = Color(0x66D1FAE5),
    borderBright = Color(0x99F2FFF8),
    accent = PrimaryEmerald,
    accentBright = GreenHighlight,
    mint = BrightMint,
    lime = LimeAccent,
    textPrimary = PrimaryWhite,
    textSecondary = SecondaryText,
    onAccent = Ink,
    heroStart = Color(0xFF12B981),
    heroEnd = Color(0xFF064C3F),
    danger = Color(0xFFFFB4A8),
    isDark = true,
)

private val DarkColors = EmeraldColors(
    canvas = Color(0xFF101614),
    canvasSecondary = Color(0xFF1A2420),
    glass = Color(0xFF24312C),
    glassHighlight = Color(0xFF31423B),
    border = Color(0x55D1FAE5),
    borderBright = Color(0x88E7F6EF),
    accent = PrimaryEmerald,
    accentBright = GreenHighlight,
    mint = BrightMint,
    lime = LimeAccent,
    textPrimary = PrimaryWhite,
    textSecondary = Color(0xFFB7C9C0),
    onAccent = Ink,
    heroStart = Color(0xFF0F9F72),
    heroEnd = Color(0xFF16332C),
    danger = Color(0xFFFFB4A8),
    isDark = true,
)

private val LightColors = EmeraldColors(
    canvas = LightCanvas,
    canvasSecondary = Color(0xFFE7F4EE),
    glass = Color(0xFFFDFEFD),
    glassHighlight = Color(0xFFFFFFFF),
    border = Color(0x330F766E),
    borderBright = Color(0x6610B981),
    accent = Color(0xFF0F9F72),
    accentBright = Color(0xFF12B981),
    mint = Color(0xFF047857),
    lime = Color(0xFF3F8F2E),
    textPrimary = Ink,
    textSecondary = Color(0xFF3D6558),
    onAccent = Ink,
    heroStart = Color(0xFF10B981),
    heroEnd = Color(0xFF047857),
    danger = Color(0xFF9F1239),
    isDark = false,
)

val LocalEmerald = staticCompositionLocalOf { EmeraldDarkColors }

private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 56.sp, lineHeight = 60.sp, fontWeight = FontWeight.SemiBold),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
)

@Composable
fun ProteinTheme(
    preference: ThemePreference,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val emerald = when (preference) {
        ThemePreference.SYSTEM -> if (systemDark) EmeraldDarkColors else LightColors
        ThemePreference.LIGHT -> LightColors
        ThemePreference.DARK -> DarkColors
        ThemePreference.EMERALD_DARK -> EmeraldDarkColors
    }
    val scheme = if (emerald.isDark) {
        darkColorScheme(
            primary = emerald.accent,
            onPrimary = emerald.onAccent,
            secondary = emerald.accentBright,
            onSecondary = emerald.onAccent,
            background = emerald.canvas,
            onBackground = emerald.textPrimary,
            surface = emerald.glass,
            onSurface = emerald.textPrimary,
            surfaceVariant = emerald.canvasSecondary,
            onSurfaceVariant = emerald.textSecondary,
            outline = emerald.border,
            error = emerald.danger,
        )
    } else {
        lightColorScheme(
            primary = emerald.accent,
            onPrimary = Color.White,
            secondary = emerald.mint,
            onSecondary = Color.White,
            background = emerald.canvas,
            onBackground = emerald.textPrimary,
            surface = emerald.glass,
            onSurface = emerald.textPrimary,
            surfaceVariant = emerald.canvasSecondary,
            onSurfaceVariant = emerald.textSecondary,
            outline = emerald.border,
            error = emerald.danger,
        )
    }
    CompositionLocalProvider(LocalEmerald provides emerald) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            content = content,
        )
    }
}

@Composable
fun emeraldColors(): EmeraldColors = LocalEmerald.current
