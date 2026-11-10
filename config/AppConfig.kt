package com.vislupus.mindmath.config

/**
 * =============================================================================
 *  APP CONFIG - how the app behaves.
 *
 *  Change a value here, run the app again, done. Nothing in this file requires
 *  touching the source code in app/src.
 *
 *  Other config files in this folder:
 *    build.properties  app id, version number, signing
 *    ThemeConfig.kt    colours, sizes, shapes, font, animations
 *    IconConfig.kt     which Lucide icon is used where
 *    TextsEn.kt        every English text
 *    TextsBg.kt        every Bulgarian text
 * =============================================================================
 */
object AppConfig {

    // ---- Storage (everything stays inside the app sandbox) -----------------

    /** Folder inside the app's private files directory that holds the database. */
    const val DATABASE_DIR = "database"

    /** SQLite file name. Do not rename after release or existing data is not found. */
    const val DATABASE_NAME = "mindmath.db"

    // ---- First-launch defaults (the user can change these in Settings) -----

    /** "en" or "bg". */
    const val DEFAULT_LANGUAGE = "en"

    /** "system", "light" or "dark". */
    const val DEFAULT_THEME = "system"

    /** Problems per day needed to reach the daily goal. */
    const val DEFAULT_DAILY_GOAL = 20

    /** Choices offered in Settings for the daily goal. */
    val DAILY_GOAL_OPTIONS = listOf(10, 20, 30, 50)

    /** Show the stopwatch while solving. */
    const val DEFAULT_SHOW_TIMER = true

    /** Show which method a problem belongs to (acts as a small hint). */
    const val DEFAULT_SHOW_METHOD_NAME = true

    /** Vibrate on key presses and answers. */
    const val DEFAULT_HAPTICS = true

    /** Type answers from the last digit to the first (handy for x11, criss-cross). */
    const val DEFAULT_RIGHT_TO_LEFT_INPUT = false

    // ---- Practice sessions -------------------------------------------------

    /** Number of problems in the daily workout. */
    const val DAILY_WORKOUT_LENGTH = 12

    /** Choices for the length of a method drill, and the preselected one. */
    val DRILL_LENGTH_OPTIONS = listOf(5, 10, 20)
    const val DEFAULT_DRILL_LENGTH = 10

    /** Choices for the speed round (seconds), and the preselected one. */
    val SPEED_ROUND_OPTIONS = listOf(60, 120, 180)
    const val DEFAULT_SPEED_ROUND_SECONDS = 60

    /** Difficulty used in the speed round: 1 = easy, 2 = medium, 3 = hard. */
    const val SPEED_ROUND_LEVEL = 1

    /** Maximum number of problems in a mistakes review. */
    const val MISTAKES_REVIEW_LENGTH = 10

    /** How long the green "correct" state stays before the next problem (ms). */
    const val CORRECT_FEEDBACK_DELAY_MS = 600L

    /** Longest answer that can be typed. */
    const val MAX_ANSWER_LENGTH = 10

    /** A single answer never counts as longer than this (app left open, etc.). */
    const val MAX_ATTEMPT_SECONDS = 180

    // ---- Difficulty --------------------------------------------------------

    /** Target time per problem for easy / medium / hard. Faster = speed bonus. */
    val TARGET_SECONDS = listOf(8, 15, 30)

    /**
     * Adaptive difficulty: a method moves up one level when, at the current
     * level, at least PROMOTE_MIN_ATTEMPTS of the recent answers exist and
     * PROMOTE_ACCURACY of them are correct.
     */
    const val PROMOTE_MIN_ATTEMPTS = 6
    const val PROMOTE_ACCURACY = 0.8
    const val PROMOTE_WINDOW_DAYS = 30

    // ---- Experience points and levels --------------------------------------

    /** XP for a correct answer at easy level. */
    const val XP_PER_CORRECT = 10

    /** Multiplier for easy / medium / hard. */
    val XP_LEVEL_MULTIPLIER = listOf(1.0, 1.5, 2.0)

    /** Extra XP for answering faster than the target time. */
    const val XP_SPEED_BONUS = 5

    /** Share of XP kept when a hint was used (0.5 = half). */
    const val XP_HINT_FACTOR = 0.5

    /** XP needed for level 2. Every next level needs this much more than the last. */
    const val XP_LEVEL_STEP = 100

    // ---- Mastery (the progress bar of each method) -------------------------

    /** Mastery looks at this many most recent answers of a method. */
    const val MASTERY_WINDOW = 20

    /** A method counts as mastered at this share of correct recent answers. */
    const val MASTERY_DONE = 0.9

    // ---- Streak ------------------------------------------------------------

    /** Problems needed on a day for it to count towards the streak. */
    const val STREAK_MIN_PROBLEMS = 5

    // ---- Achievements ------------------------------------------------------

    /** Targets of the achievements shown on the statistics screen. */
    object Achievements {
        const val FIRST_STEPS_PROBLEMS = 10
        const val HUNDRED_PROBLEMS = 100
        const val THOUSAND_PROBLEMS = 1000
        const val WEEK_STREAK_DAYS = 7
        const val MONTH_STREAK_DAYS = 30
        const val SPEED_ROUND_SECONDS = 60
        const val SPEED_ROUND_SCORE = 15
        const val MASTERED_METHODS = 5
    }

    // ---- Statistics --------------------------------------------------------

    /** Ranges offered for the activity chart (days), and the preselected one. */
    val CHART_RANGE_OPTIONS = listOf(7, 30)
    const val DEFAULT_CHART_RANGE = 7

    /** Number of weeks shown in the calendar heat map. */
    const val HEATMAP_WEEKS = 15

    // ---- Numbers -----------------------------------------------------------

    /** Numbers from this size on are grouped in threes (12 345). */
    const val GROUP_DIGITS_FROM = 10_000L

    // ---- Methods -----------------------------------------------------------

    /**
     * Methods shown in the app, in this order. Remove a line to hide a method,
     * move a line to reorder it. The ids are defined in
     * app/src/main/java/com/vislupus/mindmath/core/methods.
     */
    val ENABLED_METHODS = listOf(
        // Addition and subtraction
        "add_ltr",
        "add_round",
        "sub_round",
        "sub_complement",
        // Multiplication
        "mul_11",
        "mul_5_25",
        "mul_9_99",
        "mul_double_halve",
        "mul_ltr",
        "mul_split",
        "mul_cross",
        "mul_near_100",
        "mul_diff_squares",
        // Squares
        "sq_5",
        "sq_any",
        // Division
        "div_short",
        "div_5_25",
        "div_factor",
        "div_chunk",
        // Percentages
        "pct_blocks",
        "pct_flip",
        "pct_change",
        "pct_of",
    )
}
