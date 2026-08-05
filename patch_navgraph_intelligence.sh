sed -i 's/import com.example.ui.screens.stories.StoryScreen/import com.example.ui.screens.stories.StoryScreen\nimport com.example.ui.screens.intelligence.IntelligenceScreen/' app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt
sed -i '/composable("insights") {/i\
        composable("intelligence") {\
            IntelligenceScreen()\
        }' app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt
