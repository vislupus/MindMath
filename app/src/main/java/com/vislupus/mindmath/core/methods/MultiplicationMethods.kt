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
import kotlin.math.abs
import kotlin.random.Random

/** Shared pieces of every "a × b" method. */
abstract class ProductMethod(id: String, category: Category = Category.MULTIPLY) : MathMethod(id, category) {
    override fun question(o: Operands) = "${fmt(o.a)} × ${fmt(o.b)}"
    override fun answer(o: Operands): BigDecimal = bd(o.a * o.b)
    protected fun resultStep(o: Operands) = Step("result", "${question(o)} = ${fmt(o.a * o.b)}")
}

/** 47 × 11: write 4 and 7, put their sum in the middle. */
object MultiplyBy11 : ProductMethod("mul_11") {
    override fun generate(level: Int, rnd: Random) = when (level) {
        1 -> Operands(rnd.between(12, 99), 11)
        2 -> Operands(rnd.between(101, 999), 11)
        else -> Operands(rnd.between(1001, 9999), 11)
    }

    override fun solve(o: Operands): List<Step> {
        val digits = digitsOf(o.a)
        val steps = mutableListOf<Step>()
        steps += Step("m11_last", "${digits.last()}")
        var carry = 0
        // Walk from right to left, adding each digit to its left neighbour.
        for (i in digits.size - 1 downTo 1) {
            val sum = digits[i - 1] + digits[i] + carry
            val terms = "${digits[i - 1]} + ${digits[i]}" + if (carry > 0) " + $carry" else ""
            val nextCarry = sum / 10
            steps += if (nextCarry > 0) {
                Step("m11_pair_carry", "$terms = $sum → ${sum % 10}, {carry} $nextCarry")
            } else {
                Step("m11_pair", "$terms = $sum")
            }
            carry = nextCarry
        }
        steps += if (carry > 0) {
            Step("m11_first_carry", "${digits.first()} + $carry = ${digits.first() + carry}")
        } else {
            Step("m11_first", "${digits.first()}")
        }
        steps += resultStep(o)
        return steps
    }
}

/** 48 × 5 = 480 ÷ 2. Also ×25, ×50 and ×125. */
object MultiplyBy5Family : ProductMethod("mul_5_25") {
    override fun generate(level: Int, rnd: Random) = when (level) {
        1 -> Operands(rnd.between(12, 98), 5)
        2 -> Operands(rnd.between(12, 99), rnd.pick(25, 50))
        else -> when (rnd.nextInt(3)) {
            0 -> Operands(rnd.between(102, 998), 5)
            1 -> Operands(rnd.between(102, 498), 25)
            else -> Operands(rnd.between(6, 48) * 2, 125)
        }
    }

    override fun solve(o: Operands): List<Step> {
        val (shift, halvings) = when (o.b) {
            5L -> 10L to 1
            50L -> 100L to 1
            25L -> 100L to 2
            125L -> 1000L to 3
            else -> return listOf(resultStep(o))
        }
        val steps = mutableListOf<Step>()
        var value = o.a * shift
        steps += Step("m5_shift", "${fmt(o.a)} × ${fmt(shift)} = ${fmt(value)}")
        repeat(halvings) { index ->
            steps += Step(if (index == 0) "halve" else "halve_again", "${fmt(value)} ÷ 2 = ${fmt(value / 2)}")
            value /= 2
        }
        steps += resultStep(o)
        return steps
    }
}

/** 47 × 99 = 4700 − 47. Also ×9 and ×999. */
object MultiplyByNines : ProductMethod("mul_9_99") {
    override fun generate(level: Int, rnd: Random) = when (level) {
        1 -> Operands(rnd.between(12, 99), 9)
        2 -> if (rnd.nextBoolean()) Operands(rnd.between(12, 99), 99) else Operands(rnd.between(101, 999), 9)
        else -> if (rnd.nextBoolean()) Operands(rnd.between(101, 999), 99) else Operands(rnd.between(12, 999), 999)
    }

    override fun solve(o: Operands): List<Step> {
        val round = o.b + 1
        return listOf(
            Step("m9_round", "${fmt(o.b)} = ${fmt(round)} − 1"),
            Step("m9_shift", "${fmt(o.a)} × ${fmt(round)} = ${fmt(o.a * round)}"),
            Step("m9_sub", "${fmt(o.a * round)} − ${fmt(o.a)} = ${fmt(o.a * o.b)}"),
            resultStep(o),
        )
    }
}

/** 16 × 35 = 8 × 70: halve one number, double the other. */
object DoubleAndHalve : ProductMethod("mul_double_halve") {
    override fun generate(level: Int, rnd: Random) = when (level) {
        1 -> Operands(rnd.between(6, 24) * 2, rnd.pick(15, 25, 35, 45))
        2 -> if (rnd.nextBoolean()) Operands(rnd.between(6, 49) * 2, rnd.pick(15, 35, 45, 55, 65, 85))
        else Operands(rnd.between(3, 24) * 4, rnd.pick(25, 75))
        else -> if (rnd.nextBoolean()) Operands(rnd.between(26, 120) * 4, rnd.pick(25, 75, 125, 175))
        else Operands(rnd.between(51, 240) * 2, rnd.pick(15, 35, 45, 55))
    }

    override fun solve(o: Operands): List<Step> {
        val steps = mutableListOf<Step>()
        var x = o.a
        var y = o.b
        // Keep going while doubling still makes the second number rounder.
        while (x % 2 == 0L && (y % 10 == 5L || y % 100 == 50L)) {
            steps += Step(if (steps.isEmpty()) "dh_step" else "dh_again", "${fmt(x)} × ${fmt(y)} = ${fmt(x / 2)} × ${fmt(y * 2)}")
            x /= 2
            y *= 2
        }
        if (steps.isNotEmpty()) steps += Step("dh_final", "${fmt(x)} × ${fmt(y)} = ${fmt(x * y)}")
        steps += resultStep(o)
        return steps
    }
}

/** 347 × 6: multiply the hundreds, tens and ones, adding as you go. */
object MultiplyLeftToRight : ProductMethod("mul_ltr") {
    override fun generate(level: Int, rnd: Random): Operands {
        // Skip round numbers such as 30 or 400: they leave nothing to add up.
        fun first(from: Int, to: Int): Long {
            var value: Long
            do value = rnd.between(from, to) while (placeParts(value).size < 2)
            return value
        }
        return when (level) {
            1 -> Operands(first(12, 99), rnd.between(3, 9))
            2 -> Operands(first(101, 999), rnd.between(3, 9))
            else -> Operands(first(1001, 9999), rnd.between(3, 9))
        }
    }

    override fun solve(o: Operands): List<Step> {
        val steps = mutableListOf<Step>()
        var running = 0L
        for ((index, part) in placeParts(o.a).withIndex()) {
            val product = part * o.b
            if (index == 0) {
                steps += Step("ml_first", "${fmt(part)} × ${fmt(o.b)} = ${fmt(product)}")
            } else {
                steps += Step("ml_next", "${fmt(part)} × ${fmt(o.b)} = ${fmt(product)}")
                steps += Step("ml_add", "${fmt(running)} + ${fmt(product)} = ${fmt(running + product)}")
            }
            running += product
        }
        steps += resultStep(o)
        return steps
    }
}

/** 23 × 47 = 23 × 40 + 23 × 7. */
object MultiplySplit : ProductMethod("mul_split") {
    override fun generate(level: Int, rnd: Random): Operands {
        fun twoDigit(from: Int, to: Int): Long {
            var value: Long
            do value = rnd.between(from, to) while (value % 10 == 0L)
            return value
        }
        return when (level) {
            1 -> Operands(rnd.between(12, 49), twoDigit(11, 19))
            2 -> Operands(rnd.between(12, 99), twoDigit(21, 99))
            else -> Operands(rnd.between(101, 999), twoDigit(12, 99))
        }
    }

    override fun solve(o: Operands): List<Step> {
        val parts = placeParts(o.b)
        if (parts.size < 2) return listOf(resultStep(o))
        val steps = mutableListOf<Step>()
        steps += Step("split", "${fmt(o.b)} = ${parts.joinToString(" + ") { fmt(it) }}")
        var running = 0L
        for ((index, part) in parts.withIndex()) {
            val product = o.a * part
            steps += Step(if (index == 0) "split_big" else "split_small", "${fmt(o.a)} × ${fmt(part)} = ${fmt(product)}")
            if (index > 0) steps += Step("split_add", "${fmt(running)} + ${fmt(product)} = ${fmt(running + product)}")
            running += product
        }
        steps += resultStep(o)
        return steps
    }
}

/** Criss-cross: build the answer one digit at a time, right to left. */
object CrissCross : ProductMethod("mul_cross") {
    override fun generate(level: Int, rnd: Random): Operands {
        fun smallDigits(): Long = rnd.between(1, 3) * 10 + rnd.between(1, 4)
        return when (level) {
            1 -> Operands(smallDigits(), smallDigits())
            2 -> Operands(rnd.between(12, 99), rnd.between(12, 99))
            else -> if (rnd.nextBoolean()) Operands(rnd.between(101, 999), rnd.between(12, 99))
            else Operands(rnd.between(101, 999), rnd.between(101, 999))
        }
    }

    override fun solve(o: Operands): List<Step> {
        // Least significant digit first makes the column maths simple.
        val a = digitsOf(o.a).reversed()
        val b = digitsOf(o.b).reversed()
        val steps = mutableListOf<Step>()
        val lastColumn = a.size + b.size - 2
        var carry = 0
        for (column in 0..lastColumn) {
            val terms = mutableListOf<String>()
            var sum = carry
            for (i in a.indices.reversed()) {
                val j = column - i
                if (j < 0 || j >= b.size) continue
                terms += "${a[i]}×${b[j]}"
                sum += a[i] * b[j]
            }
            val left = terms.joinToString(" + ") + if (carry > 0) " + $carry" else ""
            val key = when (column) {
                0 -> "cross_ones"
                lastColumn -> "cross_last"
                else -> "cross_middle"
            }
            steps += if (column == lastColumn || sum < 10) {
                Step(key, "$left = $sum")
            } else {
                Step(key, "$left = $sum → ${sum % 10}, {carry} ${sum / 10}")
            }
            carry = if (column == lastColumn) 0 else sum / 10
        }
        steps += resultStep(o)
        return steps
    }
}

/** 97 × 94: work with the small distances from 100. */
object NearHundred : ProductMethod("mul_near_100") {
    override fun generate(level: Int, rnd: Random) = when (level) {
        1 -> Operands(rnd.between(91, 99), rnd.between(91, 99))
        2 -> if (rnd.nextBoolean()) Operands(rnd.between(101, 112), rnd.between(101, 112))
        else Operands(rnd.between(86, 99), rnd.between(86, 99))
        else -> when (rnd.nextInt(3)) {
            0 -> Operands(rnd.between(985, 999), rnd.between(985, 999))
            1 -> Operands(rnd.between(91, 99), rnd.between(101, 109))
            else -> Operands(rnd.between(1001, 1012), rnd.between(1001, 1012))
        }
    }

    private fun signed(value: Long) = if (value < 0) "− ${fmt(-value)}" else "+ ${fmt(value)}"
    private fun inBrackets(value: Long) = if (value < 0) "(${fmt(value)})" else fmt(value)

    override fun solve(o: Operands): List<Step> {
        val base = if (o.a >= 500 && o.b >= 500) 1000L else 100L
        val da = o.a - base
        val db = o.b - base
        val left = o.a + db
        val product = da * db
        return listOf(
            Step("nb_diff", "${fmt(o.a)} = ${fmt(base)} ${signed(da)},  ${fmt(o.b)} = ${fmt(base)} ${signed(db)}"),
            Step("nb_cross", "${fmt(o.a)} ${signed(db)} = ${fmt(left)}"),
            Step("nb_scale", "${fmt(left)} × ${fmt(base)} = ${fmt(left * base)}"),
            Step("nb_product", "${inBrackets(da)} × ${inBrackets(db)} = ${fmt(product)}"),
            Step("nb_join", "${fmt(left * base)} ${signed(product)} = ${fmt(o.a * o.b)}"),
            resultStep(o),
        )
    }
}

/** 47 × 53 = 50² − 3²: numbers equally far from a round middle. */
object DifferenceOfSquares : ProductMethod("mul_diff_squares") {
    override fun generate(level: Int, rnd: Random): Operands {
        val (center, diff) = when (level) {
            1 -> rnd.between(2, 9) * 10 to rnd.between(1, 3)
            2 -> rnd.between(2, 12) * 10 to rnd.between(2, 7)
            else -> if (rnd.nextBoolean()) rnd.between(1, 10) * 100 to rnd.between(1, 9)
            else rnd.between(11, 20) * 10 to rnd.between(1, 9)
        }
        return if (rnd.nextBoolean()) Operands(center - diff, center + diff) else Operands(center + diff, center - diff)
    }

    override fun solve(o: Operands): List<Step> {
        if ((o.a + o.b) % 2 != 0L) return listOf(resultStep(o))
        val center = (o.a + o.b) / 2
        val diff = abs(o.a - o.b) / 2
        val low = minOf(o.a, o.b)
        val high = maxOf(o.a, o.b)
        return listOf(
            Step("ds_center", "${fmt(low)} = ${fmt(center)} − ${fmt(diff)},  ${fmt(high)} = ${fmt(center)} + ${fmt(diff)}"),
            Step("ds_big", "${fmt(center)}² = ${fmt(center * center)}"),
            Step("ds_small", "${fmt(diff)}² = ${fmt(diff * diff)}"),
            Step("ds_sub", "${fmt(center * center)} − ${fmt(diff * diff)} = ${fmt(o.a * o.b)}"),
            resultStep(o),
        )
    }
}

/** 35² = 3×4 | 25. Also 43 × 47 (same tens, ones add up to 10). */
object SquaresEndingIn5 : ProductMethod("sq_5", Category.SQUARES) {
    override fun generate(level: Int, rnd: Random): Operands = when (level) {
        1 -> (rnd.between(1, 9) * 10 + 5).let { Operands(it, it) }
        2 -> (rnd.between(10, 19) * 10 + 5).let { Operands(it, it) }
        else -> {
            val tens = rnd.between(2, 12) * 10
            val ones = rnd.pick(1, 2, 3, 4, 6, 7, 8, 9)
            Operands(tens + ones, tens + 10 - ones)
        }
    }

    override fun question(o: Operands) = if (o.a == o.b) "${fmt(o.a)}²" else super.question(o)

    override fun solve(o: Operands): List<Step> {
        val tens = o.a / 10
        val onesA = o.a % 10
        val onesB = o.b % 10
        val result = Step("result", "${question(o)} = ${fmt(o.a * o.b)}")
        if (o.b / 10 != tens || onesA + onesB != 10L) return listOf(result)
        val head = tens * (tens + 1)
        val tail = (onesA * onesB).toString().padStart(2, '0')
        return listOf(
            Step("s5_head", "${fmt(tens)} × ${fmt(tens + 1)} = ${fmt(head)}"),
            Step("s5_tail", "$onesA × $onesB = $tail"),
            Step("s5_join", "${fmt(head)} | $tail → ${fmt(o.a * o.b)}"),
            result,
        )
    }
}

/** 48² = 50 × 46 + 2²: slide to a round number and back. */
object SquareAnyNumber : MathMethod("sq_any", Category.SQUARES) {
    override fun generate(level: Int, rnd: Random): Operands {
        fun notRound(from: Int, to: Int): Long {
            var value: Long
            do value = rnd.between(from, to) while (value % 10 == 0L)
            return value
        }
        val a = when (level) {
            1 -> notRound(11, 30)
            2 -> notRound(31, 99)
            else -> if (rnd.nextBoolean()) notRound(101, 125)
            else rnd.between(2, 9) * 100 + rnd.between(1, 12) * (if (rnd.nextBoolean()) 1 else -1)
        }
        return Operands(a, a)
    }

    override fun question(o: Operands) = "${fmt(o.a)}²"
    override fun answer(o: Operands): BigDecimal = bd(o.a * o.a)

    override fun solve(o: Operands): List<Step> {
        val a = o.a
        val result = Step("result", "${question(o)} = ${fmt(a * a)}")
        val nearest100 = (a + 50) / 100 * 100
        val round = if (a > 100 && abs(a - nearest100) <= 25) nearest100 else (a + 5) / 10 * 10
        val diff = abs(a - round)
        if (diff == 0L) return listOf(result)
        val other = 2 * a - round
        val up = if (round > a) "+" else "−"
        val down = if (round > a) "−" else "+"
        return listOf(
            Step("sq_slide", "${fmt(a)} $up ${fmt(diff)} = ${fmt(round)},  ${fmt(a)} $down ${fmt(diff)} = ${fmt(other)}"),
            Step("sq_product", "${fmt(round)} × ${fmt(other)} = ${fmt(round * other)}"),
            Step("sq_small", "${fmt(diff)}² = ${fmt(diff * diff)}"),
            Step("sq_add", "${fmt(round * other)} + ${fmt(diff * diff)} = ${fmt(a * a)}"),
            result,
        )
    }
}
