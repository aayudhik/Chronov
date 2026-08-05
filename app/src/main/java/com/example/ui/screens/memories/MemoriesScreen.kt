package com.example.ui.screens.memories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ChronovaApplication
import com.example.ui.components.viewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesScreen(onNavigateToDetails: (Long) -> Unit, onNavigateToMap: () -> Unit = {}) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: MemoriesViewModel = viewModel(
        factory = viewModelFactory { MemoriesViewModel(appContainer.memoryRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search memories and collections...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                viewModel.setSearchQuery("")
                            } else {
                                isSearchActive = false
                            }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("Memories", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    actions = {
                        IconButton(onClick = onNavigateToMap) {
                            Icon(androidx.compose.material.icons.Icons.Default.Map, contentDescription = "Map View", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = uiState.activeTab) {
                Tab(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    text = { Text("All Memories") }
                )
                Tab(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    text = { Text("Collections") }
                )
                Tab(
                    selected = uiState.activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    text = { Text("Life Chapters") }
                )
            }

            if (uiState.activeTab == 0) {
                AllMemoriesContent(uiState, viewModel, onNavigateToDetails)
            } else if (uiState.activeTab == 1) {
                CollectionsContent(uiState, viewModel, onNavigateToDetails)
            } else {
                LifeChaptersContent(uiState, viewModel, onNavigateToDetails)
            }
        }
    }
}

@Composable
fun AllMemoriesContent(uiState: MemoriesUiState, viewModel: MemoriesViewModel, onNavigateToDetails: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.availableTags.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedTag == null,
                        onClick = { viewModel.selectTag(null) },
                        label = { Text("All") }
                    )
                }
                items(uiState.availableTags) { tag ->
                    FilterChip(
                        selected = uiState.selectedTag == tag,
                        onClick = { viewModel.selectTag(tag) },
                        label = { Text(tag) }
                    )
                }
            }
        }

        if (uiState.memories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No memories found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.memories, key = { it.memory.id }) { memoryWithMedia ->
                    val imageUrl = memoryWithMedia.media.firstOrNull { it.type == "image" }?.url
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDetails(memoryWithMedia.memory.id) },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = memoryWithMedia.memory.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 300.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                        )
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = memoryWithMedia.memory.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    val dateString = dateFormat.format(Date(memoryWithMedia.memory.timestamp))
                                    Text(
                                        text = dateString,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionsContent(uiState: MemoriesUiState, viewModel: MemoriesViewModel, onNavigateToDetails: (Long) -> Unit) {
    if (uiState.collections.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No collections generated yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.collections, key = { it.title }) { collection ->
                CollectionCard(
                    collection = collection,
                    onPinToggle = { viewModel.togglePinCollection(collection) },
                    onNavigateToDetails = onNavigateToDetails
                )
            }
        }
    }
}

@Composable
fun CollectionCard(
    collection: CollectionItem,
    onPinToggle: () -> Unit,
    onNavigateToDetails: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (collection.coverImageUrl != null) {
                    AsyncImage(
                        model = collection.coverImageUrl,
                        contentDescription = collection.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = collection.title.take(1).uppercase(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${collection.memoryCount} items • ${collection.distinctLocations} places",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(
                        onClick = onPinToggle,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(50))
                    ) {
                        Icon(
                            imageVector = if (collection.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (collection.isPinned) "Unpin" else "Pin",
                            tint = Color.White
                        )
                    }
                }
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = collection.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Preview items
            if (collection.memories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(collection.memories.take(5)) { memoryWithMedia ->
                        val previewUrl = memoryWithMedia.media.firstOrNull { it.type == "image" }?.url
                        if (previewUrl != null) {
                            AsyncImage(
                                model = previewUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color.LightGray, RoundedCornerShape(12.dp))
                                    .clickable { onNavigateToDetails(memoryWithMedia.memory.id) },
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .clickable { onNavigateToDetails(memoryWithMedia.memory.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = memoryWithMedia.memory.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LifeChaptersContent(uiState: MemoriesUiState, viewModel: MemoriesViewModel, onNavigateToDetails: (Long) -> Unit) {
    var editingChapter by remember { mutableStateOf<com.example.data.local.LifeChapter?>(null) }

    if (uiState.lifeChapters.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No Life Chapters generated yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.lifeChapters, key = { it.id }) { chapter ->
                LifeChapterCard(
                    chapter = chapter,
                    onEditClick = { editingChapter = chapter }
                )
            }
        }
    }

    if (editingChapter != null) {
        var editTitle by remember { mutableStateOf(editingChapter!!.title) }
        var editSummary by remember { mutableStateOf(editingChapter!!.aiSummary) }
        var editMilestones by remember { mutableStateOf(editingChapter!!.milestones) }

        AlertDialog(
            onDismissRequest = { editingChapter = null },
            title = { Text("Edit Chapter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSummary,
                        onValueChange = { editSummary = it },
                        label = { Text("Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = editMilestones,
                        onValueChange = { editMilestones = it },
                        label = { Text("Milestones (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateLifeChapter(
                            editingChapter!!.copy(
                                title = editTitle,
                                aiSummary = editSummary,
                                milestones = editMilestones
                            )
                        )
                        editingChapter = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingChapter = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LifeChapterCard(chapter: com.example.data.local.LifeChapter, onEditClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (chapter.customCoverUrl.isNotBlank()) {
                    AsyncImage(
                        model = chapter.customCoverUrl,
                        contentDescription = chapter.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                    val startStr = dateFormat.format(Date(chapter.startTimestamp))
                    val endStr = dateFormat.format(Date(chapter.endTimestamp))
                    
                    Text(
                        text = "$startStr - $endStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = chapter.aiSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (chapter.milestones.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Milestones",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val milestonesList = chapter.milestones.split(",").map { it.trim() }
                    milestonesList.forEach { milestone ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = milestone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Generated Chapter",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    TextButton(onClick = onEditClick) {
                        Text("Edit Chapter")
                    }
                }
            }
        }
    }
}
