package com.vislupus.mindmath.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig.Dimens
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.core.Mode
import com.vislupus.mindmath.core.fmt
import com.vislupus.mindmath.data.Outcome
import com.vislupus.mindmath.texts.args
import com.vislupus.mindmath.ui.AppViewModel
import com.vislupus.mindmath.ui.components.AppCard
import com.vislupus.mindmath.ui.components.AppIcon
import com.vislupus.mindmath.ui.components.Divider
import com.vislupus.mindmath.ui.components.Pill
import com.vislupus.mindmath.ui.components.PrimaryButton
import com.vislupus.mindmath.ui.components.ProgressRing
import com.vislupus.mindmath.ui.components.SecondaryButton
import com.vislupus.mindmath.ui.components.SectionTitle
import com.vislupus.mindmath.ui.components.SolutionSteps
import com.vislupus.mindmath.ui.percent
import com.vislupus.mindmath.ui.seconds
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette
import com.vislupus.mindmath.ui.theme.texts
import com.vislupus.mindmath.ui.typed

/** Summary after a session, with every mistake explained. */
@Composable
fun ResultScreen(vm: AppViewModel) {
    val result = remember { vm.result } ?: return
    val t = texts
    val headline = when {
        result.accuracy >= 1f -> t.resultPerfect
        result.accuracy >= 0.8f -> t.resultGreat
        result.accuracy >= 0.5f -> t.resultGood
        else -> t.resultKeepGoing
    }
    val ringColor = when {
        result.accuracy >= 0.8f -> palette.success
        result.accuracy >= 0.5f -> palette.warning
        else -> palette.error
    }
    val mistakes = result.outcomes.filter { !it.isCorrect }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenPadding)
                .padding(top = 36.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProgressRing(result.accuracy, 148.dp, color = ringColor, stroke = 12.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(percent(result.accuracy), style = AppType.display.copy(fontSize = AppType.h1.fontSize * 1.25f), color = palette.textPrimary)
                    Text(t.accuracy, style = AppType.caption, color = palette.textSecondary)
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(headline, style = AppType.h1, color = palette.textPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(t.resultCorrect.args(result.correct, result.total), style = AppType.body, color = palette.textSecondary)
            if (result.isRecord) {
                Spacer(Modifier.height(12.dp))
                Pill(t.newRecord, palette.warning, icon = AppIcons.level)
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
                ResultStat(AppIcons.time, palette.primary, t.seconds(result.avgTimeMs), t.avgTime, Modifier.weight(1f))
                ResultStat(AppIcons.xp, palette.warning, "+${result.xp}", t.xpEarned, Modifier.weight(1f))
                ResultStat(AppIcons.streak, palette.error, t.days(vm.dashboard.currentStreak), t.streak, Modifier.weight(1f))
            }

            if (mistakes.isNotEmpty()) {
                Spacer(Modifier.height(Dimens.sectionGap))
                SectionTitle(t.mistakesTitle)
                Spacer(Modifier.height(14.dp))
                AppCard(Modifier.fillMaxWidth(), padding = 0.dp) {
                    mistakes.forEachIndexed { index, outcome ->
                        MistakeRow(outcome)
                        if (index < mistakes.lastIndex) Divider()
                    }
                }
            }
        }

        Row(
            Modifier.padding(horizontal = Dimens.screenPadding).padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // A finished mistakes review can only be repeated while mistakes remain.
            val canRepeat = result.spec.mode != Mode.MISTAKES || vm.dashboard.mistakes.isNotEmpty()
            SecondaryButton(t.done, { vm.back() }, Modifier.weight(1f))
            if (canRepeat) {
                PrimaryButton(t.practiceAgain, { vm.startSession(result.spec) }, Modifier.weight(1.4f), icon = AppIcons.mistakes)
            }
        }
    }
}

@Composable
private fun ResultStat(@DrawableRes icon: Int, color: Color, value: String, label: String, modifier: Modifier) {
    AppCard(modifier, padding = 14.dp) {
        AppIcon(icon, tint = color, size = 18.dp)
        Spacer(Modifier.height(10.dp))
        Text(value, style = AppType.title.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = palette.textPrimary, maxLines = 1)
        Text(label, style = AppType.caption, color = palette.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** A wrong answer; tap to unfold the worked solution. */
@Composable
private fun MistakeRow(outcome: Outcome) {
    val t = texts
    var open by remember { mutableStateOf(false) }
    val problem = outcome.problem
    Column(Modifier.fillMaxWidth().clickableNoRipple { open = !open }.padding(Dimens.cardPadding)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(t.localize(problem.question), style = AppType.title.copy(fontFeatureSettings = "tnum"), color = palette.textPrimary)
                Spacer(Modifier.height(4.dp))
                val given = if (outcome.userAnswer.isEmpty()) t.skipped else t.typed(outcome.userAnswer) + problem.suffix
                Text("${t.yourAnswer}: $given", style = AppType.label, color = palette.error)
                Text("${t.correctAnswer}: ${t.localize(fmt(problem.answer))}${problem.suffix}", style = AppType.label, color = palette.success)
            }
            Spacer(Modifier.width(10.dp))
            AppIcon(if (open) AppIcons.collapse else AppIcons.expand, tint = palette.textMuted, size = 18.dp)
        }
        AnimatedVisibility(open) {
            Column {
                Spacer(Modifier.height(14.dp))
                val steps = remember(problem) { Methods.get(problem.methodId)?.solve(problem.operands).orEmpty() }
                SolutionSteps(problem.methodId, steps)
            }
        }
    }
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    ),
)
