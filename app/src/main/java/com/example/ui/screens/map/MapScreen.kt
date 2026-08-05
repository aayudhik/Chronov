package com.example.ui.screens.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ChronovaApplication
import com.example.data.local.MemoryWithMedia
import com.example.ui.components.viewModelFactory
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Long) -> Unit
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: MapViewModel = viewModel(
        factory = viewModelFactory { MapViewModel(appContainer.memoryRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var selectedCluster by remember { mutableStateOf<MapCluster?>(null) }
    val scope = rememberCoroutineScope()

    // Initialize osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("World Map") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Map View
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(3.0)
                        controller.setCenter(GeoPoint(20.0, 0.0))
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()
                    
                    // Draw Travel Paths
                    if (uiState.filteredMemories.size > 1) {
                        val sorted = uiState.filteredMemories.sortedBy { it.memory.timestamp }
                        val points = sorted.map { GeoPoint(it.memory.latitude!!, it.memory.longitude!!) }
                        val polyline = Polyline().apply {
                            setPoints(points)
                            outlinePaint.color = android.graphics.Color.BLUE
                            outlinePaint.strokeWidth = 5f
                        }
                        mapView.overlays.add(polyline)
                    }

                    // Draw Clusters as Markers
                    uiState.clusters.forEach { cluster ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(cluster.latitude, cluster.longitude)
                            title = "${cluster.memories.size} Memories"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                selectedCluster = cluster
                                scope.launch { sheetState.show() }
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            // Statistics Overlay
            Card(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).padding(bottom = 32.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Travel Stats", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Places Visited: ${uiState.filteredMemories.map { it.memory.locationName }.distinct().size}", style = MaterialTheme.typography.bodyMedium)
                    Text("Total Memories: ${uiState.filteredMemories.size}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Filters
            MapFilters(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    if (selectedCluster != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedCluster = null },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(
                    text = "${selectedCluster?.memories?.size} Memories Here",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn {
                    items(selectedCluster?.memories ?: emptyList()) { memoryWithMedia ->
                        MemoryListItem(memoryWithMedia) {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                selectedCluster = null
                                onNavigateToDetails(memoryWithMedia.memory.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapFilters(uiState: MapUiState, viewModel: MapViewModel, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterDropdown(
                label = uiState.selectedYear ?: "Year",
                options = listOf("All") + uiState.availableYears,
                onSelect = { viewModel.setYear(if (it == "All") null else it) }
            )
        }
        item {
            FilterDropdown(
                label = uiState.selectedPerson ?: "Person",
                options = listOf("All") + uiState.availablePersons,
                onSelect = { viewModel.setPerson(if (it == "All") null else it) }
            )
        }
        item {
            FilterDropdown(
                label = uiState.selectedCollection ?: "Collection",
                options = listOf("All") + uiState.availableCollections,
                onSelect = { viewModel.setCollection(if (it == "All") null else it) }
            )
        }
        item {
            FilterDropdown(
                label = uiState.selectedMood ?: "Mood",
                options = listOf("All") + uiState.availableMoods,
                onSelect = { viewModel.setMood(if (it == "All") null else it) }
            )
        }
        item {
            FilterDropdown(
                label = uiState.selectedActivity ?: "Activity",
                options = listOf("All") + uiState.availableActivities,
                onSelect = { viewModel.setActivity(if (it == "All") null else it) }
            )
        }
    }
}

@Composable
fun FilterDropdown(label: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = label != "Year" && label != "Person" && label != "Collection" && label != "Mood" && label != "Activity",
            onClick = { expanded = true },
            label = { Text(label) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MemoryListItem(memoryWithMedia: MemoryWithMedia, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imgUrl = memoryWithMedia.media.firstOrNull { it.type == "image" }?.url
        if (imgUrl != null) {
            AsyncImage(
                model = imgUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Map, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(memoryWithMedia.memory.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(memoryWithMedia.memory.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(memoryWithMedia.memory.locationName, style = MaterialTheme.typography.bodySmall)
        }
    }
}
