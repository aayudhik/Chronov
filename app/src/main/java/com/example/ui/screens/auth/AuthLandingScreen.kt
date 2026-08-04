package com.example.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AuthLandingScreen(
    onEmailAuth: () -> Unit,
    onPhoneAuth: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Chronova",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Remember Every Moment.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onEmailAuth,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Continue with Email")
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = onPhoneAuth,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Continue with Phone")
            }
        }
    }
}
