package com.example.ui.screens.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.ChronovaApplication
import com.example.ui.components.viewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryDetailsScreen(
    memoryId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: MemoryDetailsViewModel = viewModel(
        factory = viewModelFactory { MemoryDetailsViewModel(appContainer.memoryRepository) }
    )
    
    LaunchedEffect(memoryId) {
        viewModel.loadMemory(memoryId)
    }

    val uiState by viewModel.uiState.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Memory") },
            text = { Text("Are you sure you want to delete this memory? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteMemory(onNavigateBack)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is MemoryDetailsUiState.Success) {
                        val memory = (uiState as MemoryDetailsUiState.Success).memoryWithMedia.memory
                        IconButton(
                            onClick = { viewModel.toggleFavorite() },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                imageVector = if (memory.isHero) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (memory.isHero) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                        IconButton(
                            onClick = { /* TODO Edit */ },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(
                            onClick = { /* TODO Share */ },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is MemoryDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MemoryDetailsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MemoryDetailsUiState.Success -> {
                    val memory = state.memoryWithMedia.memory
                    val media = state.memoryWithMedia.media
                    val images = media.filter { it.type == "image" || it.type == "video" }
                    val tags = media.filter { it.type == "tag" }
                    val audios = media.filter { it.type == "audio" }
                    
                    val heroImage = images.firstOrNull()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Hero Image Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        ) {
                            if (heroImage != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(heroImage.url),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.3f),
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.background
                                                ),
                                                startY = 0f,
                                                endY = Float.POSITIVE_INFINITY
                                            )
                                        )
                                )
                            }
                        }

                        // Content Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .offset(y = (-40).dp)
                        ) {
                            Text(
                                text = memory.title,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (memory.locationName.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(memory.locationName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (memory.temperature.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(memory.temperature, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (memory.sentiment.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Mood, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(memory.sentiment, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            
                            if (memory.score > 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI Score ${memory.score}/100", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = memory.notes,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                            )

                            if (tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(tags) { tag ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = tag.label,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (images.size > 1) {
                                Spacer(modifier = Modifier.height(32.dp))
                                Text("Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(images.drop(1)) { img ->
                                        Image(
                                            painter = rememberAsyncImagePainter(img.url),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(160.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                        )
                                    }
                                }
                            }
                            
                            if (audios.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(32.dp))
                                Text("Voice Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                audios.forEach { audio ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(8.dp))
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text("Voice Note", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("0:45", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                            }
                                        }
                                    }
                                }
                            }

                            val relatedMemories by viewModel.relatedMemories.collectAsState()
                            if (relatedMemories.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(32.dp))
                                Text("Related Memories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                relatedMemories.forEach { related ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val thumb = related.media.firstOrNull { it.type == "image" }
                                            if (thumb != null) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(thumb.url),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(related.memory.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text(related.memory.notes, style = MaterialTheme.typography.bodyMedium, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}
