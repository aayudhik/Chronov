sed -i '/val storyGenerationService/a\
    val memoryIntelligenceService by lazy { com.example.domain.MemoryIntelligenceService() }' app/src/main/java/com/example/di/AppContainer.kt
