cat << 'INNER_EOF' > app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt.patch
--- app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt
+++ app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt
@@ -93,17 +93,12 @@
                             Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                         }
                     },
-                        IconButton(onClick = { isSearchActive = true }) {
-                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
-                        }
-                    },
                     colors = TopAppBarDefaults.topAppBarColors(
                         containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                     )
                 )
             }
-                .fillMaxSize()
-                .padding(padding)
+        }
+    ) { padding ->
+        Column(
+            modifier = Modifier
+                .fillMaxSize()
+                .padding(padding)
         ) {
             TabRow(selectedTabIndex = uiState.activeTab) {
INNER_EOF
patch app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt < app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt.patch
