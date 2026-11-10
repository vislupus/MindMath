package com.vislupus.mindmath

import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.core.MathMethod
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.core.Operands
import com.vislupus.mindmath.core.Progress
import com.vislupus.mindmath.core.fmt
import com.vislupus.mindmath.texts.Texts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.MathContext
import java.time.LocalDate
import kotlin.random.Random

/**
 * Checks the maths of every method with thousands of random problems:
 * the answers, and every single line of the worked solutions.
 */
class MethodsTest {

    private val samplesPerLevel = 3000

    @Test
    fun everyEnabledMethodExists() {
        for (id in AppConfig.ENABLED_METHODS) {
            assertTrue("Unknown method id in AppConfig.ENABLED_METHODS: $id", Methods.get(id) != null)
        }
        assertEquals("Method ids must be unique", Methods.all.size, Methods.all.map { it.id }.toSet().size)
        assertTrue("At least 10 methods are expected", Methods.enabled.size >= 10)
    }

    @Test
    fun answersAndSolutionsAreCorrect() {
        val rnd = Random(20240607)
        for (method in Methods.all) {
            for (level in 1..MathMethod.MAX_LEVEL) {
                repeat(samplesPerLevel) {
                    val problem = method.problem(level, rnd)
                    val context = "${method.id} L$level ${problem.question}"
                    assertTrue("$context: answer must be positive", problem.answer.signum() > 0)
                    assertTrue("$context: answer too long to type", problem.answer.toPlainString().length <= AppConfig.MAX_ANSWER_LENGTH)
                    assertTrue("$context: more than 3 decimals", problem.answer.scale() <= 3)

                    val steps = method.solve(problem.operands)
                    // Some percentages are one-liners (50% = half); everything else needs real steps.
                    val minSteps = if (method.id == "pct_blocks" || method.id == "pct_of") 2 else 3
                    assertTrue("$context: solution has no real steps", steps.size >= minSteps)
                    assertEquals("$context: last step must be the result", "result", steps.last().key)
                    assertEquals("$context: result steps", 1, steps.count { it.key == "result" })

                    // The final line must end with the answer.
                    val expectedEnd = "= " + fmt(problem.answer) + problem.suffix
                    assertTrue("$context: '${steps.last().expr}' should end with '$expectedEnd'", steps.last().expr.endsWith(expectedEnd))

                    // Every equation shown to the user must be true.
                    for (step in steps) {
                        // "18 of 60 = 30%" and "320 − 10% = 288" are everyday notation, not strict equations.
                        if (step.key == "result" && (method.id == "pct_of" || method.id == "pct_change")) continue
                        StepChecker.check(step.expr, "$context [${step.key}] ${step.expr}")
                    }

                    // Problems survive the round trip through the database.
                    val restored = method.build(level, Operands.decode(problem.operands.encode())!!)
                    assertEquals(context, problem, restored)
                }
            }
        }
    }

    @Test
    fun answersMatchIndependentArithmetic() {
        val rnd = Random(99)
        for (method in Methods.all) {
            for (level in 1..MathMethod.MAX_LEVEL) {
                repeat(500) {
                    val problem = method.problem(level, rnd)
                    val o = problem.operands
                    val a = BigDecimal(o.a)
                    val b = BigDecimal(o.b)
                    val hundred = BigDecimal(100)
                    val expected = when {
                        method.id.startsWith("add_") -> a + b
                        method.id.startsWith("sub_") -> a - b
                        method.id.startsWith("mul_") || method.id.startsWith("sq_") -> a * b
                        method.id.startsWith("div_") -> a.divide(b, MathContext.DECIMAL64)
                        method.id == "pct_blocks" || method.id == "pct_flip" -> (a * b).divide(hundred, MathContext.DECIMAL64)
                        method.id == "pct_change" -> (a * b).divide(hundred, MathContext.DECIMAL64).let { if (o.variant == 1) b + it else b - it }
                        method.id == "pct_of" -> (a * hundred).divide(b, MathContext.DECIMAL64)
                        else -> error("No reference formula for ${method.id}")
                    }
                    assertEquals("${method.id} ${problem.question}", 0, expected.compareTo(problem.answer))
                }
            }
        }
    }

    @Test
    fun everyTextExistsInEveryLanguage() {
        val rnd = Random(7)
        for (texts in Texts.all) {
            for (method in Methods.all) {
                val text = texts.methods[method.id]
                assertTrue("${texts.languageCode}: no texts for ${method.id}", text != null)
                text!!
                for (field in listOf(text.title, text.tagline, text.idea, text.whenToUse, text.tip)) {
                    assertTrue("${texts.languageCode}: empty text in ${method.id}", field.isNotBlank())
                }
                for (level in 1..MathMethod.MAX_LEVEL) {
                    repeat(400) {
                        val problem = method.problem(level, rnd)
                        for (step in method.solve(problem.operands)) {
                            assertTrue(
                                "${texts.languageCode}: no caption for step '${step.key}' of ${method.id}",
                                texts.stepCaption(method.id, step.key).isNotBlank(),
                            )
                            assertFalse(
                                "${texts.languageCode}: untranslated token in '${step.expr}'",
                                texts.localize(step.expr).contains('{'),
                            )
                        }
                        assertFalse(texts.localize(problem.question).contains('{'))
                    }
                }
            }
            for (id in listOf("first_steps", "hundred", "thousand", "week", "month", "perfect", "speed", "explorer", "scholar", "master")) {
                assertTrue("${texts.languageCode}: no achievement text for $id", texts.achievementTexts[id] != null)
            }
            assertEquals(7, texts.weekdaysShort.size)
        }
    }

    @Test
    fun answerParsingAcceptsBothDecimalSeparators() {
        val problem = Methods.get("div_5_25")!!.build(3, Operands(137, 5))
        assertEquals(BigDecimal("27.4"), problem.answer)
        assertTrue(Progress.isCorrect(problem, "27.4"))
        assertTrue(Progress.isCorrect(problem, "27,4"))
        assertTrue(Progress.isCorrect(problem, "27.40"))
        assertFalse(Progress.isCorrect(problem, "27"))
        assertFalse(Progress.isCorrect(problem, ""))
        assertFalse(Progress.isCorrect(problem, "."))
    }

    @Test
    fun levelsAndStreaks() {
        assertEquals(1, Progress.levelInfo(0).level)
        assertEquals(1, Progress.levelInfo(AppConfig.XP_LEVEL_STEP - 1).level)
        assertEquals(2, Progress.levelInfo(AppConfig.XP_LEVEL_STEP).level)
        assertEquals(3, Progress.levelInfo(AppConfig.XP_LEVEL_STEP * 3).level)

        val today = LocalDate.of(2025, 3, 10)
        val enough = AppConfig.STREAK_MIN_PROBLEMS
        fun days(vararg back: Long) = back.associate { today.minusDays(it) to enough }
        assertEquals(0 to 0, Progress.streaks(emptyMap(), today))
        assertEquals(3 to 3, Progress.streaks(days(0, 1, 2), today))
        // Yesterday still keeps the streak alive; two days ago does not.
        assertEquals(2 to 2, Progress.streaks(days(1, 2), today))
        assertEquals(0 to 2, Progress.streaks(days(2, 3), today))
        assertEquals(1 to 3, Progress.streaks(days(0, 5, 6, 7), today))
        // Days with too few problems do not count.
        assertEquals(0 to 0, Progress.streaks(mapOf(today to enough - 1), today))
    }

    @Test
    fun numbersAreFormattedForReading() {
        assertEquals("1564", fmt(1564L))
        assertEquals("12\u202F345", fmt(12345L))
        assertEquals("−3", fmt(-3L))
        assertEquals("27.4", fmt(BigDecimal("27.40")))
        assertEquals("12\u202F345.5", fmt(BigDecimal("12345.5")))
    }
}

/**
 * A tiny calculator that verifies the equations printed in solution steps,
 * e.g. "97 − 6 = 91" or "9 − 6 = 3,  9 − 3 = 6".
 */
private object StepChecker {
    private val mc = MathContext.DECIMAL64
    private val tolerance = BigDecimal("0.000000001")

    fun check(expr: String, context: String) {
        // Anything after an arrow is a note ("→ 1, carry 1"), not an equation.
        val main = expr.substringBefore("→").trim()
        for (piece in main.split(",  ")) {
            if (!piece.contains("=")) continue
            if (piece.contains("{rem}")) {
                checkRemainder(piece, context)
                continue
            }
            var sides = piece.split("=").map { it.trim() }
            // "10% = 240 ÷ 10 = 24": the leading label is not part of the equation.
            if (sides.first().matches(Regex("\\d+%"))) sides = sides.drop(1)
            val values = sides.map { Parser(it, context).parse() }
            for (i in 1 until values.size) {
                assertTrue("Not equal: $context", (values[i] - values[0]).abs() < tolerance)
            }
        }
    }

    /** "23 ÷ 4 = 5, {rem} 3" */
    private fun checkRemainder(piece: String, context: String) {
        val numbers = Regex("\\d+").findAll(piece.replace("\u202F", "")).map { it.value.toLong() }.toList()
        assertEquals("Unexpected remainder step: $context", 4, numbers.size)
        val (dividend, divisor, quotient, remainder) = numbers
        assertEquals("Wrong remainder step: $context", dividend, divisor * quotient + remainder)
        assertTrue("Remainder too big: $context", remainder < divisor)
    }

    /** Recursive descent parser for + − × ÷ / ( ) ² % and "{of}". */
    private class Parser(text: String, private val context: String) {
        private val s = text.replace("\u202F", "").replace("{of}", "×").replace(" ", "")
        private var pos = 0

        fun parse(): BigDecimal {
            val value = sum()
            assertEquals("Could not read '$s': $context", s.length, pos)
            return value
        }

        private fun sum(): BigDecimal {
            var value = product()
            while (pos < s.length && (s[pos] == '+' || s[pos] == '−')) {
                val op = s[pos++]
                val right = product()
                value = if (op == '+') value + right else value - right
            }
            return value
        }

        private fun product(): BigDecimal {
            var value = unary()
            while (pos < s.length && (s[pos] == '×' || s[pos] == '÷' || s[pos] == '/')) {
                val op = s[pos++]
                val right = unary()
                value = if (op == '×') value.multiply(right, mc) else value.divide(right, mc)
            }
            return value
        }

        private fun unary(): BigDecimal {
            if (pos < s.length && s[pos] == '−') {
                pos++
                return unary().negate()
            }
            return postfix()
        }

        private fun postfix(): BigDecimal {
            var value = atom()
            while (pos < s.length && (s[pos] == '²' || s[pos] == '%')) {
                value = if (s[pos] == '²') value * value else value.divide(BigDecimal(100), mc)
                pos++
            }
            return value
        }

        private fun atom(): BigDecimal {
            if (pos < s.length && s[pos] == '(') {
                pos++
                val value = sum()
                assertTrue("Missing ')': $context", pos < s.length && s[pos] == ')')
                pos++
                return value
            }
            val start = pos
            while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
            assertTrue("Number expected at $pos in '$s': $context", pos > start)
            return BigDecimal(s.substring(start, pos))
        }
    }
}
