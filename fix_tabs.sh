sed -i '117,135c\
            if (uiState.activeTab == 0) {\
                AllMemoriesContent(uiState, viewModel, onNavigateToDetails)\
            } else if (uiState.activeTab == 1) {\
                CollectionsContent(uiState, viewModel, onNavigateToDetails)\
            } else {\
                LifeChaptersContent(uiState, viewModel, onNavigateToDetails)\
            }\
        }\
    }\
}\
' app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt
