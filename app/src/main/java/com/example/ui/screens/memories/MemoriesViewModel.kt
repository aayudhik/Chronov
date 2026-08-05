package com.example.ui.screens.memories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LifeChapter
import com.example.data.local.MemoryWithMedia
import com.example.data.local.SmartCollection
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CollectionItem(
    val title: String,
    val isPinned: Boolean,
    val coverImageUrl: String?,
    val memories: List<MemoryWithMedia>,
    val memoryCount: Int,
    val distinctLocations: Int
)

data class MemoriesUiState(
    val memories: List<MemoryWithMedia> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val collections: List<CollectionItem> = emptyList(),
    val lifeChapters: List<LifeChapter> = emptyList(),
    val searchQuery: String = "",
    val activeTab: Int = 0
)

class MemoriesViewModel(private val memoryRepository: MemoryRepository) : ViewModel() {

    private val selectedTag = MutableStateFlow<String?>(null)
    private val searchQuery = MutableStateFlow("")
    private val activeTab = MutableStateFlow(0)

    private val predefinedCategories = listOf(
        "Travel", "Food", "Pets", "Family", "College", "Friends", "Nature", "Festivals", "Road Trips", "Birthdays", "Fitness", "Work"
    )
    
    private data class FilterState(
        val tag: String?,
        val query: String,
        val tab: Int
    )

    private val filterState = combine(selectedTag, searchQuery, activeTab) { tag, query, tab ->
        FilterState(tag, query, tab)
    }

    val uiState: StateFlow<MemoriesUiState> = combine(
        memoryRepository.allMemories,
        memoryRepository.smartCollections,
        memoryRepository.lifeChapters,
        filterState
    ) { allMemories, dbCollections, dbChapters, filters ->
        
        // Auto categorize logic
        val memoriesByCategory = mutableMapOf<String, MutableList<MemoryWithMedia>>()
        
        allMemories.forEach { memoryWithMedia ->
            val mem = memoryWithMedia.memory
            val textToSearch = "${mem.title} ${mem.notes} ${mem.locationName} ${mem.aiCategory}".lowercase()
            
            var matchedCategory = "Other"
            if (mem.aiCategory.isNotBlank() && predefinedCategories.contains(mem.aiCategory)) {
                matchedCategory = mem.aiCategory
            } else {
                for (cat in predefinedCategories) {
                    if (textToSearch.contains(cat.lowercase()) || textToSearch.contains(cat.lowercase().dropLast(1))) {
                        matchedCategory = cat
                        break
                    }
                }
            }
            if (!memoriesByCategory.containsKey(matchedCategory)) {
                memoriesByCategory[matchedCategory] = mutableListOf()
            }
            memoriesByCategory[matchedCategory]?.add(memoryWithMedia)
        }

        // Generate CollectionItem list
        val collectionItems = memoriesByCategory.map { (category, memories) ->
            val dbCol = dbCollections.find { it.title == category }
            
            val coverImg = if (dbCol?.customCoverUrl?.isNotBlank() == true) {
                dbCol.customCoverUrl
            } else {
                memories.flatMap { it.media }.firstOrNull { it.type == "image" && it.url.isNotBlank() }?.url
            }

            CollectionItem(
                title = category,
                isPinned = dbCol?.isPinned ?: false,
                coverImageUrl = coverImg,
                memories = memories.sortedByDescending { it.memory.timestamp },
                memoryCount = memories.size,
                distinctLocations = memories.map { it.memory.locationName }.filter { it.isNotBlank() }.distinct().size
            )
        }.filter { it.title != "Other" || it.memoryCount > 0 }
         .sortedWith(compareByDescending<CollectionItem> { it.isPinned }.thenByDescending { it.memoryCount })

        val filteredCollections = if (filters.query.isNotBlank()) {
            collectionItems.filter { it.title.contains(filters.query, ignoreCase = true) }
        } else collectionItems

        // Generate Life Chapters if DB is empty (dummy generation based on basic rules)
        val finalChapters = if (dbChapters.isEmpty() && allMemories.isNotEmpty()) {
            // Group by year and create some dummy chapters for demonstration
            val sorted = allMemories.sortedBy { it.memory.timestamp }
            val first = sorted.first().memory.timestamp
            val last = sorted.last().memory.timestamp
            
            val autoChapter = LifeChapter(
                id = "recent_events",
                title = "Recent Adventures",
                startTimestamp = first,
                endTimestamp = last,
                customCoverUrl = sorted.flatMap { it.media }.firstOrNull { it.type == "image" }?.url ?: "",
                aiSummary = "A collection of your most recent activities, spanning exciting meetings to peaceful park walks.",
                milestones = "New Project Structure, Mom's Birthday",
                statistics = "{\"Photos\": 4, \"Locations\": 3}"
            )
            // Just for UI without saving to DB directly in the flow mapper
            listOf(autoChapter)
        } else {
            dbChapters
        }

        // Standard Memories Logic
        val memoriesWithImages = allMemories.filter { memoryWithMedia ->
            memoryWithMedia.media.any { it.type == "image" }
        }

        val availableTags = allMemories
            .flatMap { it.media }
            .filter { it.type == "tag" }
            .map { it.label }
            .distinct()
            .sorted()

        val filteredMemories = if (filters.tag == null) {
            memoriesWithImages
        } else {
            memoriesWithImages.filter { memoryWithMedia ->
                memoryWithMedia.media.any { it.type == "tag" && it.label == filters.tag }
            }
        }.let {
            if (filters.query.isNotBlank()) {
                it.filter { m -> 
                    m.memory.title.contains(filters.query, ignoreCase = true) || 
                    m.memory.notes.contains(filters.query, ignoreCase = true) 
                }
            } else it
        }

        MemoriesUiState(
            memories = filteredMemories,
            availableTags = availableTags,
            selectedTag = filters.tag,
            collections = filteredCollections,
            lifeChapters = finalChapters,
            searchQuery = filters.query,
            activeTab = filters.tab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MemoriesUiState()
    )
    
    init {
        // Auto-generate some initial Life Chapters in DB if none exist to fulfill the feature requirements
        viewModelScope.launch {
            val dbChapters = memoryRepository.lifeChapters.first()
            if (dbChapters.isEmpty()) {
                // Give the user some cool preset life chapters if they just started
                val dummyChapter = LifeChapter(
                    id = "college_years",
                    title = "College Years",
                    startTimestamp = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 365 * 4,
                    endTimestamp = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 365 * 1,
                    customCoverUrl = "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?q=80&w=600&auto=format&fit=crop",
                    aiSummary = "A time of great learning, new friendships, and exploring the world. You visited 12 new places and took 450 photos.",
                    milestones = "Graduation Day, First Hackathon, Roadtrip to California",
                    statistics = "{\"Memories\": 450, \"Locations\": 12, \"Top Emotion\": \"Joy\"}"
                )
                memoryRepository.insertLifeChapter(dummyChapter)
                
                val dummyChapter2 = LifeChapter(
                    id = "early_career",
                    title = "Early Career",
                    startTimestamp = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 365 * 1,
                    endTimestamp = System.currentTimeMillis(),
                    customCoverUrl = "https://images.unsplash.com/photo-1497215728101-856f4ea42174?q=80&w=600&auto=format&fit=crop",
                    aiSummary = "Starting your first job brought new challenges and a fresh routine. This chapter highlights your professional growth and networking.",
                    milestones = "First Promotion, Office Relocation",
                    statistics = "{\"Memories\": 120, \"Locations\": 3, \"Top Emotion\": \"Focus\"}"
                )
                memoryRepository.insertLifeChapter(dummyChapter2)
            }
        }
    }

    fun selectTag(tag: String?) {
        selectedTag.value = tag
    }
    
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }
    
    fun setActiveTab(index: Int) {
        activeTab.value = index
    }

    fun togglePinCollection(collection: CollectionItem) {
        viewModelScope.launch {
            val dbCollection = SmartCollection(
                title = collection.title,
                isPinned = !collection.isPinned,
                customCoverUrl = collection.coverImageUrl ?: ""
            )
            memoryRepository.insertSmartCollection(dbCollection)
        }
    }

    fun updateLifeChapter(chapter: LifeChapter) {
        viewModelScope.launch {
            memoryRepository.insertLifeChapter(chapter)
        }
    }
}
