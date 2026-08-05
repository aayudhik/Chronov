sed -i 's/import com.example.ui.screens.privacy.LockScreen//g' app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt
sed -i '/import com.example.ui.screens.map.MapScreen/a\
import com.example.ui.screens.privacy.LockScreen\
import com.example.ui.screens.privacy.PrivacyScreen' app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt

sed -i '/composable("home") {/i\
        composable("lock_screen") {\
            LockScreen(onUnlocked = {\
                navController.navigate("home") {\
                    popUpTo("lock_screen") { inclusive = true }\
                }\
            })\
        }\
        composable("privacy") {\
            PrivacyScreen()\
        }' app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt
