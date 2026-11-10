package com.vislupus.mindmath.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig.Dimens
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.data.Dashboard
import com.vislupus.mindmath.texts.args
import com.vislupus.mindmath.ui.Achievement
import com.vislupus.mindmath.ui.AppViewModel
import com.vislupus.mindmath.ui.Tab
import com.vislupus.mindmath.ui.achievements
import com.vislupus.mindmath.ui.categoryIcon
import com.vislupus.mindmath.ui.components.AppCard
import com.vislupus.mindmath.ui.components.Bar
import com.vislupus.mindmath.ui.components.BarChart
import com.vislupus.mindmath.ui.components.CategoryHeader
import com.vislupus.mindmath.ui.components.Divider
import com.vislupus.mindmath.ui.components.HeatLegend
import com.vislupus.mindmath.ui.components.HeatMap
import com.vislupus.mindmath.ui.components.IconBadge
import com.vislupus.mindmath.ui.components.ListRow
import com.vislupus.mindmath.ui.components.ProgressBar
import com.vislupus.mindmath.ui.components.ScreenHeader
import com.vislupus.mindmath.ui.components.SectionTitle
import com.vislupus.mindmath.ui.components.Segmented
import com.vislupus.mindmath.ui.components.StatTile
import com.vislupus.mindmath.ui.duration
import com.vislupus.mindmath.ui.percent
import com.vislupus.mindmath.ui.seconds
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette
import com.vislupus.mindmath.ui.theme.texts
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

/** Progress over time: totals, activity by day, results by method, achievements. */
@Composable
fun StatsScreen(vm: AppViewModel) {
    val t = texts
    val stats = vm.dashboard
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(vm.scroll.getValue(Tab.STATS))
            .padding(horizontal = Dimens.screenPadding)
            .padding(top = 20.dp, bottom = 28.dp),
    ) {
        ScreenHeader(t.statsTitle, t.statsSubtitle)
        Spacer(Modifier.height(24.dp))

        if (stats.totalProblems == 0) {
            AppCard(Modifier.fillMaxWidth(), padding = 28.dp) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    IconBadge(AppIcons.trend, palette.primary, size = 56.dp)
                    Spacer(Modifier.height(16.dp))
                    Text(t.noData, style = AppType.h2, color = palette.textPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(t.noDataDesc, style = AppType.body, color = palette.textSecondary, textAlign = TextAlign.Center)
                }
            }
        } else {
            Overview(stats)
            Spacer(Modifier.height(Dimens.sectionGap))
            ActivitySection(vm, stats)
            Spacer(Modifier.height(Dimens.sectionGap))
            CalendarSection(stats, vm.settings.dailyGoal)
            Spacer(Modifier.height(Dimens.sectionGap))
            MethodSection(stats)
        }

        Spacer(Modifier.height(Dimens.sectionGap))
        AchievementSection(stats)
    }
}

@Composable
private fun Overview(stats: Dashboard) {
    val t = texts
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
            StatTile(AppIcons.problems, stats.totalProblems.toString(), t.totalProblems, palette.primary, Modifier.weight(1f))
            StatTile(AppIcons.accuracy, percent(stats.accuracy), t.accuracy, palette.success, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
            StatTile(AppIcons.time, t.duration(stats.totalTimeMs), t.timeTrained, palette.categoryAddSub, Modifier.weight(1f))
            StatTile(AppIcons.streak, t.days(stats.bestStreak), t.bestStreak, palette.warning, Modifier.weight(1f))
        }
        // Best speed rounds, one line per round length that was played.
        val records = AppConfig.SPEED_ROUND_OPTIONS.mapNotNull { seconds -> stats.bestSpeed[seconds]?.let { seconds to it } }
        if (records.isNotEmpty()) {
            AppCard(Modifier.fillMaxWidth(), padding = 0.dp) {
                records.forEachIndexed { index, (seconds, best) ->
                    ListRow(
                        title = t.bestSpeedRound.args(seconds),
                        modifier = Modifier.padding(16.dp),
                        leading = { IconBadge(AppIcons.speedRound, palette.warning, size = 36.dp) },
                        trailing = { Text(best.toString(), style = AppType.statValue, color = palette.textPrimary) },
                    )
                    if (index < records.lastIndex) Divider()
                }
            }
        }
    }
}

/** Bar chart of the last days; the metric and range can be switched. */
@Composable
private fun ActivitySection(vm: AppViewModel, stats: Dashboard) {
    val t = texts
    SectionTitle(t.activity)
    Spacer(Modifier.height(14.dp))
    AppCard(Modifier.fillMaxWidth()) {
        Segmented(
            options = listOf(0 to t.metricProblems, 1 to t.metricAccuracy, 2 to t.metricTime),
            selected = vm.chartMetric,
            onSelect = { vm.chartMetric = it },
        )
        Spacer(Modifier.height(8.dp))
        Segmented(
            options = AppConfig.CHART_RANGE_OPTIONS.map { it to t.daysValue.args(it) },
            selected = vm.chartRange,
            onSelect = { vm.chartRange = it },
        )
        Spacer(Modifier.height(20.dp))

        val range = vm.chartRange
        val days = (range - 1 downTo 0).map { stats.today.minusDays(it.toLong()) }
        val values = days.map { date ->
            val day = stats.day(date)
            when (vm.chartMetric) {
                1 -> day.accuracy * 100f
                2 -> day.timeMs / 60_000f
                else -> day.total.toFloat()
            }
        }
        val bars = days.mapIndexed { index, date ->
            val label = when {
                range <= 7 -> t.weekdaysShort[date.dayOfWeek.value - 1]
                // Long ranges: label every fifth day, counted back from today.
                (days.lastIndex - index) % 5 == 0 -> date.dayOfMonth.toString()
                else -> ""
            }
            Bar(values[index], label, highlighted = index == days.lastIndex)
        }
        BarChart(bars, maxValue = if (vm.chartMetric == 1) 100f else null)

        Spacer(Modifier.height(14.dp))
        Divider()
        Spacer(Modifier.height(12.dp))
        // Totals of the selected range.
        val inRange = days.map { stats.day(it) }
        val total = inRange.sumOf { it.total }
        val correct = inRange.sumOf { it.correct }
        val time = inRange.sumOf { it.timeMs }
        Row {
            RangeTotal(total.toString(), t.metricProblems, Modifier.weight(1f))
            RangeTotal(if (total == 0) "–" else percent(correct.toFloat() / total), t.accuracy, Modifier.weight(1f))
            RangeTotal(t.duration(time), t.timeTrained, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RangeTotal(value: String, label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = AppType.title.copy(fontWeight = FontWeight.Bold), color = palette.textPrimary, maxLines = 1)
        Text(label, style = AppType.caption, color = palette.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

/** Heat map of the last weeks; a full-colour cell means the daily goal was met. */
@Composable
private fun CalendarSection(stats: Dashboard, dailyGoal: Int) {
    val t = texts
    SectionTitle(t.calendar)
    Spacer(Modifier.height(14.dp))
    AppCard(Modifier.fillMaxWidth()) {
        val weeks = AppConfig.HEATMAP_WEEKS
        val monday = stats.today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val first = monday.minusWeeks((weeks - 1).toLong())
        val goal = dailyGoal.coerceAtLeast(1)
        val cells = (0 until weeks * 7).map { offset ->
            val date = first.plusDays(offset.toLong())
            if (date.isAfter(stats.today)) null else (stats.day(date).total.toFloat() / goal).coerceIn(0f, 1f)
        }
        HeatMap(cells)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            HeatLegend(t.less, t.more)
        }
    }
}

/** Accuracy, speed and mastery of every method that has been practised. */
@Composable
private fun MethodSection(stats: Dashboard) {
    val t = texts
    SectionTitle(t.byMethod)
    Spacer(Modifier.height(14.dp))
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        for (category in Methods.categories) {
            val methods = Methods.inCategory(category).filter { stats.method(it.id).total > 0 }
            if (methods.isEmpty()) continue
            Column {
                CategoryHeader(category)
                Spacer(Modifier.height(10.dp))
                AppCard(Modifier.fillMaxWidth(), padding = 0.dp) {
                    methods.forEachIndexed { index, method ->
                        val stat = stats.method(method.id)
                        val color = palette.category(category)
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(t.method(method.id).title, style = AppType.bodyMedium, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "${t.answersValue.args(stat.total)} · ${t.seconds(stat.avgTimeMs)}",
                                        style = AppType.caption,
                                        color = palette.textSecondary,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(percent(stat.accuracy), style = AppType.title.copy(fontWeight = FontWeight.Bold), color = palette.textPrimary)
                            }
                            Spacer(Modifier.height(10.dp))
                            ProgressBar(stat.mastery, color = color, height = 4.dp)
                        }
                        if (index < methods.lastIndex) Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementSection(stats: Dashboard) {
    val t = texts
    val all = achievements(stats)
    SectionTitle(t.achievements) {
        Text("${all.count { it.unlocked }} / ${all.size}", style = AppType.label, color = palette.textSecondary)
    }
    Spacer(Modifier.height(14.dp))
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
        for (pair in all.chunked(2)) {
            Row(Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
                for (achievement in pair) AchievementCard(achievement, Modifier.weight(1f))
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, modifier: Modifier) {
    val text = texts.achievementTexts[achievement.id] ?: return
    val color = if (achievement.unlocked) palette.warning else palette.textMuted
    AppCard(modifier.fillMaxHeight(), padding = 16.dp) {
        IconBadge(achievement.icon, color, size = 40.dp)
        Spacer(Modifier.height(12.dp))
        Text(text.title, style = AppType.bodyMedium, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.alpha(if (achievement.unlocked) 1f else 0.75f))
        Spacer(Modifier.height(2.dp))
        Text(text.description.args(*achievement.args.toTypedArray()), style = AppType.caption, color = palette.textSecondary, minLines = 2, maxLines = 3)
        Spacer(Modifier.height(12.dp))
        ProgressBar(achievement.progress, color = color, height = 4.dp)
    }
}

