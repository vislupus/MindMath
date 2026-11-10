package com.vislupus.mindmath.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.ThemeConfig.Dimens
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.texts.args
import com.vislupus.mindmath.ui.AppViewModel
import com.vislupus.mindmath.ui.Tab
import com.vislupus.mindmath.ui.components.MethodGroups
import com.vislupus.mindmath.ui.components.ScreenHeader
import com.vislupus.mindmath.ui.theme.texts

/** The library of methods; each one opens its lesson. */
@Composable
fun LearnScreen(vm: AppViewModel) {
    val t = texts
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(vm.scroll.getValue(Tab.LEARN))
            .padding(horizontal = Dimens.screenPadding)
            .padding(top = 20.dp, bottom = 28.dp),
    ) {
        ScreenHeader(t.learnTitle, t.learnSubtitle.args(Methods.enabled.size))
        Spacer(Modifier.height(24.dp))
        MethodGroups(vm.dashboard) { vm.openMethod(it.id) }
    }
}
