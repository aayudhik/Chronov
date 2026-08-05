import re

with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    content = f.read()

# Fix the detached annotation
content = re.sub(r'@Entity\(tableName = "life_chapters"\)\s*@Entity\(tableName = "stories"\)\s*data class Story\((.*?)\)\s*data class LifeChapter',
r'''@Entity(tableName = "stories")
data class Story(\1)

@Entity(tableName = "life_chapters")
data class LifeChapter''', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/data/local/Entities.kt", "w") as f:
    f.write(content)
