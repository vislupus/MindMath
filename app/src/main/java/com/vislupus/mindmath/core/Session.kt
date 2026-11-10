package com.vislupus.mindmath.core

import com.vislupus.mindmath.config.AppConfig
import kotlin.random.Random

/** The kinds of practice the app offers. */
enum class Mode { DAILY, DRILL, SPEED, MISTAKES }

/** Everything needed to start a practice session. */
data class SessionSpec(
    val mode: Mode,
    /** Only for DRILL. */
    val methodId: String? = null,
    /** 1..3, or 0 for adaptive difficulty. */
    val level: Int = 0,
    /** Number of problems (ignored in SPEED). */
    val count: Int = 0,
    /** Length of a SPEED round. */
    val seconds: Int = 0,
)

/**
 * Hands out the problems of one session.
 *
 * @param recommendedLevel best level for a method, based on past answers
 * @param mastery          0..1 per method; weaker methods appear more often
 * @param mistakes         stored problems to repeat (MISTAKES mode)
 */
class ProblemSource(
    private val spec: SessionSpec,
    private val recommendedLevel: (String) -> Int,
    private val mastery: (String) -> Float,
    mistakes: List<Problem> = emptyList(),
    private val rnd: Random = Random.Default,
) {
    private val queue = ArrayDeque(mistakes)
    private val seen = HashSet<String>()
    private var lastMethodId: String? = null

    // Adaptive drill: the level follows the last few answers.
    private var drillLevel = if (spec.level > 0) spec.level else spec.methodId?.let(recommendedLevel) ?: 1
    private var streakCorrect = 0
    private var streakWrong = 0

    /** Number of problems in the session, or null when it is limited by time. */
    val total: Int? = when (spec.mode) {
        Mode.SPEED -> null
        Mode.MISTAKES -> mistakes.size
        else -> spec.count
    }

    /** Tells the source how the last problem went (used by adaptive drills). */
    fun report(correct: Boolean) {
        if (spec.mode != Mode.DRILL || spec.level != 0) return
        if (correct) {
            streakWrong = 0
            if (++streakCorrect >= 3 && drillLevel < MathMethod.MAX_LEVEL) {
                drillLevel++
                streakCorrect = 0
            }
        } else {
            streakCorrect = 0
            if (++streakWrong >= 2 && drillLevel > 1) {
                drillLevel--
                streakWrong = 0
            }
        }
    }

    fun next(): Problem {
        if (spec.mode == Mode.MISTAKES) return queue.removeFirst()
        val method = when (spec.mode) {
            Mode.DRILL -> Methods.get(spec.methodId ?: "") ?: Methods.enabled.first()
            Mode.SPEED -> pickMethod(weighted = false)
            else -> pickMethod(weighted = true)
        }
        val level = when (spec.mode) {
            Mode.DRILL -> drillLevel
            Mode.SPEED -> AppConfig.SPEED_ROUND_LEVEL
            else -> recommendedLevel(method.id)
        }
        lastMethodId = method.id
        // Try a few times to avoid repeating a problem inside one session.
        var problem = method.problem(level, rnd)
        repeat(8) {
            if (seen.add(problem.methodId + problem.operands.encode())) return problem
            problem = method.problem(level, rnd)
        }
        return problem
    }

    /** Random method, never the same twice in a row; weak methods come up more often. */
    private fun pickMethod(weighted: Boolean): MathMethod {
        val pool = Methods.enabled.filter { it.id != lastMethodId }.ifEmpty { Methods.enabled }
        if (!weighted) return pool[rnd.nextInt(pool.size)]
        val weights = pool.map { 1f + 2f * (1f - mastery(it.id).coerceIn(0f, 1f)) }
        var ticket = rnd.nextFloat() * weights.sum()
        for ((index, weight) in weights.withIndex()) {
            ticket -= weight
            if (ticket <= 0f) return pool[index]
        }
        return pool.last()
    }
}
