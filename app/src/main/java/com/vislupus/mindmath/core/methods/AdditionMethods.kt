package com.vislupus.mindmath.core.methods

import com.vislupus.mindmath.core.Category
import com.vislupus.mindmath.core.MathMethod
import com.vislupus.mindmath.core.Operands
import com.vislupus.mindmath.core.Step
import com.vislupus.mindmath.core.bd
import com.vislupus.mindmath.core.between
import com.vislupus.mindmath.core.fmt
import com.vislupus.mindmath.core.pick
import com.vislupus.mindmath.core.placeParts
import com.vislupus.mindmath.core.pow10
import java.math.BigDecimal
import kotlin.random.Random

/** 478 + 256: add the hundreds, then the tens, then the ones. */
object AddLeftToRight : MathMethod("add_ltr", Category.ADD_SUB) {
    override fun generate(level: Int, rnd: Random): Operands {
        // The second number needs at least two parts, otherwise there is nothing to split.
        fun second(from: Int, to: Int): Long {
            var value: Long
            do value = rnd.between(from, to) while (placeParts(value).size < 2)
            return value
        }
        return when (level) {
            1 -> Operands(rnd.between(12, 89), second(12, 89))
            2 -> Operands(rnd.between(101, 899), second(101, 899))
            else -> Operands(rnd.between(1001, 8999), second(101, 8999))
        }
    }

    override fun question(o: Operands) = "${fmt(o.a)} + ${fmt(o.b)}"
    override fun answer(o: Operands): BigDecimal = bd(o.a + o.b)

    override fun solve(o: Operands): List<Step> {
        val steps = mutableListOf<Step>()
        var running = o.a
        for (part in placeParts(o.b)) {
            val key = when {
                part >= 1000 -> "add_thousands"
                part >= 100 -> "add_hundreds"
                part >= 10 -> "add_tens"
                else -> "add_ones"
            }
            steps += Step(key, "${fmt(running)} + ${fmt(part)} = ${fmt(running + part)}")
            running += part
        }
        steps += Step("result", "${question(o)} = ${fmt(o.a + o.b)}")
        return steps
    }
}

/**
 * Finds the round number just above [value] and how far away it is.
 * The biggest round number that is still close wins: 1988 -> 2000 (12 away).
 */
internal fun roundUpTarget(value: Long): Pair<Long, Long> {
    val limits = listOf(1000L to 50L, 100L to 5L, 10L to 3L)
    for ((unit, limit) in limits) {
        val rounded = (value + unit - 1) / unit * unit
        val diff = rounded - value
        if (diff in 1..limit) return rounded to diff
    }
    val rounded = (value + 9) / 10 * 10
    return rounded to (rounded - value)
}

/** A number just below a round one: 38, 197, 2995. */
internal fun nearRound(level: Int, rnd: Random): Long = when (level) {
    1 -> rnd.between(2, 9) * 10 - rnd.between(1, 3)
    2 -> if (rnd.nextBoolean()) rnd.between(1, 9) * 100 - rnd.between(1, 5) else rnd.between(3, 9) * 10 - rnd.between(1, 3)
    else -> if (rnd.nextBoolean()) rnd.between(1, 9) * 1000 - rnd.between(1, 50) else rnd.between(11, 89) * 100 - rnd.between(1, 5)
}

/** 47 + 38: add 40, then take 2 back. */
object AddByRounding : MathMethod("add_round", Category.ADD_SUB) {
    override fun generate(level: Int, rnd: Random): Operands {
        val b = nearRound(level, rnd)
        val a = when (level) {
            1 -> rnd.between(12, 89)
            2 -> rnd.between(101, 899)
            else -> rnd.between(1001, 8999)
        }
        return Operands(a, b)
    }

    override fun question(o: Operands) = "${fmt(o.a)} + ${fmt(o.b)}"
    override fun answer(o: Operands): BigDecimal = bd(o.a + o.b)

    override fun solve(o: Operands): List<Step> {
        val (rounded, diff) = roundUpTarget(o.b)
        val steps = mutableListOf<Step>()
        if (diff > 0) {
            steps += Step("round_up", "${fmt(o.b)} = ${fmt(rounded)} − ${fmt(diff)}")
            steps += Step("add_rounded", "${fmt(o.a)} + ${fmt(rounded)} = ${fmt(o.a + rounded)}")
            steps += Step("take_back", "${fmt(o.a + rounded)} − ${fmt(diff)} = ${fmt(o.a + o.b)}")
        }
        steps += Step("result", "${question(o)} = ${fmt(o.a + o.b)}")
        return steps
    }
}

/** 83 − 47: subtract 50, then give 3 back. */
object SubtractByRounding : MathMethod("sub_round", Category.ADD_SUB) {
    override fun generate(level: Int, rnd: Random): Operands {
        val b = nearRound(level, rnd)
        val rounded = roundUpTarget(b).first
        val a = when (level) {
            1 -> rounded + rnd.between(1, 40)
            2 -> rounded + rnd.between(5, 600)
            else -> rounded + rnd.between(15, 5000)
        }
        return Operands(a, b)
    }

    override fun question(o: Operands) = "${fmt(o.a)} − ${fmt(o.b)}"
    override fun answer(o: Operands): BigDecimal = bd(o.a - o.b)

    override fun solve(o: Operands): List<Step> {
        val (rounded, diff) = roundUpTarget(o.b)
        val steps = mutableListOf<Step>()
        if (diff > 0) {
            steps += Step("round_up", "${fmt(o.b)} = ${fmt(rounded)} − ${fmt(diff)}")
            steps += Step("sub_rounded", "${fmt(o.a)} − ${fmt(rounded)} = ${fmt(o.a - rounded)}")
            steps += Step("give_back", "${fmt(o.a - rounded)} + ${fmt(diff)} = ${fmt(o.a - o.b)}")
        }
        steps += Step("result", "${question(o)} = ${fmt(o.a - o.b)}")
        return steps
    }
}

/** 1000 − 637: every digit from 9, the last one from 10. */
object SubtractComplement : MathMethod("sub_complement", Category.ADD_SUB) {
    override fun generate(level: Int, rnd: Random): Operands {
        fun notRound(from: Int, to: Int): Long {
            var value: Long
            do value = rnd.between(from, to) while (value % 10 == 0L)
            return value
        }
        return when (level) {
            1 -> Operands(100, notRound(11, 99))
            2 -> Operands(1000, notRound(101, 999))
            else -> if (rnd.nextBoolean()) Operands(10_000, notRound(1001, 9999))
            else Operands(rnd.between(2, 9) * 1000, notRound(101, 999))
        }
    }

    override fun question(o: Operands) = "${fmt(o.a)} − ${fmt(o.b)}"
    override fun answer(o: Operands): BigDecimal = bd(o.a - o.b)

    override fun solve(o: Operands): List<Step> {
        val result = o.a - o.b
        val steps = mutableListOf<Step>()
        // Trailing zeros of b are simply copied, so work on the part before them.
        var zeros = 0
        var core = o.b
        while (core % 10 == 0L && core > 0) {
            core /= 10
            zeros++
        }
        val coreDigits = core.toString().map { it - '0' }
        val power = pow10(coreDigits.size + zeros)
        if (o.a % power != 0L || o.a < power || core == 0L) {
            return listOf(Step("result", "${question(o)} = ${fmt(result)}"))
        }
        val rest = o.a - power
        if (rest > 0) steps += Step("comp_borrow", "${fmt(o.a)} = ${fmt(rest)} + ${fmt(power)}")
        if (coreDigits.size > 1) {
            steps += Step("comp_nines", coreDigits.dropLast(1).joinToString(",  ") { "9 − $it = ${9 - it}" })
        }
        steps += Step("comp_last", "10 − ${coreDigits.last()} = ${10 - coreDigits.last()}")
        val complement = power - o.b
        steps += Step("comp_join", "${fmt(power)} − ${fmt(o.b)} = ${fmt(complement)}")
        if (rest > 0) steps += Step("comp_add_rest", "${fmt(rest)} + ${fmt(complement)} = ${fmt(result)}")
        steps += Step("result", "${question(o)} = ${fmt(result)}")
        return steps
    }
}
