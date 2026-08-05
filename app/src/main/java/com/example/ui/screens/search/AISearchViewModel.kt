package com.example.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MemoryWithMedia
import com.example.data.local.SearchMessage
import com.example.data.repository.MemoryRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class AISearchViewModel(private val repository: MemoryRepository) : ViewModel() {

    val chatMessages = repository.searchMessages.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _allMemories = MutableStateFlow<List<MemoryWithMedia>>(emptyList())
    
    init {
        viewModelScope.launch {
            repository.allMemories.collect {
                _allMemories.value = it
            }
        }
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun performSearch() {
        val query = _searchQuery.value
        if (query.isBlank()) return
        
        _searchQuery.value = ""
        _isSearching.value = true

        viewModelScope.launch {
            try {
                // Add user message
                repository.insertSearchMessage(
                    SearchMessage(
                        timestamp = System.currentTimeMillis(),
                        isUser = true,
                        text = query
                    )
                )

                val allMemories = _allMemories.value
                val previousMessages = chatMessages.value.takeLast(6).joinToString("\n") { 
                    (if (it.isUser) "User: " else "AI: ") + it.text
                }
                
                val memoriesContext = allMemories.joinToString("\n") { memoryWithMedia ->
                    val mem = memoryWithMedia.memory
                    val tags = memoryWithMedia.media.filter { it.type == "tag" }.joinToString { it.label }
                    "ID: ${mem.id}, Title: ${mem.title}, Notes: ${mem.notes}, Date: ${mem.timestamp}, Location: ${mem.locationName.ifEmpty { "Unknown" }}, Tags: $tags, AI Summary: ${mem.aiSummary}, AI Category: ${mem.aiCategory}"
                }

                val promptText = """
                    You are "Ask My Life", an AI assistant helping a user search their personal memories.
                    
                    Conversation History:
                    $previousMessages
                    
                    Current Query: "$query"
                    
                    Here are all the user's memories:
                    $memoriesContext
                    
                    Instructions:
                    1. Find the memories that best match the current query in the context of the conversation.
                    2. First, output a JSON array of the matching memory IDs on a single line, exactly like this: [1, 5, 8]
                    3. Then, on a new line, provide a helpful, friendly, conversational summary answering the user's query based on the matching memories.
                    
                    If no memories match, output [] followed by a friendly message saying you couldn't find any matching memories.
                """.trimIndent()

                val generativeModel = Firebase.ai.generativeModel(
                    modelName = "gemini-2.5-flash",
                    generationConfig = generationConfig {
                        temperature = 0.2f
                    }
                )

                val responseFlow = generativeModel.generateContentStream(promptText)
                
                var fullText = ""
                var parsedIds = ""
                var summaryText = ""
                
                responseFlow.collect { chunk ->
                    val text = chunk.text
                    if (text != null) {
                        fullText += text
                        
                        if (parsedIds.isEmpty() && fullText.contains("]")) {
                            val arrayEndIndex = fullText.indexOf("]")
                            val arrayStartIndex = fullText.indexOf("[")
                            if (arrayStartIndex != -1 && arrayEndIndex > arrayStartIndex) {
                                val arrayStr = fullText.substring(arrayStartIndex, arrayEndIndex + 1)
                                try {
                                    val ids = Json.parseToJsonElement(arrayStr).jsonArray.map { it.jsonPrimitive.content }
                                    parsedIds = ids.joinToString(",")
                                } catch (e: Exception) {
                                    // wait for complete array
                                }
                            }
                        }
                    }
                }
                
                if (parsedIds.isNotEmpty()) {
                    val arrayEndIndex = fullText.indexOf("]")
                    summaryText = fullText.substring(arrayEndIndex + 1).trimStart()
                } else {
                    summaryText = fullText.trim()
                }

                repository.insertSearchMessage(
                    SearchMessage(
                        timestamp = System.currentTimeMillis(),
                        isUser = false,
                        text = summaryText,
                        matchedMemoryIds = parsedIds
                    )
                )

            } catch (e: Exception) {
                repository.insertSearchMessage(
                    SearchMessage(
                        timestamp = System.currentTimeMillis(),
                        isUser = false,
                        text = "Sorry, an error occurred: ${e.message}"
                    )
                )
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    fun getMemoriesByIds(idsString: String): List<MemoryWithMedia> {
        if (idsString.isEmpty()) return emptyList()
        val ids = idsString.split(",").mapNotNull { it.toLongOrNull() }
        return _allMemories.value.filter { it.memory.id in ids }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }
}
