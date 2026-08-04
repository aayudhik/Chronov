package com.example.ui.screens.insights

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.ChronovaApplication
import com.example.ui.components.viewModelFactory
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: InsightsViewModel = viewModel(
        factory = viewModelFactory { InsightsViewModel(appContainer.memoryRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Chronova", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
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
                    selected = false,
                    onClick = onNavigateToSearch,
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Search") },
                    label = { Text("AI Search") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
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
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Your Journey Insights", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("A deep dive into your memories, powered by AI.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    StreakCard(streak = uiState.memoryStreak)
                }

                item {
                    GlobalFootprintCard(countries = uiState.countriesVisited, cities = uiState.citiesVisited, distance = uiState.distanceTraveled)
                }

                item {
                    MediaCapturedCard(totalMedia = uiState.photosCaptured + uiState.videosCaptured + uiState.voiceNotesCaptured, recentMedia = uiState.recentMediaCount)
                }

                item {
                    ActivityGraphCard(monthlyActivity = uiState.monthlyActivity)
                }

                item {
                    FavoriteLocationsCard(locations = uiState.favoriteLocations)
                }

                item {
                    AIMonthlyRecapCard()
                }

                item {
                    MemorySentimentCard(fractions = uiState.sentimentFractions, topSentiment = uiState.topSentiment)
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun StreakCard(streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = borderStroke(MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MEMORY STREAK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$streak", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Days", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }
    }
}

@Composable
fun GlobalFootprintCard(countries: Int, cities: Int, distance: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = borderStroke(MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuB0RF0IIzSyVBStd6E1zOjT2ZXQ39ece0PDBoyXJWoVmv80_h6Hm2mlS7TlRzPFoGeKjP17AiUg4dT48vp4LN18KE_edZChk8Ce57UZicBQHdoJ9DnQaM_xN_TJFDkACX3P0LPrnmCl9_jcu9b4aLOjIfm6RZS3m9eV8DZeMSFYhP4v2RPV8Z9tnx9ak-UVPgxkqgolBh4GvVYd2NBCu8gwuJ-faZHkArEXlQ9C3Jc-kB5UhF7itxqg"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                alpha = 0.4f
            )
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GLOBAL FOOTPRINT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$countries", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Countries", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    Column {
                        Text("CITIES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$cities", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("DISTANCE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.Bottom) {
                            val distK = distance / 1000
                            Text("${distK}k", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(" mi", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaCapturedCard(totalMedia: Int, recentMedia: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = borderStroke(MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text("MEDIA CAPTURED", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(if (totalMedia > 1000) "${String.format("%.1f", totalMedia / 1000.0)}k" else "$totalMedia", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onSurface)
            Text("Photos, Videos & Audios", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("This Month", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+$recentMedia", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable
fun AIMonthlyRecapCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = borderStroke(MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuCfngrcaTzz_6gDxMxrMVlmSQH8j3sEf7T-TJ0zHRA6eU4BWUFr0XVRz27aaPI6tEIT18bXxI7rCgWfEtoo82993l_zCkUgEAsT4sFfAkGgOI2dF7GWn_KNvvMNmeB_0VcnF_z85rzVzwaciCp6FJQjcO0q3nvxE8_saLDDovxsYvHl2i5rpzqAF7kb9Xoy7519AQ5EZuOI9CR1t8Sc3csF57v5iBBMFNlAKoQMhbIU_Bi5VtpOPRzP"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI MONTHLY RECAP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("September's Serenity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This month was characterized by coastal escapes and quiet urban exploration. You spent 60% more time near water compared to August. Your most photographed subject was architecture, specifically mid-century modern facades.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("#Coastal", "#Architecture", "#Relaxation").forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            border = borderStroke(MaterialTheme.colorScheme.outline)
                        ) {
                            Text(tag, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemorySentimentCard(fractions: Map<String, Float>, topSentiment: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = borderStroke(MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MEMORY SENTIMENT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(16.dp)) {
                    Text("Top: $topSentiment", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val joy = fractions["Joy"] ?: 0.1f
                val calm = fractions["Calm"] ?: 0.1f
                val awe = fractions["Awe"] ?: 0.1f
                val nostalgia = fractions["Nostalgia"] ?: 0.1f

                BarChartBar(label = "Joy", fraction = joy, color = MaterialTheme.colorScheme.primary)
                BarChartBar(label = "Calm", fraction = calm, color = MaterialTheme.colorScheme.secondary)
                BarChartBar(label = "Awe", fraction = awe, color = MaterialTheme.colorScheme.tertiaryContainer)
                BarChartBar(label = "Nostalgia", fraction = nostalgia, color = MaterialTheme.colorScheme.secondaryContainer)
            }
        }
    }
}

@Composable
fun BarChartBar(label: String, fraction: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction)
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun borderStroke(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.dp, color)
}

@Composable
fun ActivityGraphCard(monthlyActivity: List<Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = borderStroke(MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text("MONTHLY ACTIVITY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                monthlyActivity.forEachIndexed { index, fraction ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(100.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fraction)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(months[index].take(1), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteLocationsCard(locations: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = borderStroke(MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("FAVORITE LOCATIONS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (locations.isEmpty()) {
                Text("Not enough data to show favorite locations.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                locations.forEachIndexed { index, location ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("${index + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(location, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
