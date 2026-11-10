package com.vislupus.mindmath.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.ThemeConfig
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette

/** One bar of a [BarChart]. */
data class Bar(val value: Float, val label: String, val highlighted: Boolean = false)

/**
 * Simple bar chart. Bars are scaled to the largest value (or [maxValue]).
 * Empty labels are skipped, which keeps long ranges readable.
 */
@Composable
fun BarChart(
    bars: List<Bar>,
    modifier: Modifier = Modifier,
    height: Dp = ThemeConfig.Dimens.chartHeight,
    maxValue: Float? = null,
    color: Color = palette.primary,
) {
    val top = (maxValue ?: bars.maxOfOrNull { it.value } ?: 0f).coerceAtLeast(1f)
    val gap = if (bars.size > 14) 3.dp else 8.dp
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
        for (bar in bars) {
            val fraction by animateFloatAsState(
                (bar.value / top).coerceIn(0f, 1f),
                tween(ThemeConfig.Motion.PROGRESS),
                label = "bar",
            )
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(height), contentAlignment = Alignment.BottomCenter) {
                    // Faint full-height track, so empty days are still visible.
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .widthIn(max = 28.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(palette.surfaceAlt.copy(alpha = 0.6f)),
                    )
                    if (fraction > 0f) {
                        Box(
                            Modifier
                                .fillMaxHeight(fraction.coerceAtLeast(0.04f))
                                .widthIn(max = 28.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (bar.highlighted) color else color.copy(alpha = 0.55f)),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    bar.label,
                    style = AppType.caption,
                    color = if (bar.highlighted) palette.textPrimary else palette.textMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

/**
 * Calendar heat map: one column per week, Monday on top.
 *
 * @param cells intensity 0..1 per day, oldest first; null = day outside the range
 */
@Composable
fun HeatMap(cells: List<Float?>, modifier: Modifier = Modifier, color: Color = palette.primary) {
    val weeks = (cells.size + 6) / 7
    if (weeks == 0) return
    val empty = palette.surfaceAlt
    Canvas(modifier.fillMaxWidth().aspectRatio(weeks / 7f)) {
        val cell = size.width / weeks
        val pad = cell * 0.12f
        val radius = CornerRadius(cell * 0.24f)
        cells.forEachIndexed { index, value ->
            if (value == null) return@forEachIndexed
            val column = index / 7
            val row = index % 7
            val fill = if (value <= 0f) empty else lerp(color.copy(alpha = 0.25f), color, value.coerceIn(0f, 1f))
            drawRoundRect(
                color = fill,
                topLeft = Offset(column * cell + pad, row * cell + pad),
                size = Size(cell - 2 * pad, cell - 2 * pad),
                cornerRadius = radius,
            )
        }
    }
}

/** The "less ... more" legend under the heat map. */
@Composable
fun HeatLegend(less: String, more: String, modifier: Modifier = Modifier, color: Color = palette.primary) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(less, style = AppType.caption, color = palette.textMuted)
        Spacer(Modifier.width(8.dp))
        val steps = listOf(palette.surfaceAlt, color.copy(alpha = 0.25f), lerp(color.copy(alpha = 0.25f), color, 0.5f), color)
        for (step in steps) {
            Box(Modifier.width(12.dp).height(12.dp).clip(RoundedCornerShape(3.dp)).background(step))
            Spacer(Modifier.width(4.dp))
        }
        Spacer(Modifier.width(4.dp))
        Text(more, style = AppType.caption, color = palette.textMuted)
    }
}
