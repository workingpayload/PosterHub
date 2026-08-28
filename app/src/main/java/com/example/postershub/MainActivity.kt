package com.example.postershub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.postershub.di.ServiceLocator
import com.example.postershub.ui.nav.PosterApp
import com.example.postershub.ui.theme.PostershubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val useSystemTheme by ServiceLocator.settingsStore.useSystemTheme.collectAsStateWithLifecycle(false)
            PostershubTheme(useSystemTheme = useSystemTheme) {
                PosterApp()
            }
        }
    }
}
