import re

with open("app/src/main/java/com/example/ui/navigation/ChronovaScaffold.kt", "r") as f:
    content = f.read()

tabs_old = """                    val tabs = listOf(
                        Triple("home", Icons.Default.Timeline, "Timeline"),
                        Triple("memories", Icons.Default.PhotoLibrary, "Memories"),
                        Triple("search", Icons.Default.AutoAwesome, "AI Search"),
                        Triple("stories", Icons.Default.Book, "Stories"),
                        Triple("insights", Icons.Default.Insights, "Insights"),
                        Triple("intelligence", Icons.Default.Psychology, "Intelligence"),
                        Triple("profile", Icons.Default.Person, "Profile"),
                        Triple("on_this_day", Icons.Default.History, "This Day"),
                        Triple("privacy", Icons.Default.Security, "Privacy")
                    )"""

tabs_new = """                    val tabs = listOf(
                        Triple("home", Icons.Default.Timeline, "Timeline"),
                        Triple("memories", Icons.Default.PhotoLibrary, "Memories"),
                        Triple("search", Icons.Default.AutoAwesome, "AI Search"),
                        Triple("intelligence", Icons.Default.Psychology, "Intelligence"),
                        Triple("profile", Icons.Default.Person, "Profile")
                    )"""

content = content.replace(tabs_old, tabs_new)

with open("app/src/main/java/com/example/ui/navigation/ChronovaScaffold.kt", "w") as f:
    f.write(content)
