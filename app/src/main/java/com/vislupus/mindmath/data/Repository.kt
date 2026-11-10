package com.vislupus.mindmath.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.core.MathMethod
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.core.Operands
import com.vislupus.mindmath.core.SessionSpec
import java.time.LocalDate

/**
 * All reads and writes of the database. Every function blocks, so call them
 * from a background thread (the view model does).
 */
class Repository(context: Context) {

    private val helper = Database(context.applicationContext)
    private val db: SQLiteDatabase get() = helper.writableDatabase

    // ---- Settings ------------------------------------------------------------

    fun loadSettings(): Settings {
        val values = HashMap<String, String>()
        db.rawQuery("SELECT key, value FROM settings", null).use { c ->
            while (c.moveToNext()) values[c.getString(0)] = c.getString(1)
        }
        val defaults = Settings()
        fun flag(key: String, default: Boolean) = values[key]?.let { it == "1" } ?: default
        return Settings(
            language = values["language"] ?: defaults.language,
            theme = values["theme"] ?: defaults.theme,
            dailyGoal = values["daily_goal"]?.toIntOrNull() ?: defaults.dailyGoal,
            showTimer = flag("show_timer", defaults.showTimer),
            showMethodName = flag("show_method_name", defaults.showMethodName),
            haptics = flag("haptics", defaults.haptics),
            rightToLeftInput = flag("rtl_input", defaults.rightToLeftInput),
        )
    }

    fun saveSettings(settings: Settings) {
        fun flag(value: Boolean) = if (value) "1" else "0"
        val values = mapOf(
            "language" to settings.language,
            "theme" to settings.theme,
            "daily_goal" to settings.dailyGoal.toString(),
            "show_timer" to flag(settings.showTimer),
            "show_method_name" to flag(settings.showMethodName),
            "haptics" to flag(settings.haptics),
            "rtl_input" to flag(settings.rightToLeftInput),
        )
        inTransaction {
            for ((key, value) in values) {
                execSQL("INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)", arrayOf(key, value))
            }
        }
    }

    // ---- Sessions and answers ------------------------------------------------

    /** Creates the session row and returns its id. */
    fun startSession(spec: SessionSpec, day: LocalDate, now: Long): Long {
        val values = ContentValues().apply {
            put("mode", spec.mode.name)
            put("method_id", spec.methodId)
            put("level", spec.level)
            put("param", if (spec.seconds > 0) spec.seconds else spec.count)
            put("day", day.toString())
            put("started_at", now)
        }
        return db.insertOrThrow("sessions", null, values)
    }

    fun saveAttempt(sessionId: Long, outcome: Outcome, day: LocalDate, now: Long) {
        val values = ContentValues().apply {
            put("session_id", sessionId)
            put("method_id", outcome.problem.methodId)
            put("level", outcome.problem.level)
            put("operands", outcome.problem.operands.encode())
            put("question", outcome.problem.question)
            put("correct_answer", outcome.problem.answer.toPlainString())
            put("user_answer", outcome.userAnswer)
            put("is_correct", if (outcome.isCorrect) 1 else 0)
            put("time_ms", outcome.timeMs)
            put("hint_used", if (outcome.hintUsed) 1 else 0)
            put("xp", outcome.xp)
            put("day", day.toString())
            put("created_at", now)
        }
        db.insertOrThrow("attempts", null, values)
    }

    fun finishSession(sessionId: Long, outcomes: List<Outcome>, durationMs: Long, now: Long) {
        val values = ContentValues().apply {
            put("ended_at", now)
            put("total", outcomes.size)
            put("correct", outcomes.count { it.isCorrect })
            put("duration_ms", durationMs)
            put("xp", outcomes.sumOf { it.xp })
        }
        db.update("sessions", values, "id = ?", arrayOf(sessionId.toString()))
    }

    fun markLessonViewed(methodId: String, now: Long) {
        db.execSQL("INSERT OR IGNORE INTO lessons (method_id, viewed_at) VALUES (?, ?)", arrayOf<Any>(methodId, now))
    }

    /** Deletes all progress but keeps the settings. */
    fun resetProgress() {
        inTransaction {
            delete("attempts", null, null)
            delete("sessions", null, null)
            delete("lessons", null, null)
        }
    }

    // ---- Statistics ----------------------------------------------------------

    fun loadDashboard(today: LocalDate): Dashboard = Dashboard(
        today = today,
        days = loadDays(),
        methods = loadMethodStats(today),
        mistakes = loadMistakes(),
        bestSpeed = loadBestSpeed(),
        lessonsViewed = loadLessons(),
        perfectSessions = loadPerfectSessions(),
    )

    private fun loadDays(): List<DayStat> {
        val days = ArrayList<DayStat>()
        db.rawQuery(
            "SELECT day, COUNT(*), SUM(is_correct), SUM(time_ms), SUM(xp) FROM attempts GROUP BY day ORDER BY day",
            null,
        ).use { c ->
            while (c.moveToNext()) {
                val day = runCatching { LocalDate.parse(c.getString(0)) }.getOrNull() ?: continue
                days += DayStat(day, c.getInt(1), c.getInt(2), c.getLong(3), c.getInt(4))
            }
        }
        return days
    }

    private fun loadMethodStats(today: LocalDate): Map<String, MethodStat> {
        val stats = HashMap<String, MethodStat>()
        db.rawQuery(
            "SELECT method_id, COUNT(*), SUM(is_correct), SUM(time_ms) FROM attempts GROUP BY method_id",
            null,
        ).use { c ->
            while (c.moveToNext()) {
                val id = c.getString(0)
                stats[id] = MethodStat(id, c.getInt(1), c.getInt(2), c.getLong(3))
            }
        }
        val levels = loadRecommendedLevels(today)
        for ((id, stat) in stats.toMap()) {
            val recent = db.rawQuery(
                "SELECT COALESCE(SUM(is_correct), 0) FROM (SELECT is_correct FROM attempts WHERE method_id = ? ORDER BY id DESC LIMIT ?)",
                arrayOf(id, AppConfig.MASTERY_WINDOW.toString()),
            ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
            stats[id] = stat.copy(recentCorrect = recent, recommendedLevel = levels[id] ?: 1)
        }
        return stats
    }

    /** A method moves up one level for every level it has recently passed. */
    private fun loadRecommendedLevels(today: LocalDate): Map<String, Int> {
        val passed = HashMap<String, MutableSet<Int>>()
        val since = today.minusDays(AppConfig.PROMOTE_WINDOW_DAYS.toLong()).toString()
        db.rawQuery(
            "SELECT method_id, level, COUNT(*), SUM(is_correct) FROM attempts WHERE day >= ? GROUP BY method_id, level",
            arrayOf(since),
        ).use { c ->
            while (c.moveToNext()) {
                val total = c.getInt(2)
                val correct = c.getInt(3)
                if (total >= AppConfig.PROMOTE_MIN_ATTEMPTS && correct >= total * AppConfig.PROMOTE_ACCURACY) {
                    passed.getOrPut(c.getString(0)) { HashSet() }.add(c.getInt(1))
                }
            }
        }
        return passed.mapValues { (_, levels) ->
            var level = 1
            while (level in levels && level < MathMethod.MAX_LEVEL) level++
            level
        }
    }

    /** Problems whose most recent answer is wrong, newest first. */
    private fun loadMistakes(): List<Mistake> {
        val mistakes = ArrayList<Mistake>()
        db.rawQuery(
            """
            SELECT method_id, level, operands, user_answer FROM attempts a
            WHERE is_correct = 0 AND id = (
                SELECT MAX(id) FROM attempts b WHERE b.method_id = a.method_id AND b.operands = a.operands
            )
            ORDER BY id DESC LIMIT ?
            """.trimIndent(),
            arrayOf(AppConfig.MISTAKES_REVIEW_LENGTH.toString()),
        ).use { c ->
            while (c.moveToNext()) {
                val method = Methods.get(c.getString(0)) ?: continue
                if (method !in Methods.enabled) continue
                val operands = Operands.decode(c.getString(2)) ?: continue
                val problem = runCatching { method.build(c.getInt(1), operands) }.getOrNull() ?: continue
                mistakes += Mistake(problem, c.getString(3))
            }
        }
        return mistakes
    }

    private fun loadBestSpeed(): Map<Int, Int> {
        val best = HashMap<Int, Int>()
        db.rawQuery("SELECT param, MAX(correct) FROM sessions WHERE mode = 'SPEED' AND ended_at IS NOT NULL GROUP BY param", null)
            .use { c -> while (c.moveToNext()) best[c.getInt(0)] = c.getInt(1) }
        return best
    }

    private fun loadLessons(): Set<String> {
        val lessons = HashSet<String>()
        db.rawQuery("SELECT method_id FROM lessons", null).use { c -> while (c.moveToNext()) lessons += c.getString(0) }
        return lessons
    }

    private fun loadPerfectSessions(): Int =
        db.rawQuery(
            "SELECT COUNT(*) FROM sessions WHERE ended_at IS NOT NULL AND mode != 'SPEED' AND total >= 5 AND correct = total",
            null,
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

    private inline fun inTransaction(block: SQLiteDatabase.() -> Unit) {
        val database = db
        database.beginTransaction()
        try {
            database.block()
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
}
