import re

with open("app/src/main/java/com/example/ui/navigation/ChronovaScaffold.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.filled.Security", "import androidx.compose.material.icons.filled.Security\nimport androidx.compose.material.icons.filled.History")
content = content.replace('Triple("privacy", Icons.Default.Security, "Privacy")', 'Triple("on_this_day", Icons.Default.History, "This Day"),\n                        Triple("privacy", Icons.Default.Security, "Privacy")')

with open("app/src/main/java/com/example/ui/navigation/ChronovaScaffold.kt", "w") as f:
    f.write(content)
