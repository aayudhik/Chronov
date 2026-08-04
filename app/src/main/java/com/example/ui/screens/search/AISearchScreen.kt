package com.example.ui.screens.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.ChronovaApplication
import com.example.data.local.MemoryWithMedia
import com.example.ui.components.viewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Long) -> Unit
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: AISearchViewModel = viewModel(
        factory = viewModelFactory { AISearchViewModel(appContainer.memoryRepository) }
    )

    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Search") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Ask anything... e.g., 'When was my last beach trip?'") },
                leadingIcon = {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(32.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        viewModel.performSearch()
                    }
                ),
                singleLine = true
            )

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is AISearchUiState.Idle -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Search your memories",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    is AISearchUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is AISearchUiState.Success -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                // AI Summary Box
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Gemini Summary",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = state.summary.ifBlank { "Thinking..." },
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            
                            if (state.matchedMemories.isNotEmpty()) {
                                item {
                                    Text(
                                        "Matched Memories",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }
                                
                                items(state.matchedMemories) { memory ->
                                    SearchMemoryCard(
                                        memoryWithMedia = memory,
                                        onClick = { onNavigateToDetails(memory.memory.id) }
                                    )
                                }
                            }
                        }
                    }
                    is AISearchUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchMemoryCard(memoryWithMedia: MemoryWithMedia, onClick: () -> Unit) {
    val imageUrls = memoryWithMedia.media.filter { it.type == "image" }.map { it.url }
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageUrls.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrls.first()),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memoryWithMedia.memory.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memoryWithMedia.memory.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatter.format(Date(memoryWithMedia.memory.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
