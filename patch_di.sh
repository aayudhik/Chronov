sed -i '/val aiMemoryEngine/a\
    val storyGenerationService by lazy { com.example.domain.StoryGenerationService() }' app/src/main/java/com/example/di/AppContainer.kt
