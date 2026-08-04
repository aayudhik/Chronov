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
import com.example.ui.navigation.ChronovaScaffold
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsManager = SettingsManager(this)
        
        setContent {
            val appContainer = (applicationContext as ChronovaApplication).container
            val authRepository = appContainer.authRepository
            
            MyApplicationTheme {
                val isOnboardingCompleted by settingsManager.isOnboardingCompleted.collectAsState(initial = null)
                val isSignedIn by authRepository.isSignedIn.collectAsState(initial = null)
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isOnboardingCompleted != null && isSignedIn != null) {
                        val startDest = if (isOnboardingCompleted == false) {
                            "onboarding1"
                        } else if (!isSignedIn!!) {
                            "auth_landing"
                        } else {
                            "home"
                        }
                        ChronovaScaffold(startDestination = startDest)
                    }
                }
            }
        }
    }
}

