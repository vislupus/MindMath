package com.vislupus.mindmath.core

import com.vislupus.mindmath.config.AppConfig
import java.math.BigDecimal
import kotlin.random.Random

/** Groups of methods shown in the Learn and Practice screens. */
enum class Category { ADD_SUB, MULTIPLY, SQUARES, DIVISION, PERCENT }

/** The numbers a problem is built from. Their meaning depends on the method. */
data class Operands(val a: Long, val b: Long, val variant: Int = 0) {
    /** Compact form stored in the database. */
    fun encode(): String = "$a,$b,$variant"

    companion object {
        fun decode(text: String): Operands? {
            val parts = text.split(",").mapNotNull { it.toLongOrNull() }
            return if (parts.size == 3) Operands(parts[0], parts[1], parts[2].toInt()) else null
        }
    }
}

/**
 * One line of a worked solution.
 *
 * @param key  id of the caption text (looked up in the active language)
 * @param expr the calculation itself, language neutral apart from {tokens}
 */
data class Step(val key: String, val expr: String)

/** A problem ready to be shown to the user. */
data class Problem(
    val methodId: String,
    val level: Int,
    val operands: Operands,
    /** Text of the question; may contain {tokens} that are translated in the UI. */
    val question: String,
    val answer: BigDecimal,
    /** Shown after the typed answer, e.g. "%". */
    val suffix: String,
)

/**
 * A mental math technique: it can create problems for a difficulty level and
 * explain, step by step, how to solve any of them.
 */
abstract class MathMethod(val id: String, val category: Category) {

    /** Suffix shown after the answer (e.g. "%"). */
    open val suffix: String = ""

    /** Picks random numbers suited to [level] (1 = easy, 2 = medium, 3 = hard). */
    abstract fun generate(level: Int, rnd: Random): Operands

    /** The question as text, e.g. "47 × 11". */
    abstract fun question(o: Operands): String

    /** The exact answer. */
    abstract fun answer(o: Operands): BigDecimal

    /** The worked solution. The last step always restates the full equation. */
    abstract fun solve(o: Operands): List<Step>

    fun build(level: Int, o: Operands): Problem =
        Problem(id, level, o, question(o), answer(o).normalized(), suffix)

    fun problem(level: Int, rnd: Random = Random.Default): Problem =
        build(level, generate(level.coerceIn(1, MAX_LEVEL), rnd))

    companion object {
        const val MAX_LEVEL = 3
    }
}

// ---- Number helpers shared by all methods ------------------------------------

/** Removes trailing zeros so that 12.50 and 12.5 are the same answer. */
fun BigDecimal.normalized(): BigDecimal =
    if (signum() == 0) BigDecimal.ZERO else stripTrailingZeros().let { if (it.scale() < 0) it.setScale(0) else it }

/** Thin, non-breaking space used to group digits. */
private const val GROUP_SEPARATOR = ' '

private fun groupDigits(digits: String): String {
    val out = StringBuilder()
    for ((index, ch) in digits.withIndex()) {
        if (index > 0 && (digits.length - index) % 3 == 0) out.append(GROUP_SEPARATOR)
        out.append(ch)
    }
    return out.toString()
}

/** Formats a whole number for display: 1564, 12 345, −3. */
fun fmt(value: Long): String {
    val abs = kotlin.math.abs(value)
    val digits = if (abs >= AppConfig.GROUP_DIGITS_FROM) groupDigits(abs.toString()) else abs.toString()
    return if (value < 0) "−$digits" else digits
}

/** Formats a decimal number for display: 27.4, 1 302.25. */
fun fmt(value: BigDecimal): String {
    val plain = value.normalized().abs().toPlainString()
    val whole = plain.substringBefore('.')
    val fraction = plain.substringAfter('.', "")
    val grouped = if (whole.length >= AppConfig.GROUP_DIGITS_FROM.toString().length) groupDigits(whole) else whole
    val text = if (fraction.isEmpty()) grouped else "$grouped.$fraction"
    return if (value.signum() < 0) "−$text" else text
}

internal fun bd(value: Long): BigDecimal = BigDecimal.valueOf(value)

/** Inclusive random range. */
internal fun Random.between(from: Int, to: Int): Long = nextInt(from, to + 1).toLong()

internal fun <T> Random.pick(items: List<T>): T = items[nextInt(items.size)]

internal fun Random.pick(vararg items: Long): Long = items[nextInt(items.size)]

/** Digits of a positive number, most significant first. */
internal fun digitsOf(value: Long): List<Int> = value.toString().map { it - '0' }

/** Non-zero place values, largest first: 478 -> [400, 70, 8]. */
internal fun placeParts(value: Long): List<Long> {
    val text = value.toString()
    return text.mapIndexedNotNull { index, ch ->
        val digit = ch - '0'
        if (digit == 0) null else digit * pow10(text.length - 1 - index)
    }
}

internal fun pow10(exp: Int): Long {
    var result = 1L
    repeat(exp) { result *= 10 }
    return result
}

internal fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
