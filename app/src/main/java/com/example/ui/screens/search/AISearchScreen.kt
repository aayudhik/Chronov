package com.example.ui.screens.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISearchScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToInsights: () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Timeline, contentDescription = "Timeline") },
                    label = { Text("Timeline") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Memories") },
                    label = { Text("Memories") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Search") },
                    label = { Text("AI Search") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToInsights,
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Insights") },
                    label = { Text("Insights") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("AI Search", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))

                // Search Input
                TextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Ask Chronova to find a memory...") },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 4.dp).size(40.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(8.dp))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Suggestion Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        SuggestionChip(
                            onClick = { },
                            label = { Text("Show every mountain trip") },
                            icon = { Icon(Icons.Default.Landscape, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = CircleShape
                        )
                    }
                    item {
                        SuggestionChip(
                            onClick = { },
                            label = { Text("Find sunset photos") },
                            icon = { Icon(Icons.Default.WbTwilight, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = CircleShape
                        )
                    }
                    item {
                        SuggestionChip(
                            onClick = { },
                            label = { Text("Most visited restaurant") },
                            icon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = CircleShape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Curated for you", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                Text("Memories you might be looking for today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))

                // Bento Grid 
                // Card 1: Full width
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuBZJm-X5IvfwnBiMmtxX5dUyMDJpYNw2LMOyZ4LheM8sqk12G_6BLDKT2Stcx2LLA30vninhQ3xtsE_rWEmH8M4qxAtRjrGE0dbkHoKKDh5vk1NEP6rju2ypSOZCMkMoPCRcEIMpGjC4nw5B_aPVrxbsHzhaVF8NIgG9A4ytpYl5n9Sa86jbdYIfka7enx4jGw0DIRoiyP2P9WtR_YCwYi7QTEHqghTatBISa2oatrqVD1pj29vobIg"),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    )
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GENERATED MEMORY", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text("Summer in the Alps", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            Surface(color = Color.White.copy(alpha = 0.2f), shape = CircleShape) {
                                Text("Aug 2023", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Card 2
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuAyHrv0w3kzIS6vSyL0e80adq6-JAx7l3qAAGLe4DlRLr7rfFmNelpIGhFeyG9i9wf4ROZcaFLj8HDNVym8RY0Agg0qi6nfow7ELwF-LvLp4kCkJC9V_YvQqp2bO4KvPMdaHl18DFgDzcJ8aLWBKlZaCHIM_7pZO1QdTayYd-3XIVYwFZNaNslRjokDErYwndR_PgtsjeoiM0wSvrD9IxeQTy1CZRvOwt_AOq1zRCHc6k8eVXTbyKJk"),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
                        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                            Text("Dinner at Le Petit", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Medium)
                            Text("Paris, France", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Card 3
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuB5OHWuGZHTzh7liHSBNv2zDsfsjm02yWEOZ-NgMhiG3l9PVLlSMO6sxCKE-bG8CDs8SL-QlJIcQCgDhDl3QqXGYcVG10ESlOKmpb1UYJP2hHY3b6vFAbbCE7rC9l7zZ_UdP5-BfXe7ainDc-13qKkVxmGCzTCvR796qx5NZ0i2VvtV-Jp2f1KLojdPx4AvfdgWVBjCaUzqvBTIAgF77tTaRyh1JUHpYyaIq1cvRCG5K4dFsyGBFoIH"),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
                        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                            Text("Coastal Retreat", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Medium)
                            Text("Malibu", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pattern Detected
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = borderStroke(MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(8.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Pattern Detected", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "You tend to capture the most photos during the golden hour (5 PM - 7 PM). Would you like to view a collection of your sunset memories?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("View Sunset Collection")
                        }
                    }
                }
            }
        }
    }
}

private fun borderStroke(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.dp, color)
}
