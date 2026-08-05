import re

with open("app/src/main/java/com/example/ui/screens/stories/StoryScreen.kt", "r") as f:
    content = f.read()

# Replace Toasts with actual function calls
content = content.replace('Toast.makeText(context, "Image export is processing...", Toast.LENGTH_SHORT).show()', 'ExportUtils.exportAsImage(context, story)')
content = content.replace('Toast.makeText(context, "PDF export is processing...", Toast.LENGTH_SHORT).show()', 'ExportUtils.exportAsPdf(context, story)')
content = content.replace('Toast.makeText(context, "Share Card generated!", Toast.LENGTH_SHORT).show()', 'ExportUtils.exportAsImage(context, story)')

with open("app/src/main/java/com/example/ui/screens/stories/StoryScreen.kt", "w") as f:
    f.write(content)
