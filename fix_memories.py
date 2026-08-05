with open("app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if "IconButton(onClick = { isSearchActive = true }) {" in line and "actions =" not in lines[i-1]:
        skip = True
    if skip and "colors = TopAppBarDefaults.topAppBarColors(" in line:
        skip = False
    
    if "colors = TopAppBarDefaults.topAppBarColors(" in line and "}" in lines[i-1] and ".fillMaxSize()" in lines[i+4]:
        # We know we need to insert the missing scaffold closure here
        pass # Wait, let's just do a clean replace

import re
content = "".join(lines)
fixed = re.sub(r'                        IconButton\(onClick = \{ isSearchActive = true \} \) \{\n                            Icon\(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary\)\n                        \}\n                    \},\n                    colors = TopAppBarDefaults.topAppBarColors\(\n                        containerColor = MaterialTheme.colorScheme.surface.copy\(alpha = 0.8f\)\n                    \)\n                \)\n            \}\n                .fillMaxSize\(\)\n                .padding\(padding\)',
r'''                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)''', content, flags=re.MULTILINE)

with open("app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt", "w") as f:
    f.write(fixed)
