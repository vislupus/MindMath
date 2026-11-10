package com.vislupus.mindmath.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vislupus.mindmath.ui.theme.AppPalette

/**
 * =============================================================================
 *  THEME CONFIG - how the app looks.
 *
 *  Colours are written as 0xAARRGGBB (AA = opacity, FF = fully opaque).
 *  Sizes use dp (layout) and sp (text).
 * =============================================================================
 */
object ThemeConfig {

    // ---- Font --------------------------------------------------------------

    /**
     * true  = use the Roboto files bundled in app/src/main/res/font
     * false = use the phone's own system font
     */
    const val USE_BUNDLED_ROBOTO = true

    // ---- Colours: light theme ----------------------------------------------

    val Light = AppPalette(
        isDark = false,
        background = Color(0xFFF6F6F3),      // screen background
        surface = Color(0xFFFFFFFF),         // cards
        surfaceAlt = Color(0xFFEFEFEA),      // keypad keys, inactive chips, tracks
        border = Color(0xFFE6E5DF),          // hairlines around cards
        textPrimary = Color(0xFF15161A),
        textSecondary = Color(0xFF5B5E66),
        textMuted = Color(0xFF9B9DA5),
        primary = Color(0xFF4F46E5),         // main accent
        onPrimary = Color(0xFFFFFFFF),       // text on the accent
        primarySoft = Color(0xFFECEDFF),     // tinted backgrounds
        success = Color(0xFF12925A),
        successSoft = Color(0xFFE2F5EB),
        error = Color(0xFFDC3F45),
        errorSoft = Color(0xFFFCE9EA),
        warning = Color(0xFFE08A00),         // streak flame, hints
        warningSoft = Color(0xFFFDF1D8),
        // One colour per method category.
        categoryAddSub = Color(0xFF0284C7),
        categoryMultiply = Color(0xFF6D4AE8),
        categorySquares = Color(0xFFD63384),
        categoryDivision = Color(0xFFE2621B),
        categoryPercent = Color(0xFF0E9F6E),
    )

    // ---- Colours: dark theme -----------------------------------------------

    val Dark = AppPalette(
        isDark = true,
        background = Color(0xFF0D0E12),
        surface = Color(0xFF16171D),
        surfaceAlt = Color(0xFF20222A),
        border = Color(0xFF292B35),
        textPrimary = Color(0xFFF2F2F5),
        textSecondary = Color(0xFFA9ABB6),
        textMuted = Color(0xFF6C6F7D),
        primary = Color(0xFF8E90FF),
        onPrimary = Color(0xFF0D0E12),
        primarySoft = Color(0xFF222447),
        success = Color(0xFF3DD68C),
        successSoft = Color(0xFF12301F),
        error = Color(0xFFFF6B70),
        errorSoft = Color(0xFF3A1A1D),
        warning = Color(0xFFFBBF24),
        warningSoft = Color(0xFF382C0E),
        categoryAddSub = Color(0xFF38BDF8),
        categoryMultiply = Color(0xFFA08BFF),
        categorySquares = Color(0xFFF472B6),
        categoryDivision = Color(0xFFFB923C),
        categoryPercent = Color(0xFF34D399),
    )

    /** Opacity of the tinted square behind category icons (0..1). */
    const val ICON_BADGE_TINT = 0.12f

    // ---- Spacing and sizes -------------------------------------------------

    object Dimens {
        val screenPadding = 20.dp        // left / right margin of every screen
        val sectionGap = 28.dp           // space between sections
        val itemGap = 12.dp              // space between cards in a list
        val cardPadding = 18.dp          // inner padding of cards
        val cardRadius = 22.dp           // corner radius of cards
        val buttonRadius = 18.dp
        val chipRadius = 14.dp
        val keyRadius = 18.dp
        val borderWidth = 1.dp
        val buttonHeight = 56.dp
        val chipHeight = 40.dp
        val keyHeight = 58.dp            // height of one keypad key
        val keyGap = 8.dp
        val iconSize = 22.dp
        val iconBadge = 44.dp            // tinted square behind icons
        val bottomBarHeight = 68.dp
        val progressHeight = 8.dp        // thickness of progress bars
        val ringStroke = 10.dp           // thickness of progress rings
        val chartHeight = 150.dp
    }

    // ---- Text sizes --------------------------------------------------------

    object Type {
        val display = 44.sp              // the problem being solved
        val answer = 38.sp               // the answer being typed
        val h1 = 28.sp                   // screen titles
        val h2 = 20.sp                   // section titles
        val title = 17.sp                // card titles
        val body = 15.sp
        val label = 13.sp
        val caption = 12.sp
        val key = 24.sp                  // keypad digits
        val statValue = 24.sp            // big numbers on tiles
        val stepExpr = 19.sp             // formulas inside worked solutions
    }

    // ---- Animations (milliseconds) -----------------------------------------

    object Motion {
        const val SCREEN_FADE = 220
        const val PROGRESS = 500
        const val STEP_REVEAL = 250
    }
}
