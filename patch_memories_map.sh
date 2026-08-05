sed -i 's/fun MemoriesScreen(onNavigateToDetails: (Long) -> Unit)/fun MemoriesScreen(onNavigateToDetails: (Long) -> Unit, onNavigateToMap: () -> Unit = {})/' app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt
sed -i '/title = { Text("Memories"/!b;n;c\
                    actions = {\
                        IconButton(onClick = onNavigateToMap) {\
                            Icon(androidx.compose.material.icons.Icons.Default.Map, contentDescription = "Map View", tint = MaterialTheme.colorScheme.primary)\
                        }\
                        IconButton(onClick = { isSearchActive = true }) {\
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)\
                        }\
                    },' app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt
sed -i 's/import androidx.compose.material.icons.filled.PushPin/import androidx.compose.material.icons.filled.PushPin\nimport androidx.compose.material.icons.filled.Map/' app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt
