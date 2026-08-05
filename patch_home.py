import re

with open("app/src/main/java/com/example/ui/screens/home/HomeTimelineScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports = """import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.ui.screens.onthisday.OnThisDayViewModel
import com.example.ui.screens.onthisday.OnThisDayViewModelFactory
import androidx.compose.ui.text.style.TextAlign
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
"""
content = content.replace("import com.example.ui.components.viewModelFactory", "import com.example.ui.components.viewModelFactory\n" + imports)

# Add viewmodel init
vm_init = """    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { HomeViewModel(appContainer.memoryRepository) }
    )
    val onThisDayViewModel: OnThisDayViewModel = viewModel(
        factory = OnThisDayViewModelFactory(appContainer.onThisDayRepository)
    )
    val onThisDayMemories by onThisDayViewModel.memories.collectAsState()
    val onThisDaySettings by onThisDayViewModel.settings.collectAsState()
"""
content = content.replace("    val appContainer = (context.applicationContext as ChronovaApplication).container\n    val viewModel: HomeViewModel = viewModel(\n        factory = viewModelFactory { HomeViewModel(appContainer.memoryRepository) }\n    )", vm_init)


# Find the LazyColumn items section
lazy_col_search = """                if (draftMemories.isNotEmpty()) {
                    item {
                        DraftsBanner(
                            count = draftMemories.size,
                            onClick = onNavigateToReviewDrafts
                        )
                    }
                }"""
                
lazy_col_replace = lazy_col_search + """
                
                if (onThisDaySettings.isEnabled && onThisDayMemories.isNotEmpty() && !isSearchActive) {
                    item {
                        OnThisDayCarousel(
                            memories = onThisDayMemories,
                            onClick = { /* Could navigate to specific memory or OnThisDayScreen */ }
                        )
                    }
                }
"""
content = content.replace(lazy_col_search, lazy_col_replace)

carousel_composable = """
@Composable
fun OnThisDayCarousel(
    memories: List<MemoryWithMedia>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Memory This Day", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("You made beautiful memories on this day.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(memories) { memoryWithMedia ->
                OnThisDayMiniCard(
                    memoryWithMedia = memoryWithMedia,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
fun OnThisDayMiniCard(
    memoryWithMedia: MemoryWithMedia,
    onClick: () -> Unit
) {
    val memory = memoryWithMedia.memory
    val coverImage = memoryWithMedia.media.firstOrNull { it.type == "image" }?.url
    val yearStr = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(memory.timestamp))
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            if (coverImage != null) {
                Image(
                    painter = rememberAsyncImagePainter(coverImage),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(yearStr, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    if (memory.score > 0) {
                        Text("Score ${memory.score}", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(memory.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (memory.locationName.isNotBlank()) {
                    Text(memory.locationName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
"""

content += "\n" + carousel_composable

with open("app/src/main/java/com/example/ui/screens/home/HomeTimelineScreen.kt", "w") as f:
    f.write(content)
