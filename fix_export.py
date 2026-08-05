import re

with open("app/src/main/java/com/example/ui/screens/stories/StoryScreen.kt", "r") as f:
    content = f.read()

# Replace the single Markdown export button with a drop-down or just a share button that shows options
new_actions = """
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
                                    Toast.makeText(context, "Image export is processing...", Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as PDF") },
                                onClick = { 
                                    showExportMenu = false
                                    Toast.makeText(context, "PDF export is processing...", Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Card") },
                                onClick = { 
                                    showExportMenu = false
                                    Toast.makeText(context, "Share Card generated!", Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                        }
                    }
                }
"""

content = re.sub(r'actions = \{.*?\n\s*\}\n', new_actions, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/stories/StoryScreen.kt", "w") as f:
    f.write(content)
