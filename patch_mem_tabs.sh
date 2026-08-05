sed -i '/text = { Text("Smart Collections") }/!b;n;n;n;n;n;n;n;n;a\
                Tab(\
                    selected = uiState.activeTab == 2,\
                    onClick = { viewModel.setActiveTab(2) },\
                    text = { Text("Life Chapters") }\
                )\
            }\
\
            if (uiState.activeTab == 0) {\
                AllMemoriesContent(uiState, viewModel, onNavigateToDetails)\
            } else if (uiState.activeTab == 1) {\
                CollectionsContent(uiState, viewModel, onNavigateToDetails)\
            } else {\
                LifeChaptersContent(uiState, viewModel, onNavigateToDetails)\
            }' app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt
sed -i '114,120d' app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt
