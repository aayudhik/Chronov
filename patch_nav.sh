sed -i 's/import com.example.ui.screens.memories.MemoriesScreen/import com.example.ui.screens.memories.MemoriesScreen\nimport com.example.ui.screens.stories.StoryScreen/' app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt
sed -i '/composable("insights") {/i\
        composable("stories") {\
            StoryScreen(\
                onNavigateBack = { navController.popBackStack() }\
            )\
        }' app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt
