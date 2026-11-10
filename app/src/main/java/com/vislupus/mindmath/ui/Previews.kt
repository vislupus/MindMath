package com.vislupus.mindmath.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vislupus.mindmath.config.AppIcons
import com.vislupus.mindmath.core.Methods
import com.vislupus.mindmath.core.Operands
import com.vislupus.mindmath.texts.Texts
import com.vislupus.mindmath.ui.components.AppCard
import com.vislupus.mindmath.ui.components.Bar
import com.vislupus.mindmath.ui.components.BarChart
import com.vislupus.mindmath.ui.components.Keypad
import com.vislupus.mindmath.ui.components.PrimaryButton
import com.vislupus.mindmath.ui.components.ProgressBar
import com.vislupus.mindmath.ui.components.SecondaryButton
import com.vislupus.mindmath.ui.components.Segmented
import com.vislupus.mindmath.ui.components.SolutionSteps
import com.vislupus.mindmath.ui.components.StatTile
import com.vislupus.mindmath.ui.theme.MindMathTheme
import com.vislupus.mindmath.ui.theme.palette

// Previews for Android Studio: open this file and switch to "Split" or
// "Design" to see the building blocks without starting the app. They react
// instantly to changes in config/ThemeConfig.kt and the text files.

@Composable
private fun PreviewFrame(theme: String, language: String = "en", content: @Composable () -> Unit) {
    MindMathTheme(theme, Texts.forCode(language)) {
        Column(
            Modifier.fillMaxWidth().background(palette.background).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun Showcase() {
    PrimaryButton("Start workout", {}, Modifier.fillMaxWidth(), icon = AppIcons.play)
    SecondaryButton("New example", {}, Modifier.fillMaxWidth(), icon = AppIcons.newExample)
    Segmented(listOf(1 to "Easy", 2 to "Medium", 3 to "Hard"), 2, {})
    StatTile(AppIcons.streak, "8 days", "Streak", palette.warning, Modifier.fillMaxWidth())
    ProgressBar(0.6f)
    AppCard(Modifier.fillMaxWidth()) {
        BarChart(listOf(4f, 12f, 9f, 20f, 0f, 15f, 7f).mapIndexed { i, v -> Bar(v, "MTWTFSS"[i].toString(), i == 6) }, height = 90.dp)
    }
}

@Preview(name = "Components - light", widthDp = 393)
@Composable
private fun ComponentsLightPreview() = PreviewFrame("light") { Showcase() }

@Preview(name = "Components - dark", widthDp = 393)
@Composable
private fun ComponentsDarkPreview() = PreviewFrame("dark") { Showcase() }

@Preview(name = "Worked solution", widthDp = 393)
@Composable
private fun SolutionPreview() = PreviewFrame("light") {
    val method = Methods.get("mul_cross")!!
    AppCard(Modifier.fillMaxWidth()) { SolutionSteps(method.id, method.solve(Operands(47, 36))) }
}

@Preview(name = "Worked solution - Bulgarian, dark", widthDp = 393)
@Composable
private fun SolutionBulgarianPreview() = PreviewFrame("dark", "bg") {
    val method = Methods.get("pct_blocks")!!
    AppCard(Modifier.fillMaxWidth()) { SolutionSteps(method.id, method.solve(Operands(35, 240))) }
}

@Preview(name = "Keypad", widthDp = 393)
@Composable
private fun KeypadPreview() = PreviewFrame("light") { Keypad(".", enabled = true, onKey = {}) }
