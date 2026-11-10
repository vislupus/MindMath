package com.vislupus.mindmath.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig
import com.vislupus.mindmath.texts.Texts
import com.vislupus.mindmath.ui.components.AppIcon
import com.vislupus.mindmath.ui.components.Divider
import com.vislupus.mindmath.ui.screens.HomeScreen
import com.vislupus.mindmath.ui.screens.LearnScreen
import com.vislupus.mindmath.ui.screens.MethodDetailScreen
import com.vislupus.mindmath.ui.screens.PracticeScreen
import com.vislupus.mindmath.ui.screens.ResultScreen
import com.vislupus.mindmath.ui.screens.SessionScreen
import com.vislupus.mindmath.ui.screens.SettingsScreen
import com.vislupus.mindmath.ui.screens.StatsScreen
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.MindMathTheme
import com.vislupus.mindmath.ui.theme.palette
import com.vislupus.mindmath.ui.theme.texts

/** Root of the user interface: theme, navigation and the bottom bar. */
@Composable
fun MindMathApp(vm: AppViewModel) {
    MindMathTheme(vm.settings.theme, Texts.forCode(vm.settings.language)) {
        Box(Modifier.fillMaxSize().background(palette.background).systemBarsPadding()) {
            if (!vm.ready) return@Box
            val top = vm.stack.lastOrNull()
            BackHandler(enabled = top != null || vm.tab != Tab.HOME) { vm.back() }
            AnimatedContent(
                targetState = top,
                transitionSpec = { fadeIn(tween(ThemeConfig.Motion.SCREEN_FADE)) togetherWith fadeOut(tween(ThemeConfig.Motion.SCREEN_FADE)) },
                label = "screen",
            ) { screen ->
                when (screen) {
                    null -> TabsScreen(vm)
                    is Screen.MethodDetail -> MethodDetailScreen(vm, screen.methodId)
                    Screen.Session -> SessionScreen(vm)
                    Screen.Result -> ResultScreen(vm)
                    Screen.Settings -> SettingsScreen(vm)
                }
            }
        }
    }
}

@Composable
private fun TabsScreen(vm: AppViewModel) {
    Column(Modifier.fillMaxSize()) {
        Crossfade(vm.tab, Modifier.weight(1f), tween(ThemeConfig.Motion.SCREEN_FADE), label = "tab") { tab ->
            when (tab) {
                Tab.HOME -> HomeScreen(vm)
                Tab.LEARN -> LearnScreen(vm)
                Tab.PRACTICE -> PracticeScreen(vm)
                Tab.STATS -> StatsScreen(vm)
            }
        }
        BottomBar(vm.tab) { vm.tab = it }
    }
}

@Composable
private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    val t = texts
    Column(Modifier.fillMaxWidth().background(palette.surface)) {
        Divider()
        Row(Modifier.fillMaxWidth().height(ThemeConfig.Dimens.bottomBarHeight)) {
            BottomItem(AppIcons.navHome, t.navHome, selected == Tab.HOME, Modifier.weight(1f)) { onSelect(Tab.HOME) }
            BottomItem(AppIcons.navLearn, t.navLearn, selected == Tab.LEARN, Modifier.weight(1f)) { onSelect(Tab.LEARN) }
            BottomItem(AppIcons.navPractice, t.navPractice, selected == Tab.PRACTICE, Modifier.weight(1f)) { onSelect(Tab.PRACTICE) }
            BottomItem(AppIcons.navStats, t.navStats, selected == Tab.STATS, Modifier.weight(1f)) { onSelect(Tab.STATS) }
        }
    }
}

@Composable
private fun BottomItem(@DrawableRes icon: Int, label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = if (active) palette.primary else palette.textMuted
    Column(
        modifier
            .fillMaxHeight()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(if (active) palette.primarySoft else Color.Transparent)
                .padding(horizontal = 18.dp, vertical = 5.dp),
        ) {
            AppIcon(icon, tint = color, size = 21.dp)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = AppType.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
