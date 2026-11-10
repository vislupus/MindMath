package com.vislupus.mindmath.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.BuildConfig
import com.vislupus.mindmath.R
import com.vislupus.mindmath.config.AppConfig
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.config.ThemeConfig.Dimens
import com.vislupus.mindmath.texts.Texts
import com.vislupus.mindmath.texts.args
import com.vislupus.mindmath.ui.AppViewModel
import com.vislupus.mindmath.ui.components.AppCard
import com.vislupus.mindmath.ui.components.AppIcon
import com.vislupus.mindmath.ui.components.AppSwitch
import com.vislupus.mindmath.ui.components.ConfirmDialog
import com.vislupus.mindmath.ui.components.Divider
import com.vislupus.mindmath.ui.components.IconBadge
import com.vislupus.mindmath.ui.components.ListRow
import com.vislupus.mindmath.ui.components.Segmented
import com.vislupus.mindmath.ui.components.TopBar
import com.vislupus.mindmath.ui.theme.AppType
import com.vislupus.mindmath.ui.theme.palette
import com.vislupus.mindmath.ui.theme.texts
import androidx.compose.ui.res.stringResource

/** Language, theme, daily goal, practice preferences and data reset. */
@Composable
fun SettingsScreen(vm: AppViewModel) {
    val t = texts
    val settings = vm.settings
    var confirmReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopBar(t.settingsTitle, { vm.back() }, t.close)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenPadding)
                .padding(top = 8.dp, bottom = 28.dp),
        ) {
            Choice(AppIcons.language, t.language) {
                Segmented(
                    options = Texts.all.map { it.languageCode to it.languageName },
                    selected = settings.language,
                    onSelect = { vm.updateSettings(settings.copy(language = it)) },
                )
            }
            Spacer(Modifier.height(Dimens.itemGap))
            Choice(AppIcons.theme, t.theme) {
                Segmented(
                    options = listOf("system" to t.themeSystem, "light" to t.themeLight, "dark" to t.themeDark),
                    selected = settings.theme,
                    onSelect = { vm.updateSettings(settings.copy(theme = it)) },
                )
            }
            Spacer(Modifier.height(Dimens.itemGap))
            Choice(AppIcons.goal, "${t.dailyGoal} · ${t.dailyGoalDesc}") {
                Segmented(
                    // Keep a custom default visible even if it is not in the list.
                    options = (AppConfig.DAILY_GOAL_OPTIONS + settings.dailyGoal).distinct().sorted().map { it to it.toString() },
                    selected = settings.dailyGoal,
                    onSelect = { vm.updateSettings(settings.copy(dailyGoal = it)) },
                )
            }

            Spacer(Modifier.height(Dimens.sectionGap))
            Label(t.preferences)
            AppCard(Modifier.fillMaxWidth(), padding = 0.dp) {
                Toggle(AppIcons.timer, t.showTimer, t.showTimerDesc, settings.showTimer) { vm.updateSettings(settings.copy(showTimer = it)) }
                Divider()
                Toggle(AppIcons.methodName, t.showMethodName, t.showMethodNameDesc, settings.showMethodName) { vm.updateSettings(settings.copy(showMethodName = it)) }
                Divider()
                Toggle(AppIcons.inputDirection, t.rtlInput, t.rtlInputDesc, settings.rightToLeftInput) { vm.updateSettings(settings.copy(rightToLeftInput = it)) }
                Divider()
                Toggle(AppIcons.haptics, t.haptics, t.hapticsDesc, settings.haptics) { vm.updateSettings(settings.copy(haptics = it)) }
            }

            Spacer(Modifier.height(Dimens.sectionGap))
            Label(t.dataSection)
            AppCard(Modifier.fillMaxWidth(), padding = 0.dp) {
                ListRow(
                    title = t.storageInfo,
                    modifier = Modifier.padding(16.dp),
                    leading = { IconBadge(AppIcons.storage, palette.primary, size = 36.dp) },
                    titleStyle = AppType.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal),
                )
                Divider()
                ListRow(
                    title = t.resetProgress,
                    subtitle = t.resetDesc,
                    modifier = Modifier.clickable { confirmReset = true }.padding(16.dp),
                    leading = { IconBadge(AppIcons.reset, palette.error, size = 36.dp) },
                    trailing = { AppIcon(AppIcons.chevron, tint = palette.textMuted, size = 18.dp) },
                )
            }

            Spacer(Modifier.height(Dimens.sectionGap))
            Label(t.about)
            AppCard(Modifier.fillMaxWidth(), padding = 0.dp) {
                ListRow(
                    title = stringResource(R.string.app_name),
                    subtitle = "${t.version.args(BuildConfig.VERSION_NAME)}\n${t.credits}",
                    modifier = Modifier.padding(16.dp),
                    leading = { IconBadge(AppIcons.about, palette.primary, size = 36.dp) },
                )
            }
        }
    }

    if (confirmReset) {
        ConfirmDialog(
            title = t.resetTitle,
            message = t.resetMessage,
            confirmText = t.resetConfirm,
            dismissText = t.cancel,
            destructive = true,
            onConfirm = {
                confirmReset = false
                vm.resetProgress()
            },
            onDismiss = { confirmReset = false },
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = AppType.label, color = palette.textSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 10.dp))
}

/** A card with a title and a selector underneath. */
@Composable
private fun Choice(@DrawableRes icon: Int, title: String, content: @Composable () -> Unit) {
    AppCard(Modifier.fillMaxWidth(), padding = 16.dp) {
        ListRow(title = title, leading = { AppIcon(icon, tint = palette.textSecondary, size = 20.dp) })
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun Toggle(@DrawableRes icon: Int, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListRow(
        title = title,
        subtitle = subtitle,
        modifier = Modifier.clickable { onChange(!checked) }.padding(horizontal = 16.dp, vertical = 12.dp),
        leading = { IconBadge(icon, palette.primary, size = 36.dp) },
        trailing = { AppSwitch(checked, onChange) },
    )
}
