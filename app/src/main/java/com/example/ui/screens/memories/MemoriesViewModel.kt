package com.example.ui.screens.memories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryWithMedia
import com.example.data.local.SmartCollection
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    val uiState: StateFlow<MemoriesUiState> = combine(
        memoryRepository.allMemories,
        memoryRepository.smartCollections,
        selectedTag,
        searchQuery,
        activeTab
    ) { allMemories, dbCollections, selected, query, tab ->
        
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

        val filteredCollections = if (query.isNotBlank()) {
            collectionItems.filter { it.title.contains(query, ignoreCase = true) }
        } else collectionItems

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

        val filteredMemories = if (selected == null) {
            memoriesWithImages
        } else {
            memoriesWithImages.filter { memoryWithMedia ->
                memoryWithMedia.media.any { it.type == "tag" && it.label == selected }
            }
        }.let {
            if (query.isNotBlank()) {
                it.filter { m -> 
                    m.memory.title.contains(query, ignoreCase = true) || 
                    m.memory.notes.contains(query, ignoreCase = true) 
                }
            } else it
        }

        MemoriesUiState(
            memories = filteredMemories,
            availableTags = availableTags,
            selectedTag = selected,
            collections = filteredCollections,
            searchQuery = query,
            activeTab = tab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MemoriesUiState()
    )

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
}
