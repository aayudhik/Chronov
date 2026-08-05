package com.example.ui.screens.privacy

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ChronovaApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen() {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as ChronovaApplication).container
    val privacyManager = appContainer.privacyManager
    val memoryRepository = appContainer.memoryRepository

    val isPinEnabled by privacyManager.isPinEnabled.collectAsState()
    val isBiometricEnabled by privacyManager.isBiometricEnabled.collectAsState()
    val isBackupEncrypted by privacyManager.isBackupEncrypted.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    
    var showExportDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Authentication", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Dialpad, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("PIN Lock", fontWeight = FontWeight.Bold)
                                Text("Require PIN to open Chronova", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = isPinEnabled, onCheckedChange = { if (it) showPinDialog = true else privacyManager.removePin() })
                        }
                        
                        if (isPinEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Biometric Unlock", fontWeight = FontWeight.Bold)
                                    Text("Use fingerprint or face unlock", style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(checked = isBiometricEnabled, onCheckedChange = { privacyManager.setBiometricEnabled(it) })
                            }
                        }
                    }
                }
            }
            
            item {
                Text("Data & Backup", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Encrypted Room Database") },
                            supportingContent = { Text("Your local memories are encrypted with SQLCipher using a 256-bit AES-GCM key.") },
                            leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingContent = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                        Divider()
                        ListItem(
                            headlineContent = { Text("Automatic Encrypted Backups") },
                            supportingContent = { Text("Enable secure cloud backups.") },
                            leadingContent = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
                            trailingContent = { Switch(checked = isBackupEncrypted, onCheckedChange = { privacyManager.setBackupEncrypted(it) }) }
                        )
                        Divider()
                        ListItem(
                            modifier = Modifier.fillMaxWidth(),
                            headlineContent = { Text("Restore Backups") },
                            leadingContent = { Icon(Icons.Default.Restore, contentDescription = null) }
                        )
                        Divider()
                        ListItem(
                            modifier = Modifier.fillMaxWidth(),
                            headlineContent = { Text("Selective Backup") },
                            leadingContent = { Icon(Icons.Default.FilterList, contentDescription = null) }
                        )
                    }
                }
            }
            
            item {
                Text("Data Control", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column {
                        ListItem(
                            modifier = Modifier.fillMaxWidth(),
                            headlineContent = { Text("Export all memories") },
                            leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                            trailingContent = {
                                Button(onClick = { showExportDialog = true }) {
                                    Text("Export")
                                }
                            }
                        )
                        Divider()
                        ListItem(
                            modifier = Modifier.fillMaxWidth(),
                            headlineContent = { Text("Delete all memories", color = MaterialTheme.colorScheme.error) },
                            leadingContent = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            trailingContent = {
                                Button(
                                    onClick = { showDeleteDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Delete")
                                }
                            }
                        )
                    }
                }
            }
            
            item {
                Text("Architecture & Transparency", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Offline-First Architecture") },
                            supportingContent = { Text("Chronova operates fully offline by default. No data leaves your device unless you enable Sync.") },
                            leadingContent = { Icon(Icons.Default.WifiOff, contentDescription = null) }
                        )
                        Divider()
                        ListItem(
                            headlineContent = { Text("Permission Manager") },
                            supportingContent = { Text("View transparent explanations for every permission requested by the app (Camera, Location, Audio).") },
                            leadingContent = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) }
                        )
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set PIN") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("Enter 4-6 digit PIN") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = { 
                    if (pinInput.isNotBlank()) {
                        privacyManager.setPin(pinInput)
                        showPinDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Memories") },
            text = { Text("All your memories will be compiled into a secure ZIP archive.") },
            confirmButton = {
                Button(onClick = { 
                    showExportDialog = false
                    Toast.makeText(context, "Exporting memories...", Toast.LENGTH_SHORT).show()
                }) { Text("Export") }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete All Data") },
            text = { Text("Are you sure? This action cannot be undone and will permanently wipe your encrypted database.") },
            confirmButton = {
                Button(
                    onClick = { 
                        showDeleteDialog = false
                        Toast.makeText(context, "Deleting all memories...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
