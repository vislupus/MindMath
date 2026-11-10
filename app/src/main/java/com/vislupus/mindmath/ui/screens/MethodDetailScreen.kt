package com.vislupus.mindmath.ui.screens

import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig.Dimens
import com.vislupus.mindmath.core.MathMethod
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.core.Mode
import com.vislupus.mindmath.core.SessionSpec
import com.vislupus.mindmath.ui.AppViewModel
import com.vislupus.mindmath.ui.categoryIcon
import com.vislupus.mindmath.ui.categoryName
import com.vislupus.mindmath.ui.components.AppCard
import com.vislupus.mindmath.ui.components.AppIcon
import com.vislupus.mindmath.ui.components.Pill
import com.vislupus.mindmath.ui.components.PrimaryButton
import com.vislupus.mindmath.ui.components.ProgressBar
import com.vislupus.mindmath.ui.components.SecondaryButton
import com.vislupus.mindmath.ui.components.Segmented
import com.vislupus.mindmath.ui.components.SolutionSteps
import com.vislupus.mindmath.ui.components.TopBar
import com.vislupus.mindmath.ui.levelName
import com.vislupus.mindmath.ui.percent
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette
import com.vislupus.mindmath.ui.theme.texts

/** Lesson of one method: explanation, interactive demonstration and a way to practise it. */
@Composable
fun MethodDetailScreen(vm: AppViewModel, methodId: String) {
    val method = Methods.get(methodId) ?: return
    val t = texts
    val text = t.method(methodId)
    val color = palette.category(method.category)
    val stat = vm.dashboard.method(methodId)

    Column(Modifier.fillMaxSize()) {
        TopBar(t.categoryName(method.category), { vm.back() }, t.close)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenPadding)
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            Pill(t.categoryName(method.category), color, icon = categoryIcon(method.category))
            Spacer(Modifier.height(12.dp))
            Text(text.title, style = AppType.h1, color = palette.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(text.tagline, style = AppType.body, color = palette.textSecondary)

            if (stat.total > 0) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.mastery, style = AppType.label, color = palette.textSecondary)
                    Spacer(Modifier.width(12.dp))
                    ProgressBar(stat.mastery, Modifier.weight(1f), color = color, height = 6.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(percent(stat.mastery), style = AppType.label, color = palette.textPrimary)
                }
            }

            Spacer(Modifier.height(22.dp))
            InfoCard(AppIcons.lesson, color, t.theIdea, text.idea)

            Spacer(Modifier.height(Dimens.itemGap))
            DemoCard(method)

            Spacer(Modifier.height(Dimens.itemGap))
            InfoCard(AppIcons.goal, color, t.whenToUse, text.whenToUse)

            if (text.tip.isNotBlank()) {
                Spacer(Modifier.height(Dimens.itemGap))
                AppCard(color = palette.warningSoft, borderColor = palette.warningSoft) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(AppIcons.tip, tint = palette.warning, size = 18.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(t.tipTitle, style = AppType.label, color = palette.warning)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(text.tip, style = AppType.body, color = palette.textPrimary)
                }
            }
        }

        PrimaryButton(
            t.practiceThis,
            { vm.startSession(SessionSpec(Mode.DRILL, methodId, vm.drillLevel, vm.drillLength)) },
            Modifier.fillMaxWidth().padding(horizontal = Dimens.screenPadding).padding(top = 8.dp, bottom = 16.dp),
            icon = AppIcons.play,
        )
    }
}

@Composable
private fun InfoCard(@DrawableRes icon: Int, color: Color, title: String, body: String) {
    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(icon, tint = color, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            Text(title, style = AppType.label, color = color)
        }
        Spacer(Modifier.height(8.dp))
        Text(body, style = AppType.body, color = palette.textPrimary)
    }
}

/** A worked example that is revealed one step at a time. */
@Composable
private fun DemoCard(method: MathMethod) {
    val t = texts
    var level by remember { mutableIntStateOf(1) }
    var problem by remember { mutableStateOf(method.problem(1)) }
    var visible by remember { mutableIntStateOf(1) }
    val steps = remember(problem) { method.solve(problem.operands) }

    fun newExample(newLevel: Int = level) {
        level = newLevel
        problem = method.problem(newLevel)
        visible = 1
    }

    AppCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(AppIcons.play, tint = palette.primary, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            Text(t.stepByStep, style = AppType.label, color = palette.primary)
        }
        Spacer(Modifier.height(14.dp))
        Segmented(
            options = (1..MathMethod.MAX_LEVEL).map { it to t.levelName(it) },
            selected = level,
            onSelect = { newExample(it) },
        )
        Spacer(Modifier.height(20.dp))
        Text(
            t.localize(problem.question),
            style = AppType.h1,
            color = palette.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        SolutionSteps(method.id, steps, visible = visible)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton(t.newExample, { newExample() }, Modifier.weight(1f), icon = AppIcons.newExample)
            if (visible < steps.size) {
                PrimaryButton(t.nextStep, { visible++ }, Modifier.weight(1f), icon = AppIcons.forward)
            }
        }
    }
}
