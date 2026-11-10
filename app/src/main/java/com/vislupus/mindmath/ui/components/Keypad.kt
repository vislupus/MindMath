package com.vislupus.mindmath.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig.Dimens
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette

/** Keys the keypad can report. */
sealed interface Key {
    data class Digit(val char: Char) : Key
    data object Decimal : Key
    data object Backspace : Key
    data object Clear : Key
}

/**
 * The app's own number pad, so the system keyboard never covers the problem.
 * A long press on backspace clears the whole answer.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Keypad(decimalSeparator: String, enabled: Boolean, onKey: (Key) -> Unit, modifier: Modifier = Modifier) {
    val rows = listOf("123", "456", "789")
    Column(modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f), verticalArrangement = Arrangement.spacedBy(Dimens.keyGap)) {
        for (row in rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.keyGap)) {
                for (char in row) KeyButton(Modifier.weight(1f), enabled, { onKey(Key.Digit(char)) }) {
                    Text(char.toString(), style = AppType.key, color = palette.textPrimary)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.keyGap)) {
            KeyButton(Modifier.weight(1f), enabled, { onKey(Key.Decimal) }) {
                Text(decimalSeparator, style = AppType.key, color = palette.textPrimary)
            }
            KeyButton(Modifier.weight(1f), enabled, { onKey(Key.Digit('0')) }) {
                Text("0", style = AppType.key, color = palette.textPrimary)
            }
            KeyButton(Modifier.weight(1f), enabled, { onKey(Key.Backspace) }, onLongClick = { onKey(Key.Clear) }) {
                AppIcon(AppIcons.backspace, tint = palette.textPrimary)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyButton(
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .height(Dimens.keyHeight)
            .clip(RoundedCornerShape(Dimens.keyRadius))
            .background(palette.surfaceAlt)
            .combinedClickable(enabled = enabled, role = Role.Button, onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
