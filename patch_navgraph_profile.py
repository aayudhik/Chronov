import re

with open("app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt", "r") as f:
    content = f.read()

content = content.replace(
    'ProfileScreen(\n                onNavigateToAuth = {',
    'ProfileScreen(\n                onNavigateToDest = { dest -> navController.navigate(dest) },\n                onNavigateToAuth = {'
)

with open("app/src/main/java/com/example/ui/navigation/ChronovaNavGraph.kt", "w") as f:
    f.write(content)
