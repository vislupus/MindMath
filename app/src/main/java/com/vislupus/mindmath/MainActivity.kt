package com.vislupus.mindmath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.vislupus.mindmath.ui.AppViewModel
import com.vislupus.mindmath.ui.MindMathApp

/** The single screen of the app; everything else is drawn with Compose. */
class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { MindMathApp(viewModel) }
    }

    override fun onResume() {
        super.onResume()
        // Picks up a new day (streak, daily goal) when the app comes back.
        viewModel.refresh()
    }
}
