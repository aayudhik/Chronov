sed -i 's/import com.example.ui.screens.memories.MemoriesScreen/import com.example.ui.screens.memories.MemoriesScreen\nimport com.example.ui.screens.map.MapScreen/' app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt
sed -i '/composable("memories") {/!b;n;c\
            MemoriesScreen(\
                onNavigateToDetails = { memoryId ->\
                    navController.navigate("details/$memoryId")\
                },\
                onNavigateToMap = {\
                    navController.navigate("map")\
                }\
            )' app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt
sed -i '/composable("memories") {/i\
        composable("map") {\
            MapScreen(\
                onNavigateBack = { navController.popBackStack() },\
                onNavigateToDetails = { memoryId -> navController.navigate("details/$memoryId") }\
            )\
        }' app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt
