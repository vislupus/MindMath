package com.vislupus.mindmath.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig.Dimens
import com.vislupus.mindmath.core.MathMethod
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.core.Mode
import com.vislupus.mindmath.core.SessionSpec
import com.vislupus.mindmath.data.Dashboard
import com.vislupus.mindmath.texts.args
import com.vislupus.mindmath.ui.AppViewModel
import com.vislupus.mindmath.ui.Screen
import com.vislupus.mindmath.ui.Tab
import com.vislupus.mindmath.ui.components.AppCard
import com.vislupus.mindmath.ui.components.AppIcon
import com.vislupus.mindmath.ui.components.Bar
import com.vislupus.mindmath.ui.components.BarChart
import com.vislupus.mindmath.ui.components.IconBadge
import com.vislupus.mindmath.ui.components.IconButton
import com.vislupus.mindmath.ui.components.MethodRow
import com.vislupus.mindmath.ui.components.PrimaryButton
import com.vislupus.mindmath.ui.components.ProgressBar
import com.vislupus.mindmath.ui.components.ProgressRing
import com.vislupus.mindmath.ui.components.ScreenHeader
import com.vislupus.mindmath.ui.components.SectionTitle
import com.vislupus.mindmath.ui.percent
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette
import com.vislupus.mindmath.ui.theme.texts
import java.time.LocalTime

/** Start page: today's goal, streak, level and shortcuts into practice. */
@Composable
fun HomeScreen(vm: AppViewModel) {
    val t = texts
    val stats = vm.dashboard
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> t.greetingMorning
        in 12..17 -> t.greetingAfternoon
        else -> t.greetingEvening
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(vm.scroll.getValue(Tab.HOME))
            .padding(horizontal = Dimens.screenPadding)
            .padding(top = 20.dp, bottom = 28.dp),
    ) {
        ScreenHeader(greeting, t.homeTagline) {
            IconButton(AppIcons.settings, t.settingsTitle, { vm.open(Screen.Settings) })
        }

        Spacer(Modifier.height(24.dp))
        GoalCard(stats, vm.settings.dailyGoal) {
            vm.startSession(SessionSpec(Mode.DAILY, count = AppConfig.DAILY_WORKOUT_LENGTH))
        }

        Spacer(Modifier.height(Dimens.itemGap))
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
            MiniStat(AppIcons.streak, palette.warning, t.days(stats.currentStreak), t.streak, Modifier.weight(1f))
            MiniStat(AppIcons.xp, palette.primary, stats.todayStat.xp.toString(), t.xpToday, Modifier.weight(1f))
        }

        Spacer(Modifier.height(Dimens.itemGap))
        LevelCard(stats)

        Spacer(Modifier.height(Dimens.sectionGap))
        SectionTitle(t.quickPractice)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
            QuickCard(
                icon = AppIcons.speedRound,
                color = palette.warning,
                title = t.speedRound,
                subtitle = t.speedRoundDesc.args(vm.speedSeconds),
                enabled = true,
                modifier = Modifier.weight(1f),
            ) { vm.startSession(SessionSpec(Mode.SPEED, seconds = vm.speedSeconds)) }
            QuickCard(
                icon = AppIcons.mistakes,
                color = palette.error,
                title = t.mistakesReview,
                subtitle = if (stats.mistakes.isEmpty()) t.mistakesNone else t.mistakesReviewDesc.args(stats.mistakes.size),
                enabled = stats.mistakes.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) { vm.startSession(SessionSpec(Mode.MISTAKES)) }
        }

        val suggestion = suggestedMethod(stats)
        if (suggestion != null) {
            Spacer(Modifier.height(Dimens.sectionGap))
            SectionTitle(t.suggested)
            Spacer(Modifier.height(14.dp))
            AppCard(padding = 0.dp) {
                MethodRow(suggestion, stats.method(suggestion.id), { vm.openMethod(suggestion.id) })
            }
        }

        Spacer(Modifier.height(Dimens.sectionGap))
        SectionTitle(t.thisWeek)
        Spacer(Modifier.height(14.dp))
        AppCard {
            val weekdays = t.weekdaysShort
            val bars = (6 downTo 0).map { back ->
                val date = stats.today.minusDays(back.toLong())
                Bar(stats.day(date).total.toFloat(), weekdays[date.dayOfWeek.value - 1], highlighted = back == 0)
            }
            BarChart(bars, height = 96.dp, maxValue = maxOf(vm.settings.dailyGoal.toFloat(), bars.maxOf { it.value }))
        }
    }
}

/** The first method never tried, otherwise the one with the lowest mastery. */
private fun suggestedMethod(stats: Dashboard): MathMethod? =
    Methods.enabled.firstOrNull { stats.method(it.id).total == 0 }
        ?: Methods.enabled.filter { !stats.method(it.id).isMastered }.minByOrNull { stats.method(it.id).mastery }

@Composable
private fun GoalCard(stats: Dashboard, goal: Int, onStart: () -> Unit) {
    val t = texts
    val done = stats.todayStat.total
    val progress = if (goal <= 0) 1f else done.toFloat() / goal
    val reached = done >= goal
    val onColor = palette.onPrimary
    AppCard(color = palette.primary, borderColor = palette.primary, padding = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (reached) t.goalReached else t.dailyGoal, style = AppType.label, color = onColor.copy(alpha = 0.75f))
                Spacer(Modifier.height(6.dp))
                Text(t.goalProgress.args(done, goal), style = AppType.h2, color = onColor)
            }
            Spacer(Modifier.width(16.dp))
            ProgressRing(progress, 68.dp, color = onColor, track = onColor.copy(alpha = 0.22f), stroke = 7.dp) {
                if (reached) {
                    AppIcon(AppIcons.check, tint = onColor, size = 26.dp)
                } else {
                    Text(percent(progress.coerceIn(0f, 1f)), style = AppType.label.copy(fontWeight = FontWeight.Bold), color = onColor)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(t.dailyWorkout, style = AppType.title, color = onColor)
        Spacer(Modifier.height(2.dp))
        Text(t.dailyWorkoutDesc.args(AppConfig.DAILY_WORKOUT_LENGTH), style = AppType.label.copy(fontWeight = FontWeight.Normal), color = onColor.copy(alpha = 0.75f))
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            t.startWorkout,
            onStart,
            Modifier.fillMaxWidth(),
            icon = AppIcons.play,
            background = onColor,
            contentColor = palette.primary,
        )
    }
}

@Composable
private fun MiniStat(@DrawableRes icon: Int, color: Color, value: String, label: String, modifier: Modifier) {
    AppCard(modifier.fillMaxHeight(), padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, color, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(value, style = AppType.h2, color = palette.textPrimary, maxLines = 1)
                Text(label, style = AppType.caption, color = palette.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun LevelCard(stats: Dashboard) {
    val t = texts
    val info = stats.levelInfo
    AppCard(padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(AppIcons.level, palette.success, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.levelNumber.args(info.level), style = AppType.title, color = palette.textPrimary, modifier = Modifier.weight(1f))
                    Text(t.xpToNext.args(info.xpToNext, info.level + 1), style = AppType.caption, color = palette.textSecondary)
                }
                Spacer(Modifier.height(10.dp))
                ProgressBar(info.progress, color = palette.success)
            }
        }
    }
}

@Composable
private fun QuickCard(
    @DrawableRes icon: Int,
    color: Color,
    title: String,
    subtitle: String,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    AppCard(modifier.fillMaxHeight().alpha(if (enabled) 1f else 0.55f), onClick = if (enabled) onClick else null, padding = 16.dp) {
        IconBadge(icon, color, size = 40.dp)
        Spacer(Modifier.height(14.dp))
        Text(title, style = AppType.title, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = AppType.label.copy(fontWeight = FontWeight.Normal), color = palette.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
