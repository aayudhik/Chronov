package com.example.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen2(onStart: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .align(Alignment.TopStart)
                .offset(x = (-50).dp, y = (-50).dp)
                .blur(100.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chronova",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Privacy",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .background(Color(0xFF22C55E), CircleShape)
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Secure",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Privacy First.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Everything belongs to you. Secure,\nprivate, and transparent.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape))
                Box(modifier = Modifier.size(width = 32.dp, height = 8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            }

            Button(
                onClick = { 
                    scope.launch {
                        com.example.data.local.SettingsManager(context).setOnboardingCompleted(true)
                        onStart()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Start My Journey", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Start")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
