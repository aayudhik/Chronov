package com.example.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.ChronovaApplication
import com.example.data.local.Memory
import com.example.data.local.MemoryWithMedia
import com.example.data.repository.MemoryRepository
import com.example.ui.components.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReviewDraftsViewModel(private val repository: MemoryRepository) : ViewModel() {
    val draftMemories: StateFlow<List<MemoryWithMedia>> = repository.draftMemories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun acceptDraft(memory: Memory) {
        viewModelScope.launch {
            repository.updateMemory(memory.copy(isDraft = false))
        }
    }

    fun rejectDraft(memory: Memory) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDraftsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: ReviewDraftsViewModel = viewModel(
        factory = viewModelFactory { ReviewDraftsViewModel(appContainer.memoryRepository) }
    )
    val drafts by viewModel.draftMemories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Timeline") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        if (drafts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No drafts to review.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(drafts) { draft ->
                    DraftCard(
                        draft = draft,
                        onAccept = { viewModel.acceptDraft(draft.memory) },
                        onReject = { viewModel.rejectDraft(draft.memory) }
                    )
                }
            }
        }
    }
}

@Composable
fun DraftCard(draft: MemoryWithMedia, onAccept: () -> Unit, onReject: () -> Unit) {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(draft.memory.source, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("${draft.memory.confidenceScore}% match", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(draft.memory.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(formatter.format(Date(draft.memory.timestamp)), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(draft.memory.notes, style = MaterialTheme.typography.bodyMedium)
            
            val images = draft.media.filter { it.type == "image" }
            if (images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    images.take(3).forEach { img ->
                        Image(
                            painter = rememberAsyncImagePainter(img.url),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onReject) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onAccept) {
                    Text("Save to Timeline")
                }
            }
        }
    }
}
