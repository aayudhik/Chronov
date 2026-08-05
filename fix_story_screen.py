import re

with open("app/src/main/java/com/example/ui/screens/stories/StoryScreen.kt", "r") as f:
    content = f.read()

# Replace the entire StoryDetailScreen function
start_idx = content.find("@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun StoryDetailScreen")
end_idx = content.find("private fun exportAsMarkdown")

new_func = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailScreen(story: Story, onBack: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(story.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showExportMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Export")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export as Markdown") },
                                onClick = { 
                                    showExportMenu = false
                                    exportAsMarkdown(context, story) 
                                },
                                leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as Image") },
                                onClick = { 
                                    showExportMenu = false
                                    ExportUtils.exportAsImage(context, story)
                                },
                                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as PDF") },
                                onClick = { 
                                    showExportMenu = false
                                    ExportUtils.exportAsPdf(context, story)
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Card") },
                                onClick = { 
                                    showExportMenu = false
                                    ExportUtils.exportAsImage(context, story)
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            if (story.coverImageUrl.isNotBlank()) {
                AsyncImage(
                    model = story.coverImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(story.type, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(story.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(story.content, style = MaterialTheme.typography.bodyLarge, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f)
            }
        }
    }
}

"""

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + new_func + content[end_idx:]

with open("app/src/main/java/com/example/ui/screens/stories/StoryScreen.kt", "w") as f:
    f.write(content)
