package com.vislupus.mindmath.texts

import com.vislupus.mindmath.config.TextsBg
import com.vislupus.mindmath.config.TextsEn

/** Texts that describe one mental math method. */
class MethodText(
    val title: String,
    /** One short line shown in lists. */
    val tagline: String,
    /** How and why the method works. */
    val idea: String,
    /** Everyday situations where it helps. */
    val whenToUse: String,
    /** A practical tip. */
    val tip: String,
    /** Captions of the worked solution, by step key. */
    val steps: Map<String, String>,
)

class AchievementText(val title: String, val description: String)

/**
 * Every text of the app in one language. The actual wording lives in
 * config/TextsEn.kt and config/TextsBg.kt. Placeholders such as {0} and {1}
 * are filled in by the app (see [args]) and must be kept in translations.
 */
class Texts(
    val languageCode: String,
    val languageName: String,
    /** "." or ",". */
    val decimalSeparator: String,

    // General
    val cancel: String,
    val close: String,
    val next: String,
    val done: String,

    // Bottom navigation
    val navHome: String,
    val navLearn: String,
    val navPractice: String,
    val navStats: String,

    // Home
    val greetingMorning: String,
    val greetingAfternoon: String,
    val greetingEvening: String,
    val homeTagline: String,
    val dailyGoal: String,
    val goalProgress: String,
    val goalReached: String,
    val dailyWorkout: String,
    val dailyWorkoutDesc: String,
    val startWorkout: String,
    val streak: String,
    val dayOne: String,
    val dayMany: String,
    val level: String,
    val levelNumber: String,
    val xpToNext: String,
    val xpToday: String,
    val quickPractice: String,
    val speedRound: String,
    val speedRoundDesc: String,
    val mistakesReview: String,
    val mistakesReviewDesc: String,
    val mistakesNone: String,
    val suggested: String,
    val thisWeek: String,

    // Learn
    val learnTitle: String,
    val learnSubtitle: String,
    val theIdea: String,
    val whenToUse: String,
    val stepByStep: String,
    val nextStep: String,
    val newExample: String,
    val practiceThis: String,
    val tipTitle: String,
    val mastery: String,
    val newBadge: String,

    // Categories
    val catAddSub: String,
    val catMultiply: String,
    val catSquares: String,
    val catDivision: String,
    val catPercent: String,

    // Difficulty
    val difficulty: String,
    val levelAdaptive: String,
    val levelEasy: String,
    val levelMedium: String,
    val levelHard: String,

    // Practice
    val practiceTitle: String,
    val practiceSubtitle: String,
    val modes: String,
    val methodDrill: String,
    val methodDrillDesc: String,
    val problemsCount: String,
    val secondsValue: String,

    // Session
    val check: String,
    val skip: String,
    val hint: String,
    val correct: String,
    val wrong: String,
    val correctAnswer: String,
    val yourAnswer: String,
    val skipped: String,
    val howToSolve: String,
    val quitTitle: String,
    val quitMessage: String,
    val quitConfirm: String,
    val quitStay: String,
    val inputDirection: String,

    // Result
    val resultPerfect: String,
    val resultGreat: String,
    val resultGood: String,
    val resultKeepGoing: String,
    val resultCorrect: String,
    val accuracy: String,
    val avgTime: String,
    val xpEarned: String,
    val newRecord: String,
    val mistakesTitle: String,
    val practiceAgain: String,

    // Statistics
    val statsTitle: String,
    val statsSubtitle: String,
    val totalProblems: String,
    val timeTrained: String,
    val bestStreak: String,
    val activity: String,
    val metricProblems: String,
    val metricAccuracy: String,
    val metricTime: String,
    val daysValue: String,
    val calendar: String,
    val less: String,
    val more: String,
    val byMethod: String,
    val answersValue: String,
    val noData: String,
    val noDataDesc: String,
    val achievements: String,
    val bestSpeedRound: String,

    // Settings
    val settingsTitle: String,
    val language: String,
    val theme: String,
    val themeSystem: String,
    val themeLight: String,
    val themeDark: String,
    val dailyGoalDesc: String,
    val showTimer: String,
    val showTimerDesc: String,
    val showMethodName: String,
    val showMethodNameDesc: String,
    val haptics: String,
    val hapticsDesc: String,
    val rtlInput: String,
    val rtlInputDesc: String,
    val preferences: String,
    val dataSection: String,
    val storageInfo: String,
    val resetProgress: String,
    val resetDesc: String,
    val resetTitle: String,
    val resetMessage: String,
    val resetConfirm: String,
    val about: String,
    val version: String,
    val credits: String,

    // Units
    val unitSeconds: String,
    val unitMinutes: String,
    val unitHours: String,

    /** Monday first. */
    val weekdaysShort: List<String>,

    /** Words used inside questions and solutions: of, carry, rem. */
    val words: Map<String, String>,

    /** Step captions shared by several methods. */
    val commonSteps: Map<String, String>,

    val methods: Map<String, MethodText>,
    val achievementTexts: Map<String, AchievementText>,
) {
    private val decimalPoint = Regex("(?<=\\d)\\.(?=\\d)")

    /** Translates {tokens} and the decimal separator of a question or formula. */
    fun localize(text: String): String {
        var out = text
        for ((token, word) in words) out = out.replace("{$token}", word)
        return if (decimalSeparator == ".") out else decimalPoint.replace(out, decimalSeparator)
    }

    fun method(id: String): MethodText = methods[id] ?: MethodText(id, "", "", "", "", emptyMap())

    /** Caption of a solution step; falls back to the shared captions. */
    fun stepCaption(methodId: String, key: String): String =
        methods[methodId]?.steps?.get(key) ?: commonSteps[key] ?: ""

    fun days(count: Int): String = "$count ${if (count == 1) dayOne else dayMany}"

    companion object {
        /** All available languages; the first one is the fallback. */
        val all: List<Texts> by lazy { listOf(TextsEn, TextsBg) }

        fun forCode(code: String): Texts = all.firstOrNull { it.languageCode == code } ?: all.first()
    }
}

/** Fills the {0}, {1}... placeholders of a text. */
fun String.args(vararg values: Any): String {
    var out = this
    values.forEachIndexed { index, value -> out = out.replace("{$index}", value.toString()) }
    return out
}
