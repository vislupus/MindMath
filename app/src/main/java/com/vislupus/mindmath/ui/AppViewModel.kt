package com.vislupus.mindmath.ui

import android.app.Application
import android.os.SystemClock
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.core.Mode
import com.vislupus.mindmath.core.Problem
import com.vislupus.mindmath.core.ProblemSource
import com.vislupus.mindmath.core.Progress
import com.vislupus.mindmath.core.SessionSpec
import com.vislupus.mindmath.core.Step
import com.vislupus.mindmath.data.Dashboard
import com.vislupus.mindmath.data.Outcome
import com.vislupus.mindmath.data.Repository
import com.vislupus.mindmath.data.Settings
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.Executors

/** The four tabs of the bottom bar. */
enum class Tab { HOME, LEARN, PRACTICE, STATS }

/** Full-screen pages that open on top of the tabs. */
sealed interface Screen {
    data class MethodDetail(val methodId: String) : Screen
    data object Session : Screen
    data object Result : Screen
    data object Settings : Screen
}

/** What the session screen is currently showing. */
enum class Phase { ANSWERING, CORRECT, WRONG }

/** State of the running practice session. */
class SessionState(val spec: SessionSpec, private val source: ProblemSource, rightToLeft: Boolean) {
    /** Number of problems, or null in a timed round. */
    val total: Int? = source.total
    val startedAt: Long = SystemClock.elapsedRealtime()

    /** End of a speed round (elapsedRealtime), or null. */
    val deadline: Long? = if (spec.mode == Mode.SPEED) startedAt + spec.seconds * 1000L else null

    var problem: Problem by mutableStateOf(source.next())
        private set
    var steps: List<Step> by mutableStateOf(solve(problem))
        private set
    var index by mutableIntStateOf(0)
        private set
    var input by mutableStateOf("")
    var phase by mutableStateOf(Phase.ANSWERING)
        private set
    var hintsShown by mutableIntStateOf(0)
    var rightToLeft by mutableStateOf(rightToLeft)
    var problemStartedAt: Long = startedAt
        private set
    val outcomes = mutableStateListOf<Outcome>()

    val isLast: Boolean get() = total != null && index >= total - 1
    val correctCount: Int get() = outcomes.count { it.isCorrect }

    private fun solve(problem: Problem): List<Step> =
        Methods.get(problem.methodId)?.solve(problem.operands).orEmpty()

    fun record(outcome: Outcome) {
        outcomes += outcome
        source.report(outcome.isCorrect)
        phase = if (outcome.isCorrect) Phase.CORRECT else Phase.WRONG
    }

    fun advance() {
        problem = source.next()
        steps = solve(problem)
        index++
        input = ""
        hintsShown = 0
        phase = Phase.ANSWERING
        problemStartedAt = SystemClock.elapsedRealtime()
    }
}

/** Summary shown after a session. */
data class SessionResult(
    val spec: SessionSpec,
    val outcomes: List<Outcome>,
    val durationMs: Long,
    /** Only for speed rounds: true if this beat the previous best. */
    val isRecord: Boolean,
) {
    val total: Int get() = outcomes.size
    val correct: Int get() = outcomes.count { it.isCorrect }
    val accuracy: Float get() = if (total == 0) 0f else correct.toFloat() / total
    val xp: Int get() = outcomes.sumOf { it.xp }
    val avgTimeMs: Long get() = if (total == 0) 0 else outcomes.sumOf { it.timeMs } / total
}

/** Holds the app state and talks to the database on a background thread. */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)

    // One background thread keeps database writes in order.
    private val executor = Executors.newSingleThreadExecutor()
    private val io = executor.asCoroutineDispatcher()

    /** Id of the session row; only touched on the io thread. */
    private var sessionRowId = 0L

    // Settings are tiny, so they are read right away: the first frame already
    // has the right language and theme.
    var settings by mutableStateOf(repository.loadSettings())
        private set
    var dashboard by mutableStateOf(Dashboard.empty())
        private set
    var ready by mutableStateOf(false)
        private set

    // ---- Navigation ----------------------------------------------------------

    var tab by mutableStateOf(Tab.HOME)
    val stack = mutableStateListOf<Screen>()

    /** Scroll position of each tab, kept while other pages are open. */
    val scroll: Map<Tab, ScrollState> = Tab.entries.associateWith { ScrollState(0) }

    fun open(screen: Screen) {
        stack += screen
    }

    /** Closes the top page. Returns false when there was nothing to close. */
    fun back(): Boolean {
        if (stack.isNotEmpty()) {
            stack.removeAt(stack.lastIndex)
            return true
        }
        if (tab != Tab.HOME) {
            tab = Tab.HOME
            return true
        }
        return false
    }

    // ---- Choices remembered while the app is open ------------------------------

    var drillLevel by mutableIntStateOf(0)
    var drillLength by mutableIntStateOf(AppConfig.DEFAULT_DRILL_LENGTH)
    var speedSeconds by mutableIntStateOf(AppConfig.DEFAULT_SPEED_ROUND_SECONDS)
    var chartRange by mutableIntStateOf(AppConfig.DEFAULT_CHART_RANGE)
    var chartMetric by mutableIntStateOf(0)

    // ---- Session ---------------------------------------------------------------

    var session by mutableStateOf<SessionState?>(null)
        private set
    var result by mutableStateOf<SessionResult?>(null)
        private set

    init {
        refresh()
    }

    /** Re-reads all statistics from the database. */
    fun refresh() {
        viewModelScope.launch {
            dashboard = withContext(io) { repository.loadDashboard(LocalDate.now()) }
            ready = true
        }
    }

    fun updateSettings(new: Settings) {
        settings = new
        viewModelScope.launch(io) { repository.saveSettings(new) }
    }

    fun openMethod(methodId: String) {
        open(Screen.MethodDetail(methodId))
        if (methodId !in dashboard.lessonsViewed) {
            dashboard = dashboard.copy(lessonsViewed = dashboard.lessonsViewed + methodId)
            viewModelScope.launch(io) { repository.markLessonViewed(methodId, System.currentTimeMillis()) }
        }
    }

    fun startSession(spec: SessionSpec) {
        val stats = dashboard
        val mistakes = if (spec.mode == Mode.MISTAKES) stats.mistakes.map { it.problem } else emptyList()
        if (spec.mode == Mode.MISTAKES && mistakes.isEmpty()) return
        val source = ProblemSource(
            spec = spec,
            recommendedLevel = { stats.method(it).recommendedLevel },
            mastery = { stats.method(it).mastery },
            mistakes = mistakes,
        )
        session = SessionState(spec, source, settings.rightToLeftInput)
        result = null
        stack.removeAll { it is Screen.Result || it is Screen.Session }
        stack += Screen.Session
        val day = LocalDate.now()
        val now = System.currentTimeMillis()
        viewModelScope.launch(io) { sessionRowId = repository.startSession(spec, day, now) }
    }

    /** Checks the typed answer, or records a skip. */
    fun submit(skipped: Boolean = false) {
        val state = session ?: return
        if (state.phase != Phase.ANSWERING) return
        if (!skipped && Progress.parseAnswer(state.input) == null) return
        val elapsed = SystemClock.elapsedRealtime() - state.problemStartedAt
        val timeMs = elapsed.coerceIn(0, AppConfig.MAX_ATTEMPT_SECONDS * 1000L)
        val correct = !skipped && Progress.isCorrect(state.problem, state.input)
        val hintUsed = state.hintsShown > 0
        val outcome = Outcome(
            problem = state.problem,
            userAnswer = if (skipped) "" else state.input,
            isCorrect = correct,
            timeMs = timeMs,
            hintUsed = hintUsed,
            xp = if (correct) Progress.xpFor(state.problem.level, timeMs, hintUsed) else 0,
        )
        state.record(outcome)
        val day = LocalDate.now()
        val now = System.currentTimeMillis()
        viewModelScope.launch(io) { repository.saveAttempt(sessionRowId, outcome, day, now) }
    }

    /** Moves to the next problem, or to the result page after the last one. */
    fun next() {
        val state = session ?: return
        if (state.phase == Phase.ANSWERING) return
        val timeUp = state.deadline?.let { SystemClock.elapsedRealtime() >= it } ?: false
        if (state.isLast || timeUp) finishSession() else state.advance()
    }

    /** Ends the session and shows the result (or just closes it when empty). */
    fun finishSession() {
        val state = session ?: return
        session = null
        stack.removeAll { it is Screen.Session }
        val outcomes = state.outcomes.toList()
        val duration = SystemClock.elapsedRealtime() - state.startedAt
        val now = System.currentTimeMillis()
        if (outcomes.isNotEmpty()) {
            val previousBest = dashboard.bestSpeed[state.spec.seconds] ?: 0
            val correct = outcomes.count { it.isCorrect }
            result = SessionResult(
                spec = state.spec,
                outcomes = outcomes,
                durationMs = duration,
                isRecord = state.spec.mode == Mode.SPEED && correct > previousBest && correct > 0,
            )
            stack += Screen.Result
        }
        viewModelScope.launch {
            dashboard = withContext(io) {
                repository.finishSession(sessionRowId, outcomes, duration, now)
                repository.loadDashboard(LocalDate.now())
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            dashboard = withContext(io) {
                repository.resetProgress()
                repository.loadDashboard(LocalDate.now())
            }
        }
    }

    override fun onCleared() {
        executor.shutdown()
    }
}
