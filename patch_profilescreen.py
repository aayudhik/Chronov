import re

with open("app/src/main/java/com/example/ui/screens/profile/ProfileScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    'fun ProfileScreen(onNavigateToAuth: () -> Unit) {',
    'fun ProfileScreen(onNavigateToDest: (String) -> Unit = {}, onNavigateToAuth: () -> Unit) {'
)

# I also need to add the imports for Icons
icons = """import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
"""

content = content.replace("import androidx.compose.material.icons.filled.ContentCopy", icons + "import androidx.compose.material.icons.filled.ContentCopy")


# Add the ListItems for the other destinations under the Settings list item.
# Find: item {\n                Text(\n                    "Settings",
settings_header = """            item {
                Text(
                    "Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Stories") },
                    supportingContent = { Text("AI generated memory collections") },
                    leadingContent = { Icon(Icons.Default.Book, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToDest("stories") }
                )
                ListItem(
                    headlineContent = { Text("Insights") },
                    supportingContent = { Text("Analytics about your memories") },
                    leadingContent = { Icon(Icons.Default.Insights, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToDest("insights") }
                )
                ListItem(
                    headlineContent = { Text("On This Day") },
                    supportingContent = { Text("Relive memories from the past") },
                    leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToDest("on_this_day") }
                )
            }
            item {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }"""

content = re.sub(
    r'item \{\s+Text\(\s+"Settings",\s+style = MaterialTheme\.typography\.titleMedium,\s+fontWeight = FontWeight\.Bold,\s+modifier = Modifier\.padding\(start = 16\.dp, top = 16\.dp, bottom = 8\.dp\)\s+\)\s+\}',
    settings_header,
    content,
    count=1
)

# Change Privacy ListItem to use navigation instead of dialog if there's a privacy screen
# Wait, let's check if the privacy screen is fully implemented. The user can use the Privacy dialog too.
# The Bottom Bar used to have 'privacy' which navigates to 'privacy' route. So I'll change the Privacy ListItem to navigate to 'privacy' screen.
privacy_item_old = """ListItem(
                    headlineContent = { Text("Privacy") },
                    supportingContent = { Text("Local encryption, location privacy, cache") },
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showPrivacyDialog = true
                    }
                )"""

privacy_item_new = """ListItem(
                    headlineContent = { Text("Privacy") },
                    supportingContent = { Text("Manage encryption and privacy settings") },
                    leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onNavigateToDest("privacy")
                    }
                )"""
content = content.replace(privacy_item_old, privacy_item_new)


with open("app/src/main/java/com/example/ui/screens/profile/ProfileScreen.kt", "w") as f:
    f.write(content)
