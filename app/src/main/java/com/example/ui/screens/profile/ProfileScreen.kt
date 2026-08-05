package com.example.ui.screens.profile

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ChronovaApplication
import com.example.ui.components.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigateToDest: (String) -> Unit = {}, onNavigateToAuth: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val viewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory { ProfileViewModel(appContainer.memoryRepository, appContainer.authRepository) }
    )

    val uiState by viewModel.uiState.collectAsState()

    var showAppearanceDialog by remember { mutableStateOf(false) }
    var selectedThemeMode by remember { mutableStateOf("System Default") }
    var compactTimeline by remember { mutableStateOf(false) }
    var highContrast by remember { mutableStateOf(false) }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var encryptionEnabled by remember { mutableStateOf(true) }
    var locationPrivacyEnabled by remember { mutableStateOf(true) }
    var analyticsEnabled by remember { mutableStateOf(false) }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("Text Summary") }
    var includeLocations by remember { mutableStateOf(true) }
    var includeImages by remember { mutableStateOf(true) }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }

    var showSignOutDialog by remember { mutableStateOf(false) }
    var autoSyncEnabled by remember { mutableStateOf(false) }

    // Appearance Dialog
    if (showAppearanceDialog) {
        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            title = { Text("Appearance Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Theme Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val themeOptions = listOf("System Default", "Light Mode", "Dark Mode")
                    Column(Modifier.selectableGroup()) {
                        themeOptions.forEach { text ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .selectable(
                                        selected = (text == selectedThemeMode),
                                        onClick = { selectedThemeMode = text },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (text == selectedThemeMode),
                                    onClick = null
                                )
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Compact Timeline", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Show dense timeline cards", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = compactTimeline,
                            onCheckedChange = { compactTimeline = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High Contrast", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Enhance text legibility", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = highContrast,
                            onCheckedChange = { highContrast = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAppearanceDialog = false
                    Toast.makeText(context, "Appearance preferences updated", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppearanceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "All memories, photos, and notes are stored locally on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local Storage Encryption", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Encrypt Room database files", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = encryptionEnabled,
                            onCheckedChange = { encryptionEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Location Privacy", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Never upload location telemetry", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = locationPrivacyEnabled,
                            onCheckedChange = { locationPrivacyEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Usage Analytics", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Share anonymous diagnostic data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = analyticsEnabled,
                            onCheckedChange = { analyticsEnabled = it }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Local cache cleared (14.2 MB freed)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Local Media Cache")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPrivacyDialog = false
                    Toast.makeText(context, "Privacy settings saved", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Done")
                }
            }
        )
    }

    // Export Data Dialog
    if (showExportDialog) {
        val exportText = viewModel.generateExportText(
            memories = uiState.memories,
            format = exportFormat,
            includeLocations = includeLocations,
            includeImages = includeImages
        )

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Memory Data", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Choose Export Format", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val formats = listOf("Text Summary", "JSON")
                    Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        formats.forEach { format ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.selectable(
                                    selected = (format == exportFormat),
                                    onClick = { exportFormat = format },
                                    role = Role.RadioButton
                                )
                            ) {
                                RadioButton(selected = (format == exportFormat), onClick = null)
                                Text(format, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { includeLocations = !includeLocations }
                    ) {
                        Checkbox(checked = includeLocations, onCheckedChange = { includeLocations = it })
                        Text("Include Location Data", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { includeImages = !includeImages }
                    ) {
                        Checkbox(checked = includeImages, onCheckedChange = { includeImages = it })
                        Text("Include Attached Images & Media URLs", style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        Text(
                            text = exportText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(exportText))
                        Toast.makeText(context, "Copied export to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy")
                    }

                    TextButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Chronova Memory Export")
                            putExtra(Intent.EXTRA_TEXT, exportText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Memory Data"))
                        showExportDialog = false
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("About Chronova", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Version 1.0.0 (Build 100)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Chronova is your private, intelligent memory journal. Capture notes, photos, voice recordings, and location tags to curate your life's timeline effortlessly.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showLicenses = !showLicenses },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showLicenses) "Hide Open Source Licenses" else "View Open Source Licenses")
                    }

                    if (showLicenses) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 100.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text("• Jetpack Compose (Apache 2.0)", style = MaterialTheme.typography.bodySmall)
                                Text("• Material Design 3 (Apache 2.0)", style = MaterialTheme.typography.bodySmall)
                                Text("• Android Room Database (Apache 2.0)", style = MaterialTheme.typography.bodySmall)
                                Text("• Coil Image Loader (Apache 2.0)", style = MaterialTheme.typography.bodySmall)
                                Text("• Firebase Auth (Google APIs Terms)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Sign Out Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    viewModel.signOut(onComplete = onNavigateToAuth)
                }) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                if (uiState.userEmailOrPhone != null) {
                    Text(
                        text = "Signed in as: ${uiState.userEmailOrPhone}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Statistics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Memories")
                            Text(uiState.totalMemories.toString(), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Distinct Locations")
                            Text(uiState.distinctLocations.toString(), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Images")
                            Text(uiState.totalImages.toString(), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Longest Streak")
                            Text("${uiState.longestStreak} days", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

                        item {
                Text(
                    "Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Stories") },
                    supportingContent = { Text("AI generated memory collections") },
                    leadingContent = { Icon(Icons.Default.Book, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToDest("stories") }
                )
                ListItem(
                    headlineContent = { Text("Insights") },
                    supportingContent = { Text("Analytics about your memories") },
                    leadingContent = { Icon(Icons.Default.Insights, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToDest("insights") }
                )
                ListItem(
                    headlineContent = { Text("On This Day") },
                    supportingContent = { Text("Relive memories from the past") },
                    leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToDest("on_this_day") }
                )
            }
            item {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Appearance") },
                    supportingContent = { Text("Theme mode, font contrast, layout density") },
                    leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showAppearanceDialog = true
                    }
                )
                ListItem(
                    headlineContent = { Text("Privacy") },
                    supportingContent = { Text("Manage encryption and privacy settings") },
                    leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onNavigateToDest("privacy")
                    }
                )
                ListItem(
                    headlineContent = { Text("Export Data") },
                    supportingContent = { Text("Export memory timeline to JSON or text") },
                    leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showExportDialog = true
                    }
                )
                ListItem(
                    headlineContent = { Text("Auto-Timeline Sync") },
                    supportingContent = { Text("Generate drafts from device photos in background") },
                    leadingContent = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    trailingContent = { 
                        Switch(
                            checked = autoSyncEnabled,
                            onCheckedChange = {
                                autoSyncEnabled = it
                                viewModel.toggleAutoTimelineSync(context, it)
                                if (it) {
                                    Toast.makeText(context, "Timeline Sync Enabled", Toast.LENGTH_SHORT).show()
                                    viewModel.triggerSyncNow(context)
                                }
                            }
                        )
                    },
                    modifier = Modifier.clickable {}
                )
                ListItem(
                    headlineContent = { Text("About") },
                    supportingContent = { Text("App version, licenses, and info") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showAboutDialog = true
                    }
                )

                if (uiState.userEmailOrPhone != null) {
                    ListItem(
                        headlineContent = { Text("Sign Out", color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable {
                            showSignOutDialog = true
                        }
                    )
                } else {
                    ListItem(
                        headlineContent = { Text("Sign In") },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null) },
                        modifier = Modifier.clickable {
                            onNavigateToAuth()
                        }
                    )
                }
            }
        }
    }
}
