import re

with open("app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt", "r") as f:
    content = f.read()

if "com.example.ui.screens.onthisday.OnThisDayScreen" not in content:
    content = content.replace("import com.example.ui.screens.privacy.PrivacyScreen", "import com.example.ui.screens.privacy.PrivacyScreen\nimport com.example.ui.screens.onthisday.OnThisDayScreen")
    
    content = content.replace('composable("privacy") {', """composable("on_this_day") {
            OnThisDayScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMemory = { id -> navController.navigate("memory_detail/$id") }
            )
        }
        composable("privacy") {""")

with open("app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt", "w") as f:
    f.write(content)

