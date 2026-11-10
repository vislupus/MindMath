package com.vislupus.mindmath.ui.screens

import android.os.SystemClock
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig.Dimens
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.core.Mode
import com.vislupus.mindmath.core.fmt
import com.vislupus.mindmath.ui.AppViewModel
import com.vislupus.mindmath.ui.Phase
import com.vislupus.mindmath.ui.SessionState
import com.vislupus.mindmath.ui.categoryIcon
import com.vislupus.mindmath.ui.components.AppCard
import com.vislupus.mindmath.ui.components.AppIcon
import com.vislupus.mindmath.ui.components.ConfirmDialog
import com.vislupus.mindmath.ui.components.IconButton
import com.vislupus.mindmath.ui.components.Key
import com.vislupus.mindmath.ui.components.Keypad
import com.vislupus.mindmath.ui.components.Pill
import com.vislupus.mindmath.ui.components.PrimaryButton
import com.vislupus.mindmath.ui.components.ProgressBar
import com.vislupus.mindmath.ui.components.SolutionSteps
import com.vislupus.mindmath.ui.seconds
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette
import com.vislupus.mindmath.ui.theme.texts
import com.vislupus.mindmath.ui.typed
import kotlinx.coroutines.delay

/** The page where problems are solved. */
@Composable
fun SessionScreen(vm: AppViewModel) {
    // Keep the session while this page fades out after it has ended.
    val state = remember { vm.session } ?: return
    val t = texts
    val view = LocalView.current
    val haptics = vm.settings.haptics
    var confirmQuit by remember { mutableStateOf(false) }

    // A clock that ticks ten times a second drives the timer and countdown.
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(state) {
        while (true) {
            now = SystemClock.elapsedRealtime()
            val deadline = state.deadline
            if (deadline != null && now >= deadline) {
                vm.finishSession()
                break
            }
            delay(100)
        }
    }

    // Feedback and automatic move to the next problem.
    LaunchedEffect(state.index, state.phase) {
        when (state.phase) {
            Phase.CORRECT -> {
                if (haptics) view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                delay(AppConfig.CORRECT_FEEDBACK_DELAY_MS)
                vm.next()
            }
            Phase.WRONG -> {
                if (haptics) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                // A speed round never waits for a tap.
                if (state.spec.mode == Mode.SPEED) {
                    delay(AppConfig.CORRECT_FEEDBACK_DELAY_MS * 2)
                    vm.next()
                }
            }
            Phase.ANSWERING -> Unit
        }
    }

    fun quit() {
        if (state.outcomes.isEmpty()) vm.finishSession() else confirmQuit = true
    }
    BackHandler { quit() }

    fun onKey(key: Key) {
        if (state.phase != Phase.ANSWERING) return
        if (haptics) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val input = state.input
        val rtl = state.rightToLeft
        state.input = when (key) {
            is Key.Digit -> when {
                input.length >= AppConfig.MAX_ANSWER_LENGTH -> input
                rtl -> key.char + input
                else -> input + key.char
            }
            Key.Decimal -> when {
                '.' in input || input.length >= AppConfig.MAX_ANSWER_LENGTH -> input
                rtl -> ".$input"
                input.isEmpty() -> "0."
                else -> "$input."
            }
            Key.Backspace -> if (rtl) input.drop(1) else input.dropLast(1)
            Key.Clear -> ""
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = Dimens.screenPadding).padding(top = 10.dp, bottom = 16.dp)) {
        SessionTopBar(state, { now }, vm.settings.showTimer, onClose = { quit() })

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(18.dp))
            val method = Methods.get(state.problem.methodId)
            if (vm.settings.showMethodName && method != null) {
                Pill(t.method(method.id).title, palette.category(method.category), icon = categoryIcon(method.category))
                Spacer(Modifier.height(14.dp))
            }
            val question = t.localize(state.problem.question)
            Text(
                question,
                // Long questions get a smaller size so they stay on one line.
                style = AppType.display.copy(fontSize = if (question.length > 13) 30.sp else if (question.length > 9) 36.sp else AppType.display.fontSize),
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Spacer(Modifier.height(18.dp))
            AnswerField(state)

            when (state.phase) {
                Phase.WRONG -> {
                    Spacer(Modifier.height(14.dp))
                    AppCard(Modifier.fillMaxWidth()) {
                        Text(t.howToSolve, style = AppType.label, color = palette.textSecondary)
                        Spacer(Modifier.height(14.dp))
                        SolutionSteps(state.problem.methodId, state.steps)
                    }
                }
                Phase.ANSWERING -> if (state.hintsShown > 0) {
                    Spacer(Modifier.height(14.dp))
                    AppCard(Modifier.fillMaxWidth(), color = palette.warningSoft, borderColor = palette.warningSoft) {
                        SolutionSteps(state.problem.methodId, state.steps, visible = state.hintsShown)
                    }
                }
                Phase.CORRECT -> Unit
            }
            Spacer(Modifier.height(12.dp))
        }

        if (state.phase == Phase.WRONG && state.spec.mode != Mode.SPEED) {
            PrimaryButton(t.next, { vm.next() }, Modifier.fillMaxWidth(), icon = AppIcons.forward)
        } else {
            val answering = state.phase == Phase.ANSWERING
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // The last step is the answer itself, so a hint never reveals it.
                val canHint = answering && state.hintsShown < state.steps.size - 1
                ToolButton(AppIcons.hint, t.hint, Modifier.weight(1f), enabled = canHint) { state.hintsShown++ }
                ToolButton(
                    null,
                    if (state.rightToLeft) "← 123" else "123 →",
                    Modifier.weight(1f),
                    enabled = answering,
                    active = state.rightToLeft,
                    description = t.inputDirection,
                ) {
                    state.rightToLeft = !state.rightToLeft
                    state.input = ""
                }
                ToolButton(AppIcons.skip, t.skip, Modifier.weight(1f), enabled = answering) { vm.submit(skipped = true) }
            }
            Spacer(Modifier.height(10.dp))
            Keypad(t.decimalSeparator, enabled = answering, onKey = { onKey(it) })
            Spacer(Modifier.height(10.dp))
            PrimaryButton(
                t.check,
                { vm.submit() },
                Modifier.fillMaxWidth(),
                icon = AppIcons.check,
                enabled = answering && state.input.isNotEmpty(),
            )
        }
    }

    if (confirmQuit) {
        ConfirmDialog(
            title = t.quitTitle,
            message = t.quitMessage,
            confirmText = t.quitConfirm,
            dismissText = t.quitStay,
            onConfirm = {
                confirmQuit = false
                vm.finishSession()
            },
            onDismiss = { confirmQuit = false },
        )
    }
}

/** Close button, progress (or countdown) and the timer. */
@Composable
private fun SessionTopBar(state: SessionState, clock: () -> Long, showTimer: Boolean, onClose: () -> Unit) {
    val t = texts
    // Reading the clock here keeps the ten-per-second updates inside this bar.
    val now = clock()
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(AppIcons.close, t.close, onClose)
        Spacer(Modifier.width(14.dp))
        val deadline = state.deadline
        if (deadline != null) {
            // Speed round: the bar empties as time runs out.
            val totalMs = state.spec.seconds * 1000f
            val left = (deadline - now).coerceAtLeast(0)
            ProgressBar(left / totalMs, Modifier.weight(1f), color = if (left < 10_000) palette.error else palette.warning)
            Spacer(Modifier.width(14.dp))
            Text("${(left + 999) / 1000} ${t.unitSeconds}", style = AppType.label, color = palette.textPrimary)
            Spacer(Modifier.width(12.dp))
            Pill(state.correctCount.toString(), palette.success, icon = AppIcons.check)
        } else {
            val total = state.total ?: 1
            val done = state.index + if (state.phase == Phase.ANSWERING) 0 else 1
            ProgressBar(done.toFloat() / total, Modifier.weight(1f))
            Spacer(Modifier.width(14.dp))
            Text("${state.index + 1} / $total", style = AppType.label, color = palette.textSecondary)
            if (showTimer) {
                Spacer(Modifier.width(12.dp))
                val elapsed = if (state.phase == Phase.ANSWERING) now - state.problemStartedAt else state.outcomes.lastOrNull()?.timeMs ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(AppIcons.timer, tint = palette.textMuted, size = 14.dp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        t.seconds(elapsed.coerceIn(0, AppConfig.MAX_ATTEMPT_SECONDS * 1000L)),
                        style = AppType.label.copy(fontFeatureSettings = "tnum"),
                        color = palette.textSecondary,
                    )
                }
            }
        }
    }
}

/** Shows what is being typed, and afterwards whether it was right. */
@Composable
private fun AnswerField(state: SessionState) {
    val t = texts
    val phase = state.phase
    val outcome = if (phase == Phase.ANSWERING) null else state.outcomes.lastOrNull()
    val border by animateColorAsState(
        when (phase) {
            Phase.CORRECT -> palette.success
            Phase.WRONG -> palette.error
            Phase.ANSWERING -> if (state.input.isEmpty()) palette.border else palette.primary
        },
        label = "answerBorder",
    )
    val fill by animateColorAsState(
        when (phase) {
            Phase.CORRECT -> palette.successSoft
            Phase.WRONG -> palette.errorSoft
            Phase.ANSWERING -> palette.surface
        },
        label = "answerFill",
    )
    val shape = RoundedCornerShape(Dimens.cardRadius)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(fill)
            .border(2.dp, border, shape)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val shown = when {
            state.input.isNotEmpty() -> t.typed(state.input) + state.problem.suffix
            phase == Phase.ANSWERING -> "?"
            else -> t.skipped
        }
        Text(
            shown,
            style = if (state.input.isEmpty() && phase != Phase.ANSWERING) AppType.h2 else AppType.answer,
            color = when {
                phase == Phase.CORRECT -> palette.success
                phase == Phase.WRONG -> palette.error
                state.input.isEmpty() -> palette.textMuted
                else -> palette.textPrimary
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (outcome != null) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(
                    if (outcome.isCorrect) AppIcons.correct else AppIcons.wrong,
                    tint = if (outcome.isCorrect) palette.success else palette.error,
                    size = 16.dp,
                )
                Spacer(Modifier.width(6.dp))
                val message = if (outcome.isCorrect) {
                    "${t.correct}  +${outcome.xp} XP"
                } else {
                    "${t.correctAnswer}: ${t.localize(fmt(state.problem.answer))}${state.problem.suffix}"
                }
                Text(message, style = AppType.bodyMedium, color = if (outcome.isCorrect) palette.success else palette.error)
            }
        }
    }
}

/** Small button above the keypad (hint, typing direction, skip). */
@Composable
private fun ToolButton(
    @DrawableRes icon: Int?,
    label: String,
    modifier: Modifier,
    enabled: Boolean,
    active: Boolean = false,
    description: String? = null,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(Dimens.chipRadius)
    val tint: Color = if (active) palette.primary else palette.textSecondary
    Row(
        modifier
            .height(Dimens.chipHeight)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(if (active) palette.primarySoft else Color.Transparent)
            .border(Dimens.borderWidth, if (active) palette.primarySoft else palette.border, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            AppIcon(icon, tint = tint, size = 16.dp)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            label,
            style = AppType.label,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = if (description != null) Modifier.semantics { contentDescription = description } else Modifier,
        )
    }
}
