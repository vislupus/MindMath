package com.vislupus.mindmath.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.vislupus.mindmath.R
import com.vislupus.mindmath.config.TextsEn
import com.vislupus.mindmath.config.ThemeConfig
import com.vislupus.mindmath.core.Category
import com.vislupus.mindmath.texts.Texts

/** All colours of one theme. The values are set in config/ThemeConfig.kt. */
@Immutable
data class AppPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val success: Color,
    val successSoft: Color,
    val error: Color,
    val errorSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val categoryAddSub: Color,
    val categoryMultiply: Color,
    val categorySquares: Color,
    val categoryDivision: Color,
    val categoryPercent: Color,
) {
    fun category(category: Category): Color = when (category) {
        Category.ADD_SUB -> categoryAddSub
        Category.MULTIPLY -> categoryMultiply
        Category.SQUARES -> categorySquares
        Category.DIVISION -> categoryDivision
        Category.PERCENT -> categoryPercent
    }
}

val LocalPalette = staticCompositionLocalOf { ThemeConfig.Light }
val LocalTexts = staticCompositionLocalOf<Texts> { TextsEn }

/** Shortcuts used all over the UI: `palette.primary`, `texts.check`. */
val palette: AppPalette
    @Composable @ReadOnlyComposable get() = LocalPalette.current

val texts: Texts
    @Composable @ReadOnlyComposable get() = LocalTexts.current

/** Roboto from res/font, or the system font if disabled in ThemeConfig. */
val AppFont: FontFamily = if (ThemeConfig.USE_BUNDLED_ROBOTO) {
    FontFamily(
        Font(R.font.roboto_regular, FontWeight.Normal),
        Font(R.font.roboto_medium, FontWeight.Medium),
        Font(R.font.roboto_bold, FontWeight.Bold),
    )
} else {
    FontFamily.Default
}

/** Text styles built from the sizes in ThemeConfig.Type. */
object AppType {
    private fun style(size: androidx.compose.ui.unit.TextUnit, weight: FontWeight, tight: Boolean = false) = TextStyle(
        fontFamily = AppFont,
        fontWeight = weight,
        fontSize = size,
        lineHeight = size * if (tight) 1.15f else 1.4f,
        letterSpacing = if (tight) (-0.3).sp else 0.sp,
    )

    private const val TABULAR = "tnum"

    val display = style(ThemeConfig.Type.display, FontWeight.Bold, tight = true).copy(fontFeatureSettings = TABULAR)
    val answer = style(ThemeConfig.Type.answer, FontWeight.Medium, tight = true).copy(fontFeatureSettings = TABULAR)
    val h1 = style(ThemeConfig.Type.h1, FontWeight.Bold, tight = true)
    val h2 = style(ThemeConfig.Type.h2, FontWeight.Bold, tight = true)
    val title = style(ThemeConfig.Type.title, FontWeight.Medium)
    val body = style(ThemeConfig.Type.body, FontWeight.Normal)
    val bodyMedium = style(ThemeConfig.Type.body, FontWeight.Medium)
    val label = style(ThemeConfig.Type.label, FontWeight.Medium)
    val caption = style(ThemeConfig.Type.caption, FontWeight.Normal)
    val key = style(ThemeConfig.Type.key, FontWeight.Medium, tight = true)
    val statValue = style(ThemeConfig.Type.statValue, FontWeight.Bold, tight = true).copy(fontFeatureSettings = TABULAR)
    val stepExpr = style(ThemeConfig.Type.stepExpr, FontWeight.Medium).copy(fontFeatureSettings = TABULAR)
    val button = style(ThemeConfig.Type.title, FontWeight.Medium)
}

/** true when the app should currently use the dark palette. */
@Composable
fun isDarkTheme(themeSetting: String): Boolean = when (themeSetting) {
    "dark" -> true
    "light" -> false
    else -> isSystemInDarkTheme()
}

/** Wraps the app: provides colours, texts and the Material theme. */
@Composable
fun MindMathTheme(themeSetting: String, texts: Texts, content: @Composable () -> Unit) {
    val dark = isDarkTheme(themeSetting)
    val colors = if (dark) ThemeConfig.Dark else ThemeConfig.Light

    // Keep the status and navigation bar icons readable on our background.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    val scheme = if (dark) {
        darkColorScheme(
            primary = colors.primary, onPrimary = colors.onPrimary,
            background = colors.background, onBackground = colors.textPrimary,
            surface = colors.surface, onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceAlt, onSurfaceVariant = colors.textSecondary,
            surfaceContainerHigh = colors.surface, outline = colors.border,
            error = colors.error,
        )
    } else {
        lightColorScheme(
            primary = colors.primary, onPrimary = colors.onPrimary,
            background = colors.background, onBackground = colors.textPrimary,
            surface = colors.surface, onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceAlt, onSurfaceVariant = colors.textSecondary,
            surfaceContainerHigh = colors.surface, outline = colors.border,
            error = colors.error,
        )
    }
    val base = Typography()
    val typography = Typography(
        bodyLarge = base.bodyLarge.copy(fontFamily = AppFont),
        bodyMedium = base.bodyMedium.copy(fontFamily = AppFont),
        bodySmall = base.bodySmall.copy(fontFamily = AppFont),
        titleLarge = base.titleLarge.copy(fontFamily = AppFont),
        titleMedium = base.titleMedium.copy(fontFamily = AppFont),
        labelLarge = base.labelLarge.copy(fontFamily = AppFont),
        headlineSmall = base.headlineSmall.copy(fontFamily = AppFont),
    )

    CompositionLocalProvider(LocalPalette provides colors, LocalTexts provides texts) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
