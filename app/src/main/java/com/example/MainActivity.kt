package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.local.SettingsManager
import com.example.ui.navigation.ChronovaNavGraph
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsManager = SettingsManager(this)

        setContent {
            MyApplicationTheme {
                val isOnboardingCompleted by settingsManager.isOnboardingCompleted.collectAsState(initial = null)
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isOnboardingCompleted != null) {
                        val startDest = if (isOnboardingCompleted == true) "home" else "onboarding1"
                        ChronovaNavGraph(startDestination = startDest)
                    }
                }
            }
        }
    }
}

