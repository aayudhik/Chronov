import re

with open("app/src/main/java/com/example/di/AppContainer.kt", "r") as f:
    content = f.read()

content = content.replace("val memoryIntelligenceService by lazy { com.example.domain.MemoryIntelligenceService() }",
"val memoryIntelligenceService by lazy { com.example.domain.MemoryIntelligenceService() }\n    val onThisDayRepository by lazy { com.example.data.repository.OnThisDayRepository(database.onThisDayDao(), aiMemoryEngine) }")

with open("app/src/main/java/com/example/di/AppContainer.kt", "w") as f:
    f.write(content)
