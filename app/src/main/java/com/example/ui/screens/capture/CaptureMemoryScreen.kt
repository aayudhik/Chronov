package com.example.ui.screens.capture

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ChronovaApplication
import com.example.ui.components.viewModelFactory
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureMemoryScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: CaptureViewModel = viewModel(
        factory = viewModelFactory { CaptureViewModel(appContainer.memoryRepository) }
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capture Memory") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.updateFavorite(!uiState.isFavorite) }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveMemory() },
                icon = { Icon(Icons.Default.Check, contentDescription = "Save") },
                text = { Text("Save Memory") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        if (uiState.isSaving) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = uiState.location,
                    onValueChange = viewModel::updateLocation,
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.mood,
                        onValueChange = viewModel::updateMood,
                        label = { Text("Mood") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.weather,
                        onValueChange = viewModel::updateWeather,
                        label = { Text("Weather") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = uiState.tags,
                    onValueChange = viewModel::updateTags,
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.photos,
                    onValueChange = viewModel::updatePhotos,
                    label = { Text("Photos (comma separated URLs)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.videos,
                    onValueChange = viewModel::updateVideos,
                    label = { Text("Videos (comma separated URLs)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.voiceNotes,
                    onValueChange = viewModel::updateVoiceNotes,
                    label = { Text("Voice Notes (comma separated URLs)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
