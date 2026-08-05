package com.example.ui.screens.intelligence

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ChronovaApplication
import com.example.domain.MemoryScores
import com.example.ui.components.viewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntelligenceScreen() {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: IntelligenceViewModel = viewModel(
        factory = viewModelFactory { IntelligenceViewModel(appContainer.memoryRepository, appContainer.memoryIntelligenceService) }
    )
    val uiState by viewModel.uiState.collectAsState()
    
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedMemory by remember { mutableStateOf<MemoryScores?>(null) }

    if (selectedMemory != null) {
        MemoryScoreDetail(
            scores = selectedMemory!!,
            onBack = { selectedMemory = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Intelligence", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("Sort by ${option.name.lowercase().replaceFirstChar { it.uppercase() }}") },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.top100.isEmpty()) {
                Text(
                    "No memories to analyze.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        "Top 100 Memories",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.top100, key = { it.memoryId }) { score ->
                            MemoryScoreCard(
                                score = score,
                                sortOption = uiState.currentSort,
                                onClick = { selectedMemory = score }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryScoreCard(score: MemoryScores, sortOption: SortOption, onClick: () -> Unit) {
    val displayScore = when (sortOption) {
        SortOption.OVERALL -> score.overallScore
        SortOption.IMPORTANCE -> score.importanceScore
        SortOption.EMOTIONAL -> score.emotionalScore
        SortOption.HAPPINESS -> score.happinessScore
        SortOption.CONFIDENCE -> score.confidenceScore
        SortOption.ACTIVITY -> score.activityScore
        SortOption.SOCIAL -> score.socialScore
        SortOption.TRAVEL -> score.travelScore
        SortOption.PHOTOGRAPHY -> score.photographyScore
    }
    val scoreLabel = sortOption.name.lowercase().replaceFirstChar { it.uppercase() }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (score.coverImage != null) {
                AsyncImage(
                    model = score.coverImage,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(score.memoryTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(score.memoryDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("$displayScore", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(scoreLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScoreDetail(scores: MemoryScores, onBack: () -> Unit) {
    var animationTrigger by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        animationTrigger = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intelligence Report") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                Text(scores.memoryTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(scores.memoryDate)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                ScoreBar("Overall Score", scores.overallScore, MaterialTheme.colorScheme.primary, animationTrigger, "The general health and impact rating of this memory.")
                ScoreBar("Importance", scores.importanceScore, MaterialTheme.colorScheme.secondary, animationTrigger, "How significant this moment is in the grand timeline of your life.")
                ScoreBar("Emotional Depth", scores.emotionalScore, MaterialTheme.colorScheme.tertiary, animationTrigger, "The intensity and variety of feelings associated with this memory.")
                ScoreBar("Happiness", scores.happinessScore, Color(0xFFFFB300), animationTrigger, "The pure joy and positivity radiating from this moment.")
                ScoreBar("Confidence", scores.confidenceScore, Color(0xFF4CAF50), animationTrigger, "The clarity and reliability of the data reconstructing this memory.")
                ScoreBar("Activity", scores.activityScore, Color(0xFFF44336), animationTrigger, "The physical and dynamic energy involved in this event.")
                ScoreBar("Social", scores.socialScore, Color(0xFF9C27B0), animationTrigger, "The level of interpersonal connection and shared experience.")
                ScoreBar("Travel", scores.travelScore, Color(0xFF00BCD4), animationTrigger, "How far you ventured from your usual routine or location.")
                ScoreBar("Photography", scores.photographyScore, Color(0xFFE91E63), animationTrigger, "The visual richness and amount of captured media.")
            }
        }
    }
}

@Composable
fun ScoreBar(label: String, score: Int, color: Color, animate: Boolean, explanation: String) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animate) score / 100f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$score", style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            Text(explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
