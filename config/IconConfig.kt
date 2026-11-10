package com.vislupus.mindmath.config

import com.vislupus.mindmath.R

/**
 * =============================================================================
 *  ICON CONFIG - which icon is used where.
 *
 *  All icons come from Lucide (https://lucide.dev/icons/) and live in
 *  app/src/main/res/drawable as lucide_<name>.xml.
 *
 *  To use another icon:
 *    1. python tools/add_icon.py <lucide-name>
 *    2. point the entry below at R.drawable.lucide_<name>
 * =============================================================================
 */
object AppIcons {
    // Bottom navigation
    val navHome = R.drawable.lucide_house
    val navLearn = R.drawable.lucide_graduation_cap
    val navPractice = R.drawable.lucide_dumbbell
    val navStats = R.drawable.lucide_chart_column

    // Method categories
    val categoryAddSub = R.drawable.lucide_diff
    val categoryMultiply = R.drawable.lucide_x
    val categorySquares = R.drawable.lucide_superscript
    val categoryDivision = R.drawable.lucide_divide
    val categoryPercent = R.drawable.lucide_percent

    // Practice modes
    val dailyWorkout = R.drawable.lucide_sparkles
    val speedRound = R.drawable.lucide_zap
    val mistakes = R.drawable.lucide_rotate_ccw
    val drill = R.drawable.lucide_target

    // Progress
    val streak = R.drawable.lucide_flame
    val level = R.drawable.lucide_trophy
    val xp = R.drawable.lucide_star
    val goal = R.drawable.lucide_target
    val accuracy = R.drawable.lucide_circle_check
    val time = R.drawable.lucide_clock
    val problems = R.drawable.lucide_hash
    val calendar = R.drawable.lucide_calendar_days
    val trend = R.drawable.lucide_trending_up
    val mastered = R.drawable.lucide_circle_check

    // Actions
    val settings = R.drawable.lucide_settings
    val back = R.drawable.lucide_arrow_left
    val forward = R.drawable.lucide_arrow_right
    val close = R.drawable.lucide_x
    val chevron = R.drawable.lucide_chevron_right
    val expand = R.drawable.lucide_chevron_down
    val collapse = R.drawable.lucide_chevron_up
    val play = R.drawable.lucide_play
    val check = R.drawable.lucide_check
    val backspace = R.drawable.lucide_delete
    val hint = R.drawable.lucide_lightbulb
    val skip = R.drawable.lucide_skip_forward
    val newExample = R.drawable.lucide_refresh_cw
    val inputDirection = R.drawable.lucide_arrow_left_right
    val correct = R.drawable.lucide_circle_check
    val wrong = R.drawable.lucide_circle_x
    val lesson = R.drawable.lucide_book_open
    val tip = R.drawable.lucide_lightbulb

    // Settings rows
    val language = R.drawable.lucide_languages
    val theme = R.drawable.lucide_sun_moon
    val timer = R.drawable.lucide_timer
    val methodName = R.drawable.lucide_eye
    val haptics = R.drawable.lucide_vibrate
    val reset = R.drawable.lucide_trash_2
    val about = R.drawable.lucide_info
    val storage = R.drawable.lucide_database

    // Achievements
    val achFirstSteps = R.drawable.lucide_footprints
    val achHundred = R.drawable.lucide_medal
    val achThousand = R.drawable.lucide_crown
    val achWeek = R.drawable.lucide_flame
    val achMonth = R.drawable.lucide_rocket
    val achPerfect = R.drawable.lucide_award
    val achSpeed = R.drawable.lucide_gauge
    val achExplorer = R.drawable.lucide_list_checks
    val achMaster = R.drawable.lucide_trophy
    val achScholar = R.drawable.lucide_graduation_cap
}
