with open("app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt", "r") as f:
    content = f.read()

import re

# Find Scaffold(
start_scaffold = content.find("Scaffold(")
if start_scaffold != -1:
    # Find TabRow(
    start_tabrow = content.find("TabRow(selectedTabIndex = uiState.activeTab) {")
    if start_tabrow != -1:
        new_scaffold = """Scaffold(
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
            """
        
        content = content[:start_scaffold] + new_scaffold + content[start_tabrow:]
        
        with open("app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt", "w") as f:
            f.write(content)
