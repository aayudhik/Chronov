package com.example.ui.screens.privacy

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ChronovaApplication

@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val privacyManager = (context.applicationContext as ChronovaApplication).container.privacyManager
    
    var pin by remember { mutableStateOf("") }
    val isBiometricEnabled by privacyManager.isBiometricEnabled.collectAsState()
    
    val showBiometric = remember { mutableStateOf(false) }

    LaunchedEffect(isBiometricEnabled) {
        if (isBiometricEnabled) {
            showBiometric.value = true
        }
    }

    if (showBiometric.value) {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                        showBiometric.value = false
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onUnlocked()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Chronova")
                .setSubtitle("Use your biometric credential to unlock")
                .setNegativeButtonText("Use PIN")
                .build()

            LaunchedEffect(Unit) {
                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            showBiometric.value = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Chronova is Locked", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Please enter your PIN to continue.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = pin,
                onValueChange = { 
                    pin = it
                    if (privacyManager.verifyPin(pin)) {
                        onUnlocked()
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Enter PIN") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (isBiometricEnabled) {
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = { showBiometric.value = true }) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use Biometrics")
                }
            }
        }
    }
}
