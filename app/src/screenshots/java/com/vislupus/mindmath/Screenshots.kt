package com.vislupus.mindmath

import android.app.Application
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.core.Mode
import com.vislupus.mindmath.core.Operands
import com.vislupus.mindmath.core.Progress
import com.vislupus.mindmath.core.SessionSpec
import com.vislupus.mindmath.data.Outcome
import com.vislupus.mindmath.data.Repository
import com.vislupus.mindmath.data.Settings
import com.vislupus.mindmath.texts.Texts
import com.vislupus.mindmath.ui.AppViewModel
import com.vislupus.mindmath.ui.Phase
import com.vislupus.mindmath.ui.Screen
import com.vislupus.mindmath.ui.Tab
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import kotlin.random.Random

/**
 * Renders the real app (real database, real screens) on the computer and
 * saves a picture of every screen to app/build/screenshots.
 *
 * Run with:  gradlew testDebugUnitTest -Pscreenshots --tests "*Screenshots"
 *
 * The device profile matches a Redmi Note 8 Pro (1080 x 2340, 440 dpi).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = PHONE)
abstract class ScreenshotBase(
    private val language: String,
    private val theme: String,
    private val seedHistory: Boolean = true,
) {

    /** Fills the database with a few weeks of practice before the app starts. */
    @get:Rule(order = 0)
    val seed = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            val repository = Repository(app)
            repository.saveSettings(Settings(language = language, theme = theme))
            if (!seedHistory) return
            val rnd = Random(42)
            val today = LocalDate.now()
            for (back in 40 downTo 0) {
                // Leave a few gaps, but keep the last days active for a streak.
                if (back > 6 && rnd.nextInt(4) == 0) continue
                val day = today.minusDays(back.toLong())
                val count = if (back == 0) 9 else rnd.nextInt(8, 34)
                val spec = SessionSpec(Mode.DAILY, count = count)
                val id = repository.startSession(spec, day, 0)
                val outcomes = (1..count).map {
                    val method = Methods.enabled[rnd.nextInt(Methods.enabled.size * 2 / 3)]
                    val problem = method.problem(rnd.nextInt(1, 3), rnd)
                    val correct = rnd.nextInt(100) < 84
                    val time = rnd.nextLong(2500, 16000)
                    Outcome(problem, if (correct) problem.answer.toPlainString() else "12", correct, time, false, if (correct) Progress.xpFor(problem.level, time, false) else 0)
                }
                outcomes.forEach { repository.saveAttempt(id, it, day, 0) }
                repository.finishSession(id, outcomes, 60_000, 0)
            }
            val speed = repository.startSession(SessionSpec(Mode.SPEED, seconds = 60), today.minusDays(2), 0)
            val fast = (1..14).map { Outcome(Methods.enabled[0].problem(1, rnd), "1", true, 4000, false, 15) }
            repository.finishSession(speed, fast, 60_000, 0)
        }
    }

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private val vm: AppViewModel get() = ViewModelProvider(rule.activity)[AppViewModel::class.java]

    private fun ui(block: AppViewModel.() -> Unit) {
        rule.runOnUiThread { vm.block() }
        settle()
    }

    /** Lets background database work and animations finish. */
    private fun settle() {
        Thread.sleep(250)
        rule.mainClock.advanceTimeBy(1200)
        rule.waitForIdle()
    }

    private fun shot(name: String) {
        settle()
        rule.onRoot().captureRoboImage("build/screenshots/$name.png")
    }

    private fun tourTabs(prefix: String) {
        rule.waitUntil(10_000) { vm.ready }
        shot("${prefix}_home")
        ui { tab = Tab.LEARN }
        shot("${prefix}_learn")
        ui { tab = Tab.PRACTICE }
        shot("${prefix}_practice")
        ui { tab = Tab.STATS }
        shot("${prefix}_stats")
        ui { tab = Tab.HOME; open(Screen.Settings) }
        shot("${prefix}_settings")
        ui { back() }
    }

    private fun tourFlow(prefix: String) {
        rule.waitUntil(10_000) { vm.ready }
        val t = Texts.forCode(language)

        // Lesson with the demonstration fully revealed.
        ui { openMethod("mul_cross") }
        shot("${prefix}_lesson_top")
        repeat(4) {
            runCatching { rule.onNodeWithText(t.nextStep).performScrollTo().performClick() }
            settle()
        }
        shot("${prefix}_lesson_steps")
        ui { back() }

        // A drill: typing, a correct answer, a hint, a wrong answer, the result.
        ui { startSession(SessionSpec(Mode.DRILL, "mul_11", 1, 3)) }
        shot("${prefix}_session_empty")
        ui { session!!.input = session!!.problem.answer.toPlainString().dropLast(1) }
        shot("${prefix}_session_typing")
        ui {
            session!!.input = session!!.problem.answer.toPlainString()
            submit()
        }
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeBy(300)
        rule.onRoot().captureRoboImage("build/screenshots/${prefix}_session_correct.png")
        rule.mainClock.autoAdvance = true
        settle()
        if (vm.session?.phase != Phase.ANSWERING) ui { next() }
        ui { session!!.hintsShown = 2 }
        shot("${prefix}_session_hint")
        ui {
            session!!.input = "123"
            submit()
        }
        shot("${prefix}_session_wrong")
        ui { next() }
        ui {
            session!!.input = session!!.problem.answer.toPlainString()
            submit()
        }
        settle()
        if (vm.session != null) ui { next() }
        settle()
        shot("${prefix}_result")
        ui { back() }

        // Percent problem with a decimal answer, and a speed round.
        ui { startSession(SessionSpec(Mode.DRILL, "pct_change", 3, 5)) }
        ui { session!!.input = "48.5" }
        shot("${prefix}_session_percent")
        ui { finishSession() }
        ui { startSession(SessionSpec(Mode.SPEED, seconds = 60)) }
        shot("${prefix}_session_speed")
        ui { finishSession() }
    }

    protected val prefix: String get() = "${language}_$theme"

    protected fun tour() {
        tourTabs(prefix)
        tourFlow(prefix)
    }

    protected fun tourTall() {
        tourTabs("${prefix}_tall")
        tourFlow("${prefix}_tall")
    }

    protected fun tourEmpty() {
        rule.waitUntil(10_000) { vm.ready }
        shot("empty_home")
        ui { tab = Tab.STATS }
        shot("empty_stats")
    }
}

// One class per variant: every class runs in its own process, so each one
// starts with a clean app and database.

class EnglishLightScreenshots : ScreenshotBase("en", "light") {
    @Test
    fun phone() = tour()
}

class EnglishLightTallScreenshots : ScreenshotBase("en", "light") {
    @Test
    @Config(qualifiers = TALL)
    fun tall() = tourTall()
}

class BulgarianDarkScreenshots : ScreenshotBase("bg", "dark") {
    @Test
    fun phone() = tour()
}

class BulgarianDarkTallScreenshots : ScreenshotBase("bg", "dark") {
    @Test
    @Config(qualifiers = TALL)
    fun tall() = tourTall()
}

class FirstLaunchScreenshots : ScreenshotBase("en", "light", seedHistory = false) {
    @Test
    fun phone() = tourEmpty()
}

/** Redmi Note 8 Pro: 1080 x 2340 px at 440 dpi. */
private const val PHONE = "w393dp-h851dp-440dpi"

/** Same width but very tall, to see whole scrolling pages at once. */
private const val TALL = "w393dp-h2900dp-440dpi"
