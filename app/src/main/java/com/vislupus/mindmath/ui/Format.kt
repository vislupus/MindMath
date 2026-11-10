package com.vislupus.mindmath.ui

import androidx.annotation.DrawableRes
import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.core.Category
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.data.Dashboard
import com.vislupus.mindmath.texts.Texts
import java.util.Locale
import kotlin.math.roundToInt

// Small helpers that turn numbers and ids into text for the screens.

/** "1 h 12 min", "12 min" or "45 s". */
fun Texts.duration(ms: Long): String {
    val seconds = ms / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "$hours $unitHours $minutes $unitMinutes"
        minutes > 0 -> "$minutes $unitMinutes"
        else -> "$seconds $unitSeconds"
    }
}

/** "4.2 s" */
fun Texts.seconds(ms: Long): String =
    localize(String.format(Locale.US, "%.1f", ms / 1000.0)) + " " + unitSeconds

/** "87%" */
fun percent(fraction: Float): String = "${(fraction * 100).roundToInt()}%"

/** What the user typed, with the decimal separator of the language. */
fun Texts.typed(input: String): String = input.replace(".", decimalSeparator)

fun Texts.levelName(level: Int): String = when (level) {
    0 -> levelAdaptive
    1 -> levelEasy
    2 -> levelMedium
    else -> levelHard
}

fun Texts.categoryName(category: Category): String = when (category) {
    Category.ADD_SUB -> catAddSub
    Category.MULTIPLY -> catMultiply
    Category.SQUARES -> catSquares
    Category.DIVISION -> catDivision
    Category.PERCENT -> catPercent
}

@DrawableRes
fun categoryIcon(category: Category): Int = when (category) {
    Category.ADD_SUB -> AppIcons.categoryAddSub
    Category.MULTIPLY -> AppIcons.categoryMultiply
    Category.SQUARES -> AppIcons.categorySquares
    Category.DIVISION -> AppIcons.categoryDivision
    Category.PERCENT -> AppIcons.categoryPercent
}

/** One achievement and how close the user is to it (progress 0..1). */
data class Achievement(
    val id: String,
    @DrawableRes val icon: Int,
    val progress: Float,
    /** Numbers filled into the description text. */
    val args: List<Any> = emptyList(),
) {
    val unlocked: Boolean get() = progress >= 1f
}

/** Works out all achievements from the statistics. */
fun achievements(stats: Dashboard): List<Achievement> {
    val goals = AppConfig.Achievements
    val enabled = Methods.enabled.map { it.id }
    fun ratio(value: Int, target: Int) = if (target <= 0) 1f else (value.toFloat() / target).coerceIn(0f, 1f)
    return listOf(
        Achievement("first_steps", AppIcons.achFirstSteps, ratio(stats.totalProblems, goals.FIRST_STEPS_PROBLEMS), listOf(goals.FIRST_STEPS_PROBLEMS)),
        Achievement("hundred", AppIcons.achHundred, ratio(stats.totalProblems, goals.HUNDRED_PROBLEMS), listOf(goals.HUNDRED_PROBLEMS)),
        Achievement("thousand", AppIcons.achThousand, ratio(stats.totalProblems, goals.THOUSAND_PROBLEMS), listOf(goals.THOUSAND_PROBLEMS)),
        Achievement("week", AppIcons.achWeek, ratio(stats.bestStreak, goals.WEEK_STREAK_DAYS), listOf(goals.WEEK_STREAK_DAYS)),
        Achievement("month", AppIcons.achMonth, ratio(stats.bestStreak, goals.MONTH_STREAK_DAYS), listOf(goals.MONTH_STREAK_DAYS)),
        Achievement("perfect", AppIcons.achPerfect, ratio(stats.perfectSessions, 1)),
        Achievement(
            "speed", AppIcons.achSpeed,
            ratio(stats.bestSpeed[goals.SPEED_ROUND_SECONDS] ?: 0, goals.SPEED_ROUND_SCORE),
            listOf(goals.SPEED_ROUND_SCORE, goals.SPEED_ROUND_SECONDS),
        ),
        Achievement("explorer", AppIcons.achExplorer, ratio(enabled.count { stats.method(it).total > 0 }, enabled.size)),
        Achievement("scholar", AppIcons.achScholar, ratio(enabled.count { it in stats.lessonsViewed }, enabled.size)),
        Achievement("master", AppIcons.achMaster, ratio(enabled.count { stats.method(it).isMastered }, goals.MASTERED_METHODS), listOf(goals.MASTERED_METHODS)),
    )
}
