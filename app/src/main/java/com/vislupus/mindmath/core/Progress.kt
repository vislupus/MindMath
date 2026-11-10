package com.vislupus.mindmath.core

import com.vislupus.mindmath.config.AppConfig
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.roundToInt

/** Where the user stands on the level ladder. */
data class LevelInfo(
    val level: Int,
    /** XP collected inside the current level. */
    val xpInLevel: Int,
    /** XP needed to finish the current level. */
    val xpForLevel: Int,
) {
    val progress: Float get() = if (xpForLevel == 0) 0f else xpInLevel.toFloat() / xpForLevel
    val xpToNext: Int get() = xpForLevel - xpInLevel
}

/** Rules for points, levels, streaks and answer checking. */
object Progress {

    /** XP earned for one correct answer. */
    fun xpFor(level: Int, timeMs: Long, hintUsed: Boolean): Int {
        val index = (level - 1).coerceIn(0, AppConfig.XP_LEVEL_MULTIPLIER.lastIndex)
        var xp = AppConfig.XP_PER_CORRECT * AppConfig.XP_LEVEL_MULTIPLIER[index]
        if (timeMs <= targetSeconds(level) * 1000L) xp += AppConfig.XP_SPEED_BONUS
        if (hintUsed) xp *= AppConfig.XP_HINT_FACTOR
        return xp.roundToInt().coerceAtLeast(1)
    }

    fun targetSeconds(level: Int): Int =
        AppConfig.TARGET_SECONDS[(level - 1).coerceIn(0, AppConfig.TARGET_SECONDS.lastIndex)]

    /** Level n is finished after XP_LEVEL_STEP * n more points. */
    fun levelInfo(totalXp: Int): LevelInfo {
        var level = 1
        var remaining = totalXp.coerceAtLeast(0)
        while (remaining >= AppConfig.XP_LEVEL_STEP * level) {
            remaining -= AppConfig.XP_LEVEL_STEP * level
            level++
        }
        return LevelInfo(level, remaining, AppConfig.XP_LEVEL_STEP * level)
    }

    /**
     * Current and best streak from the number of problems solved per day.
     * The current streak stays alive until the end of the day after the last
     * active day.
     */
    fun streaks(problemsPerDay: Map<LocalDate, Int>, today: LocalDate): Pair<Int, Int> {
        val active = problemsPerDay.filterValues { it >= AppConfig.STREAK_MIN_PROBLEMS }.keys.sorted()
        if (active.isEmpty()) return 0 to 0
        var best = 1
        var run = 1
        for (i in 1 until active.size) {
            run = if (active[i - 1].plusDays(1) == active[i]) run + 1 else 1
            if (run > best) best = run
        }
        val last = active.last()
        val current = if (last == today || last == today.minusDays(1)) run else 0
        return current to best
    }

    /** Reads what the user typed; accepts both "." and "," as decimal separator. */
    fun parseAnswer(text: String): BigDecimal? =
        text.trim().replace(',', '.').trimEnd('.').takeIf { it.isNotEmpty() }?.toBigDecimalOrNull()

    fun isCorrect(problem: Problem, typed: String): Boolean =
        parseAnswer(typed)?.let { it.compareTo(problem.answer) == 0 } ?: false
}
