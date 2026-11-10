package com.vislupus.mindmath.core.methods

import com.vislupus.mindmath.core.Category
import com.vislupus.mindmath.core.MathMethod
import com.vislupus.mindmath.core.Operands
import com.vislupus.mindmath.core.Step
import com.vislupus.mindmath.core.bd
import com.vislupus.mindmath.core.between
import com.vislupus.mindmath.core.digitsOf
import com.vislupus.mindmath.core.fmt
import com.vislupus.mindmath.core.pick
import com.vislupus.mindmath.core.placeParts
import java.math.BigDecimal
import kotlin.random.Random

/** Shared pieces of every "a ÷ b" method. */
abstract class QuotientMethod(id: String) : MathMethod(id, Category.DIVISION) {
    override fun question(o: Operands) = "${fmt(o.a)} ÷ ${fmt(o.b)}"
    override fun answer(o: Operands): BigDecimal = bd(o.a).divide(bd(o.b), 6, java.math.RoundingMode.HALF_UP)
    protected fun resultStep(o: Operands) = Step("result", "${question(o)} = ${fmt(answer(o))}")
}

/** 1 344 ÷ 6: divide digit by digit, carrying the remainder to the right. */
object ShortDivision : QuotientMethod("div_short") {
    override fun generate(level: Int, rnd: Random): Operands {
        val divisor = if (level == 1) rnd.between(2, 9) else rnd.between(3, 9)
        val quotient = when (level) {
            1 -> rnd.between(12, 99)
            2 -> rnd.between(101, 999)
            else -> rnd.between(1001, 9999)
        }
        return Operands(divisor * quotient, divisor)
    }

    override fun solve(o: Operands): List<Step> {
        val steps = mutableListOf<Step>()
        var remainder = 0L
        var started = false
        for (digit in digitsOf(o.a)) {
            val current = remainder * 10 + digit
            val q = current / o.b
            remainder = current % o.b
            if (!started && q == 0L) continue // not big enough yet: take one more digit
            val key = if (!started) "sd_first" else "sd_next"
            started = true
            steps += Step(key, "${fmt(current)} ÷ ${fmt(o.b)} = $q" + if (remainder > 0) ", {rem} $remainder" else "")
        }
        steps += resultStep(o)
        return steps
    }
}

/** 135 ÷ 5 = 270 ÷ 10. Also ÷25, ÷50 and ÷125. */
object DivideBy5Family : QuotientMethod("div_5_25") {
    override fun generate(level: Int, rnd: Random) = when (level) {
        1 -> Operands(rnd.between(12, 99) * 5, 5)
        2 -> when (rnd.nextInt(3)) {
            0 -> Operands(rnd.between(4, 40) * 25, 25)
            1 -> Operands(rnd.between(3, 40) * 50, 50)
            else -> Operands(rnd.between(101, 999), 5)
        }
        else -> when (rnd.nextInt(4)) {
            0 -> Operands(rnd.between(1001, 9999), 5)
            1 -> Operands(rnd.between(21, 400) * 5, 25)
            2 -> Operands(rnd.between(21, 200) * 5, 50)
            else -> Operands(rnd.between(5, 80) * 25, 125)
        }
    }

    override fun solve(o: Operands): List<Step> {
        val (doublings, shift) = when (o.b) {
            5L -> 1 to 10L
            50L -> 1 to 100L
            25L -> 2 to 100L
            125L -> 3 to 1000L
            else -> return listOf(resultStep(o))
        }
        val steps = mutableListOf<Step>()
        var value = o.a
        repeat(doublings) { index ->
            steps += Step(if (index == 0) "double" else "double_again", "${fmt(value)} × 2 = ${fmt(value * 2)}")
            value *= 2
        }
        steps += Step("d5_shift", "${fmt(value)} ÷ ${fmt(shift)} = ${fmt(answer(o))}")
        steps += resultStep(o)
        return steps
    }
}

/** 432 ÷ 12 = 432 ÷ 2 ÷ 6: divide by the factors one at a time. */
object DivideByFactors : QuotientMethod("div_factor") {
    private val factors = mapOf(
        4L to listOf(2L, 2L), 6L to listOf(2L, 3L), 8L to listOf(2L, 4L),
        12L to listOf(2L, 6L), 14L to listOf(2L, 7L), 15L to listOf(5L, 3L), 16L to listOf(4L, 4L), 18L to listOf(2L, 9L),
        24L to listOf(4L, 6L), 28L to listOf(4L, 7L), 32L to listOf(4L, 8L), 35L to listOf(5L, 7L),
        36L to listOf(6L, 6L), 45L to listOf(5L, 9L), 48L to listOf(6L, 8L), 72L to listOf(8L, 9L),
    )

    override fun generate(level: Int, rnd: Random): Operands {
        val divisor = when (level) {
            1 -> rnd.pick(4, 6, 8)
            2 -> rnd.pick(12, 14, 15, 16, 18)
            else -> rnd.pick(24, 28, 32, 35, 36, 45, 48, 72)
        }
        val quotient = when (level) {
            1 -> rnd.between(12, 60)
            2 -> rnd.between(11, 60)
            else -> rnd.between(12, 150)
        }
        return Operands(divisor * quotient, divisor)
    }

    override fun solve(o: Operands): List<Step> {
        val parts = factors[o.b] ?: return listOf(resultStep(o))
        val steps = mutableListOf<Step>()
        steps += Step("df_split", "${fmt(o.b)} = ${parts.joinToString(" × ") { fmt(it) }}")
        var value = o.a
        for ((index, factor) in parts.withIndex()) {
            steps += Step(if (index == 0) "df_first" else "df_next", "${fmt(value)} ÷ ${fmt(factor)} = ${fmt(value / factor)}")
            value /= factor
        }
        steps += resultStep(o)
        return steps
    }
}

/** 1 564 ÷ 23: take away the biggest easy multiple, then the rest. */
object DivideByChunks : QuotientMethod("div_chunk") {
    override fun generate(level: Int, rnd: Random): Operands {
        // A round quotient (20, 300) would be found in a single chunk.
        fun quotient(from: Int, to: Int): Long {
            var value: Long
            do value = rnd.between(from, to) while (placeParts(value).size < 2)
            return value
        }
        val (divisor, quotient) = when (level) {
            1 -> rnd.between(11, 19) to quotient(11, 49)
            2 -> rnd.between(12, 49) to quotient(12, 99)
            else -> rnd.between(12, 99) to quotient(101, 999)
        }
        return Operands(divisor * quotient, divisor)
    }

    override fun solve(o: Operands): List<Step> {
        val quotient = o.a / o.b
        if (o.a % o.b != 0L) return listOf(resultStep(o))
        val chunks = placeParts(quotient)
        val steps = mutableListOf<Step>()
        var left = o.a
        for ((index, chunk) in chunks.withIndex()) {
            val taken = o.b * chunk
            steps += Step(if (index == 0) "dc_chunk" else "dc_next", "${fmt(o.b)} × ${fmt(chunk)} = ${fmt(taken)}")
            if (left - taken > 0) steps += Step("dc_left", "${fmt(left)} − ${fmt(taken)} = ${fmt(left - taken)}")
            left -= taken
        }
        if (chunks.size > 1) steps += Step("dc_sum", "${chunks.joinToString(" + ") { fmt(it) }} = ${fmt(quotient)}")
        steps += resultStep(o)
        return steps
    }
}
