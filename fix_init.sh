sed -i 's/memoryRepository.lifeChapters.collect { dbChapters ->/val dbChapters = kotlinx.coroutines.flow.first(memoryRepository.lifeChapters)/' app/src/main/java/com/example/ui/screens/memories/MemoriesViewModel.kt
sed -i '/if (dbChapters.isEmpty()) {/!b;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n;n' app/src/main/java/com/example/ui/screens/memories/MemoriesViewModel.kt
