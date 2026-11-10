package com.vislupus.mindmath.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig
import com.vislupus.mindmath.core.Category
import com.vislupus.mindmath.core.MathMethod
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.core.Step
import com.vislupus.mindmath.data.Dashboard
import com.vislupus.mindmath.data.MethodStat
import com.vislupus.mindmath.ui.categoryIcon
import com.vislupus.mindmath.ui.categoryName
import com.vislupus.mindmath.ui.percent
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette
import com.vislupus.mindmath.ui.theme.texts

/** One method in a list: icon, name, tagline and mastery. */
@Composable
fun MethodRow(method: MathMethod, stat: MethodStat, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val text = texts.method(method.id)
    val color = palette.category(method.category)
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ThemeConfig.Dimens.cardPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(categoryIcon(method.category), color)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(text.title, style = AppType.title, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(text.tagline, style = AppType.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), color = palette.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (stat.total > 0) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressBar(stat.mastery, Modifier.weight(1f), color = color, height = 4.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(percent(stat.mastery), style = AppType.caption, color = palette.textMuted)
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        when {
            stat.isMastered -> AppIcon(AppIcons.mastered, tint = palette.success, size = 20.dp)
            stat.total == 0 -> Pill(texts.newBadge, palette.primary)
            else -> AppIcon(AppIcons.chevron, tint = palette.textMuted, size = 18.dp)
        }
    }
}

/** All enabled methods grouped by category, one card per category. */
@Composable
fun MethodGroups(stats: Dashboard, onMethod: (MathMethod) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(ThemeConfig.Dimens.sectionGap - 8.dp)) {
        for (category in Methods.categories) {
            Column {
                CategoryHeader(category)
                Spacer(Modifier.height(10.dp))
                AppCard(padding = 0.dp) {
                    val methods = Methods.inCategory(category)
                    methods.forEachIndexed { index, method ->
                        MethodRow(method, stats.method(method.id), { onMethod(method) })
                        if (index < methods.lastIndex) Divider(Modifier.padding(start = 76.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(category: Category, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        AppIcon(categoryIcon(category), tint = palette.category(category), size = 16.dp)
        Spacer(Modifier.width(8.dp))
        Text(texts.categoryName(category), style = AppType.label, color = palette.textSecondary)
    }
}

/**
 * A worked solution. Shows the first [visible] steps; the final "answer" step
 * is highlighted.
 */
@Composable
fun SolutionSteps(methodId: String, steps: List<Step>, modifier: Modifier = Modifier, visible: Int = steps.size) {
    val t = texts
    Column(modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            AnimatedVisibility(
                visible = index < visible,
                enter = fadeIn(tween(ThemeConfig.Motion.STEP_REVEAL)) + expandVertically(tween(ThemeConfig.Motion.STEP_REVEAL)),
            ) {
                val isResult = step.key == "result"
                val accent = if (isResult) palette.success else palette.primary
                // Spacing sits inside the item so hidden steps take no room.
                Row(Modifier.padding(top = if (index == 0) 0.dp else 14.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier
                            .padding(top = 1.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = ThemeConfig.ICON_BADGE_TINT)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isResult) {
                            AppIcon(AppIcons.check, tint = accent, size = 13.dp)
                        } else {
                            Text("${index + 1}", style = AppType.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = accent)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.stepCaption(methodId, step.key), style = AppType.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), color = palette.textSecondary)
                        Spacer(Modifier.height(2.dp))
                        Text(t.localize(step.expr), style = AppType.stepExpr, color = if (isResult) accent else palette.textPrimary)
                    }
                }
            }
        }
    }
}
