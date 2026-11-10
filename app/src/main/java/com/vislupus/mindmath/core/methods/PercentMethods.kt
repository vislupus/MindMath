package com.vislupus.mindmath.core.methods

import com.vislupus.mindmath.core.Category
import com.vislupus.mindmath.core.MathMethod
import com.vislupus.mindmath.core.Operands
import com.vislupus.mindmath.core.Step
import com.vislupus.mindmath.core.bd
import com.vislupus.mindmath.core.between
import com.vislupus.mindmath.core.fmt
import com.vislupus.mindmath.core.gcd
import com.vislupus.mindmath.core.normalized
import com.vislupus.mindmath.core.pick
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

// In every percent method: a = the percent, b = the number it is taken of.

private val HUNDRED = bd(100)

/** Exact value of [percent] % of [base]. */
internal fun percentOf(percent: Long, base: Long): BigDecimal =
    bd(percent * base).divide(HUNDRED, 4, RoundingMode.HALF_UP).normalized()

/** 35% of 240: build it from 10%, 5% and 1%. */
object PercentBlocks : MathMethod("pct_blocks", Category.PERCENT) {
    override fun generate(level: Int, rnd: Random): Operands = when (level) {
        1 -> {
            val percent = rnd.pick(10, 20, 50, 25, 5, 1)
            val base = if (percent == 1L) rnd.between(1, 9) * 100 else rnd.between(1, 20) * 20
            Operands(percent, base)
        }
        2 -> Operands(rnd.pick(15, 30, 40, 60, 70, 75, 35, 45, 12, 11, 90, 80, 2, 3, 4), rnd.between(2, 45) * 20)
        else -> {
            var percent: Long
            do percent = rnd.between(6, 99) while (percent % 10 == 0L)
            Operands(percent, rnd.between(6, 150) * 10)
        }
    }

    override fun question(o: Operands) = "${o.a}% {of} ${fmt(o.b)}"
    override fun answer(o: Operands): BigDecimal = percentOf(o.a, o.b)

    override fun solve(o: Operands): List<Step> {
        val percent = o.a
        val n = fmt(o.b)
        val result = answer(o)
        val resultStep = Step("result", "${question(o)} = ${fmt(result)}")
        val ten = percentOf(10, o.b)
        val one = percentOf(1, o.b)
        val steps = mutableListOf<Step>()

        when (percent) {
            // Simple fractions are faster than building blocks.
            50L -> steps += Step("pb_half", "$n ÷ 2 = ${fmt(result)}")
            25L -> steps += Step("pb_quarter", "$n ÷ 4 = ${fmt(result)}")
            75L -> {
                val half = percentOf(50, o.b)
                val quarter = percentOf(25, o.b)
                steps += Step("pb_half", "$n ÷ 2 = ${fmt(half)}")
                steps += Step("pb_quarter_of_half", "${fmt(half)} ÷ 2 = ${fmt(quarter)}")
                steps += Step("pb_sum", "${fmt(half)} + ${fmt(quarter)} = ${fmt(result)}")
            }
            // Just below 100%: take the small missing piece away.
            90L, 95L, 99L -> {
                val missing = 100 - percent
                val piece = percentOf(missing, o.b)
                when (missing) {
                    10L -> steps += Step("pb_10", "10% = $n ÷ 10 = ${fmt(ten)}")
                    5L -> {
                        steps += Step("pb_10", "10% = $n ÷ 10 = ${fmt(ten)}")
                        steps += Step("pb_5", "5% = ${fmt(ten)} ÷ 2 = ${fmt(piece)}")
                    }
                    else -> steps += Step("pb_1", "1% = $n ÷ 100 = ${fmt(one)}")
                }
                steps += Step("pb_from_whole", "$n − ${fmt(piece)} = ${fmt(result)}")
            }
            else -> {
                val tens = percent / 10
                val ones = percent % 10
                val parts = mutableListOf<BigDecimal>()
                if (tens > 0 || ones == 5L) steps += Step("pb_10", "10% = $n ÷ 10 = ${fmt(ten)}")
                if (tens == 1L) parts += ten
                if (tens > 1) {
                    val value = percentOf(tens * 10, o.b)
                    steps += Step("pb_tens", "${tens * 10}% = ${fmt(ten)} × $tens = ${fmt(value)}")
                    parts += value
                }
                if (ones == 5L) {
                    val five = percentOf(5, o.b)
                    steps += Step("pb_5", "5% = ${fmt(ten)} ÷ 2 = ${fmt(five)}")
                    parts += five
                } else if (ones > 0) {
                    steps += Step("pb_1", "1% = $n ÷ 100 = ${fmt(one)}")
                    if (ones == 1L) {
                        parts += one
                    } else {
                        val value = percentOf(ones, o.b)
                        steps += Step("pb_ones", "$ones% = ${fmt(one)} × $ones = ${fmt(value)}")
                        parts += value
                    }
                }
                if (parts.size > 1) {
                    steps += Step("pb_sum", "${parts.joinToString(" + ") { fmt(it) }} = ${fmt(result)}")
                }
            }
        }
        steps += resultStep
        return steps
    }
}

/** 8% of 50 = 50% of 8: swap the numbers when the other way is easier. */
object PercentFlip : MathMethod("pct_flip", Category.PERCENT) {
    override fun generate(level: Int, rnd: Random): Operands = when (level) {
        1 -> if (rnd.nextBoolean()) Operands(rnd.between(1, 24) * 2, 50) else Operands(rnd.between(1, 12) * 4, 25)
        2 -> when (rnd.nextInt(4)) {
            0 -> Operands(rnd.between(13, 49) * 2, 50)
            1 -> Operands(rnd.between(7, 24) * 4, 25)
            2 -> Operands(rnd.between(3, 19) * 5, 20)
            else -> Operands(rnd.between(3, 24) * 4, 75)
        }
        else -> when (rnd.nextInt(4)) {
            0 -> Operands(rnd.between(3, 48) * 2, 250)
            1 -> Operands(rnd.between(3, 99), 500)
            2 -> Operands(rnd.between(3, 48) * 2, 150)
            else -> Operands(rnd.between(2, 24) * 4, 125)
        }
    }

    override fun question(o: Operands) = "${o.a}% {of} ${fmt(o.b)}"
    override fun answer(o: Operands): BigDecimal = percentOf(o.a, o.b)

    override fun solve(o: Operands): List<Step> {
        val p = fmt(o.a)
        val result = fmt(answer(o))
        val easy = when (o.b) {
            50L -> Step("pf_half", "$p ÷ 2 = $result")
            25L -> Step("pf_quarter", "$p ÷ 4 = $result")
            20L -> Step("pf_fifth", "$p ÷ 5 = $result")
            10L -> Step("pf_tenth", "$p ÷ 10 = $result")
            75L -> Step("pf_three_quarters", "$p ÷ 4 × 3 = $result")
            else -> Step("pf_times", "$p × ${fmt(bd(o.b).divide(HUNDRED))} = $result")
        }
        return listOf(
            Step("pf_flip", "${o.a}% {of} ${fmt(o.b)} = ${o.b}% {of} $p"),
            easy,
            Step("result", "${question(o)} = $result"),
        )
    }
}

/** 80 − 20% (discount) or 80 + 20% (tax, tip). variant 0 = minus, 1 = plus. */
object PercentChange : MathMethod("pct_change", Category.PERCENT) {
    override fun generate(level: Int, rnd: Random): Operands {
        val variant = rnd.nextInt(2)
        return when (level) {
            1 -> Operands(rnd.pick(10, 20, 50, 25), rnd.between(2, 20) * 20, variant)
            2 -> Operands(rnd.pick(5, 15, 30, 40, 20, 25, 10), rnd.between(2, 90) * 10, variant)
            else -> if (rnd.nextBoolean()) Operands(rnd.between(1, 19) * 5, rnd.between(8, 199) * 5, variant)
            else Operands(rnd.pick(8, 9, 12, 18, 21, 22), rnd.between(4, 99) * 10, variant)
        }
    }

    private fun sign(o: Operands) = if (o.variant == 1) "+" else "−"

    override fun question(o: Operands) = "${fmt(o.b)} ${sign(o)} ${o.a}%"

    override fun answer(o: Operands): BigDecimal {
        val part = percentOf(o.a, o.b)
        return if (o.variant == 1) bd(o.b).add(part) else bd(o.b).subtract(part)
    }

    override fun solve(o: Operands): List<Step> {
        val part = percentOf(o.a, o.b)
        val result = fmt(answer(o))
        val factor = bd(if (o.variant == 1) 100 + o.a else 100 - o.a).divide(HUNDRED)
        return listOf(
            Step("pc_part", "${o.a}% {of} ${fmt(o.b)} = ${fmt(part)}"),
            Step(if (o.variant == 1) "pc_add" else "pc_sub", "${fmt(o.b)} ${sign(o)} ${fmt(part)} = $result"),
            Step("pc_shortcut", "${fmt(o.b)} × ${fmt(factor)} = $result"),
            Step("result", "${question(o)} = $result"),
        )
    }
}

/** 18 of 60 = ?%  Here a = the part and b = the whole. */
object WhatPercent : MathMethod("pct_of", Category.PERCENT) {
    override val suffix = "%"

    override fun generate(level: Int, rnd: Random): Operands {
        val whole = when (level) {
            1 -> rnd.pick(10, 20, 25, 50, 200, 5, 4)
            2 -> rnd.pick(40, 60, 80, 120, 150, 250, 300, 400, 500)
            else -> rnd.pick(8, 16, 30, 70, 90, 125, 160, 240, 350, 450, 600, 750)
        }
        // Keep only parts that give a tidy percentage (whole, or .5 at level 3).
        val step = if (level == 3) 200L else 100L
        val candidates = (1 until whole).filter { part ->
            (part * step) % whole == 0L && (level > 1 || part * 100 / whole in 1..99)
        }
        return Operands(rnd.pick(candidates), whole)
    }

    override fun question(o: Operands) = "${fmt(o.a)} {of} ${fmt(o.b)} = ?%"

    override fun answer(o: Operands): BigDecimal =
        bd(o.a * 100).divide(bd(o.b), 4, RoundingMode.HALF_UP).normalized()

    override fun solve(o: Operands): List<Step> {
        val result = fmt(answer(o))
        val divisor = gcd(o.a, o.b)
        val top = o.a / divisor
        val bottom = o.b / divisor
        val steps = mutableListOf<Step>()
        steps += if (divisor > 1) {
            Step("po_simplify", "${fmt(o.a)} / ${fmt(o.b)} = ${fmt(top)} / ${fmt(bottom)}")
        } else {
            Step("po_fraction", "${fmt(o.a)} / ${fmt(o.b)}")
        }
        if (100 % bottom == 0L) {
            val scale = 100 / bottom
            if (scale > 1) steps += Step("po_scale", "${fmt(top)} / ${fmt(bottom)} = ${fmt(top * scale)} / 100")
        } else {
            val decimal = bd(top).divide(bd(bottom), 6, RoundingMode.HALF_UP).normalized()
            steps += Step("po_divide", "${fmt(top)} ÷ ${fmt(bottom)} = ${fmt(decimal)}")
        }
        steps += Step("result", "${fmt(o.a)} {of} ${fmt(o.b)} = $result%")
        return steps
    }
}
