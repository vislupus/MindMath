package com.vislupus.mindmath.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig
import com.vislupus.mindmath.config.ThemeConfig.Dimens
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette

// Small building blocks shared by all screens, so the interface stays uniform.

/** A Lucide icon. */
@Composable
fun AppIcon(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    tint: Color = palette.textPrimary,
    size: Dp = Dimens.iconSize,
    contentDescription: String? = null,
) {
    Icon(painterResource(id), contentDescription, modifier.size(size), tint)
}

/** An icon inside a softly tinted rounded square. */
@Composable
fun IconBadge(@DrawableRes icon: Int, color: Color, modifier: Modifier = Modifier, size: Dp = Dimens.iconBadge) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(color.copy(alpha = ThemeConfig.ICON_BADGE_TINT)),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(icon, tint = color, size = size * 0.48f)
    }
}

/** The standard card: flat, rounded, with a hairline border. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = palette.surface,
    borderColor: Color = palette.border,
    padding: Dp = Dimens.cardPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(Dimens.cardRadius)
    var base = modifier
        .clip(shape)
        .background(color)
        .border(Dimens.borderWidth, borderColor, shape)
    if (onClick != null) base = base.clickable(onClick = onClick)
    Column(base.padding(padding), content = content)
}

/** Round icon button (back, close, settings). */
@Composable
fun IconButton(@DrawableRes icon: Int, description: String, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = palette.textPrimary) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(palette.surface)
            .border(Dimens.borderWidth, palette.border, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(icon, tint = tint, size = 20.dp, contentDescription = description)
    }
}

/** Main call-to-action button. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    background: Color = palette.primary,
    contentColor: Color = palette.onPrimary,
) {
    Row(
        modifier
            .height(Dimens.buttonHeight)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(Dimens.buttonRadius))
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            AppIcon(icon, tint = contentColor, size = 20.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = AppType.button, color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Quieter button for secondary actions. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(Dimens.buttonRadius)
    Row(
        modifier
            .height(Dimens.buttonHeight)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(palette.surface)
            .border(Dimens.borderWidth, palette.border, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            AppIcon(icon, size = 18.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = AppType.button, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Title of a screen with an optional subtitle and trailing content. */
@Composable
fun ScreenHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = AppType.h1, color = palette.textPrimary)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = AppType.body, color = palette.textSecondary)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

/** Header of sub-screens: back button and centred title. */
@Composable
fun TopBar(title: String, onBack: () -> Unit, backDescription: String, @DrawableRes backIcon: Int = AppIcons.back) {
    Box(Modifier.fillMaxWidth().padding(horizontal = Dimens.screenPadding, vertical = 10.dp)) {
        IconButton(backIcon, backDescription, onBack, Modifier.align(Alignment.CenterStart))
        Text(
            title,
            style = AppType.title,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 56.dp),
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: (@Composable RowScope.() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = AppType.h2, color = palette.textPrimary, modifier = Modifier.weight(1f))
        if (trailing != null) trailing()
    }
}

/** Small rounded label, e.g. a category or "New". */
@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier, @DrawableRes icon: Int? = null) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = ThemeConfig.ICON_BADGE_TINT))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            AppIcon(icon, tint = color, size = 13.dp)
            Spacer(Modifier.width(5.dp))
        }
        Text(text, style = AppType.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = color, maxLines = 1)
    }
}

/** Segmented control: pick exactly one of a few options. */
@Composable
fun <T> Segmented(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.chipRadius)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.surfaceAlt)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for ((value, label) in options) {
            val active = value == selected
            val inner = RoundedCornerShape(Dimens.chipRadius - 3.dp)
            Box(
                Modifier
                    .weight(1f)
                    .height(Dimens.chipHeight)
                    .clip(inner)
                    // On dark themes the card colour is darker than the track, so lighten instead.
                    .background(
                        when {
                            !active -> Color.Transparent
                            palette.isDark -> palette.textPrimary.copy(alpha = 0.14f)
                            else -> palette.surface
                        },
                    )
                    .clickable(role = Role.Tab) { onSelect(value) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = AppType.label,
                    color = if (active) palette.textPrimary else palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Horizontal progress bar; [progress] is 0..1. */
@Composable
fun ProgressBar(progress: Float, modifier: Modifier = Modifier, color: Color = palette.primary, track: Color = palette.surfaceAlt, height: Dp = Dimens.progressHeight) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(ThemeConfig.Motion.PROGRESS), label = "progress")
    Box(modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(50)).background(track)) {
        Box(Modifier.fillMaxWidth(animated).fillMaxHeight().clip(RoundedCornerShape(50)).background(color))
    }
}

/** Circular progress with free content in the middle. */
@Composable
fun ProgressRing(
    progress: Float,
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = palette.primary,
    track: Color = palette.surfaceAlt,
    stroke: Dp = Dimens.ringStroke,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(ThemeConfig.Motion.PROGRESS), label = "ring")
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val width = stroke.toPx()
            val inset = width / 2
            val arcSize = Size(this.size.width - width, this.size.height - width)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(width))
            if (animated > 0f) {
                drawArc(color, -90f, 360f * animated, false, Offset(inset, inset), arcSize, style = Stroke(width, cap = StrokeCap.Round))
            }
        }
        content()
    }
}

/** Number with a label and an icon, used on the overview tiles. */
@Composable
fun StatTile(@DrawableRes icon: Int, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    AppCard(modifier, padding = 16.dp) {
        IconBadge(icon, color, size = 36.dp)
        Spacer(Modifier.height(14.dp))
        Text(value, style = AppType.statValue, color = palette.textPrimary, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(label, style = AppType.caption, color = palette.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Row with icon, title, description and something on the right. */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    titleStyle: TextStyle = AppType.title,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = titleStyle, color = palette.textPrimary, maxLines = 4, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = AppType.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), color = palette.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun AppSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = palette.onPrimary,
            checkedTrackColor = palette.primary,
            uncheckedThumbColor = palette.textMuted,
            uncheckedTrackColor = palette.surfaceAlt,
            uncheckedBorderColor = palette.border,
        ),
    )
}

/** Thin separator line inside cards. */
@Composable
fun Divider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(Dimens.borderWidth).background(palette.border))
}

/** Yes/no question in a dialog. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        titleContentColor = palette.textPrimary,
        textContentColor = palette.textSecondary,
        shape = RoundedCornerShape(Dimens.cardRadius),
        title = { Text(title, style = AppType.h2) },
        text = { Text(message, style = AppType.body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, style = AppType.bodyMedium, color = if (destructive) palette.error else palette.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText, style = AppType.bodyMedium, color = palette.textSecondary) }
        },
    )
}
