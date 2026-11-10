package com.vislupus.mindmath.data

import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.core.LevelInfo
import com.vislupus.mindmath.core.Problem
import com.vislupus.mindmath.core.Progress
import java.time.LocalDate

/** User preferences, stored in the settings table. */
data class Settings(
    val language: String = AppConfig.DEFAULT_LANGUAGE,
    val theme: String = AppConfig.DEFAULT_THEME,
    val dailyGoal: Int = AppConfig.DEFAULT_DAILY_GOAL,
    val showTimer: Boolean = AppConfig.DEFAULT_SHOW_TIMER,
    val showMethodName: Boolean = AppConfig.DEFAULT_SHOW_METHOD_NAME,
    val haptics: Boolean = AppConfig.DEFAULT_HAPTICS,
    val rightToLeftInput: Boolean = AppConfig.DEFAULT_RIGHT_TO_LEFT_INPUT,
)

/** Totals of one calendar day. */
data class DayStat(
    val day: LocalDate,
    val total: Int = 0,
    val correct: Int = 0,
    val timeMs: Long = 0,
    val xp: Int = 0,
) {
    val accuracy: Float get() = if (total == 0) 0f else correct.toFloat() / total
}

/** Totals of one method. */
data class MethodStat(
    val methodId: String,
    val total: Int = 0,
    val correct: Int = 0,
    val timeMs: Long = 0,
    /** Correct answers among the most recent AppConfig.MASTERY_WINDOW. */
    val recentCorrect: Int = 0,
    /** Level suggested by the adaptive difficulty (1..3). */
    val recommendedLevel: Int = 1,
) {
    val accuracy: Float get() = if (total == 0) 0f else correct.toFloat() / total
    val avgTimeMs: Long get() = if (total == 0) 0 else timeMs / total

    /** 0..1. Needs a full window of answers to reach 1, so one lucky answer is not mastery. */
    val mastery: Float get() = (recentCorrect.toFloat() / AppConfig.MASTERY_WINDOW).coerceIn(0f, 1f)
    val isMastered: Boolean get() = mastery >= AppConfig.MASTERY_DONE
}

/** A problem that was answered wrongly and not yet fixed. */
data class Mistake(val problem: Problem, val userAnswer: String)

/** One answer inside a running session. */
data class Outcome(
    val problem: Problem,
    val userAnswer: String,
    val isCorrect: Boolean,
    val timeMs: Long,
    val hintUsed: Boolean,
    val xp: Int,
)

/** A snapshot of all statistics, read from the database in one go. */
data class Dashboard(
    val today: LocalDate,
    /** Days with at least one answer, oldest first. */
    val days: List<DayStat>,
    val methods: Map<String, MethodStat>,
    val mistakes: List<Mistake>,
    /** Best number of correct answers per speed round length (seconds). */
    val bestSpeed: Map<Int, Int>,
    val lessonsViewed: Set<String>,
    val perfectSessions: Int,
) {
    private val byDay = days.associateBy { it.day }

    fun day(date: LocalDate): DayStat = byDay[date] ?: DayStat(date)

    val todayStat: DayStat get() = day(today)
    val totalProblems: Int = days.sumOf { it.total }
    val totalCorrect: Int = days.sumOf { it.correct }
    val totalTimeMs: Long = days.sumOf { it.timeMs }
    val totalXp: Int = days.sumOf { it.xp }
    val accuracy: Float get() = if (totalProblems == 0) 0f else totalCorrect.toFloat() / totalProblems
    val levelInfo: LevelInfo = Progress.levelInfo(totalXp)

    private val streaks = Progress.streaks(days.associate { it.day to it.total }, today)
    val currentStreak: Int get() = streaks.first
    val bestStreak: Int get() = streaks.second

    fun method(id: String): MethodStat = methods[id] ?: MethodStat(id)

    companion object {
        fun empty(today: LocalDate = LocalDate.now()) =
            Dashboard(today, emptyList(), emptyMap(), emptyList(), emptyMap(), emptySet(), 0)
    }
}
