package com.vislupus.mindmath.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig.Dimens
import com.vislupus.mindmath.core.Mode
import com.vislupus.mindmath.core.SessionSpec
import com.vislupus.mindmath.texts.args
import com.vislupus.mindmath.ui.AppViewModel
import com.vislupus.mindmath.ui.Tab
import com.vislupus.mindmath.ui.components.AppCard
import com.vislupus.mindmath.ui.components.AppIcon
import com.vislupus.mindmath.ui.components.IconBadge
import com.vislupus.mindmath.ui.components.ListRow
import com.vislupus.mindmath.ui.components.MethodGroups
import com.vislupus.mindmath.ui.components.ScreenHeader
import com.vislupus.mindmath.ui.components.SectionTitle
import com.vislupus.mindmath.ui.components.Segmented
import com.vislupus.mindmath.ui.levelName
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette
import com.vislupus.mindmath.ui.theme.texts

/** Choose how to practise: a mixed mode or a drill of one method. */
@Composable
fun PracticeScreen(vm: AppViewModel) {
    val t = texts
    val stats = vm.dashboard
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(vm.scroll.getValue(Tab.PRACTICE))
            .padding(horizontal = Dimens.screenPadding)
            .padding(top = 20.dp, bottom = 28.dp),
    ) {
        ScreenHeader(t.practiceTitle, t.practiceSubtitle)

        Spacer(Modifier.height(24.dp))
        SectionTitle(t.modes)
        Spacer(Modifier.height(14.dp))

        ModeCard(AppIcons.dailyWorkout, palette.primary, t.dailyWorkout, t.dailyWorkoutDesc.args(AppConfig.DAILY_WORKOUT_LENGTH)) {
            vm.startSession(SessionSpec(Mode.DAILY, count = AppConfig.DAILY_WORKOUT_LENGTH))
        }
        Spacer(Modifier.height(Dimens.itemGap))

        // Speed round: the card starts it, the selector below sets its length.
        AppCard(padding = 0.dp) {
            ModeRow(AppIcons.speedRound, palette.warning, t.speedRound, t.speedRoundDesc.args(vm.speedSeconds)) {
                vm.startSession(SessionSpec(Mode.SPEED, seconds = vm.speedSeconds))
            }
            Segmented(
                options = AppConfig.SPEED_ROUND_OPTIONS.map { it to t.secondsValue.args(it) },
                selected = vm.speedSeconds,
                onSelect = { vm.speedSeconds = it },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
        Spacer(Modifier.height(Dimens.itemGap))

        val hasMistakes = stats.mistakes.isNotEmpty()
        ModeCard(
            AppIcons.mistakes,
            palette.error,
            t.mistakesReview,
            if (hasMistakes) t.mistakesReviewDesc.args(stats.mistakes.size) else t.mistakesNone,
            enabled = hasMistakes,
        ) { vm.startSession(SessionSpec(Mode.MISTAKES)) }

        Spacer(Modifier.height(Dimens.sectionGap))
        SectionTitle(t.methodDrill)
        Spacer(Modifier.height(4.dp))
        Text(t.methodDrillDesc, style = AppType.body, color = palette.textSecondary)

        Spacer(Modifier.height(16.dp))
        Text(t.difficulty, style = AppType.label, color = palette.textSecondary)
        Spacer(Modifier.height(8.dp))
        Segmented(
            options = (0..3).map { it to t.levelName(it) },
            selected = vm.drillLevel,
            onSelect = { vm.drillLevel = it },
        )
        Spacer(Modifier.height(14.dp))
        Text(t.problemsCount, style = AppType.label, color = palette.textSecondary)
        Spacer(Modifier.height(8.dp))
        Segmented(
            options = AppConfig.DRILL_LENGTH_OPTIONS.map { it to it.toString() },
            selected = vm.drillLength,
            onSelect = { vm.drillLength = it },
        )

        Spacer(Modifier.height(22.dp))
        MethodGroups(stats) { method ->
            vm.startSession(SessionSpec(Mode.DRILL, method.id, vm.drillLevel, vm.drillLength))
        }
    }
}

@Composable
private fun ModeCard(@DrawableRes icon: Int, color: Color, title: String, subtitle: String, enabled: Boolean = true, onClick: () -> Unit) {
    AppCard(Modifier.alpha(if (enabled) 1f else 0.55f), padding = 0.dp) {
        ModeRow(icon, color, title, subtitle, if (enabled) onClick else null)
    }
}

@Composable
private fun ModeRow(@DrawableRes icon: Int, color: Color, title: String, subtitle: String, onClick: (() -> Unit)?) {
    val rowModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ListRow(
        title = title,
        subtitle = subtitle,
        modifier = rowModifier.padding(16.dp),
        leading = { IconBadge(icon, color) },
        trailing = { AppIcon(AppIcons.play, tint = if (onClick != null) color else palette.textMuted, size = 20.dp) },
    )
}
