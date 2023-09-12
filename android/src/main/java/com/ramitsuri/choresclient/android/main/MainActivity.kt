package com.ramitsuri.choresclient.android.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramitsuri.choresclient.android.NavGraph
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val viewModel: MainViewModel = viewModel(factory = MainViewModel.factory)
            val state by viewModel.state.collectAsStateWithLifecycle()
            val startDestination = state.startDestination
            splashScreen.setKeepOnScreenCondition { startDestination == null }
            ChoresClientTheme {
                if (startDestination != null) {
                    NavGraph(startDestination = startDestination)
                }
            }
        }
    }
}
