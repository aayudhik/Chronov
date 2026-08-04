package com.example.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.MemoryWithMedia
import com.example.data.network.*
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed class AISearchUiState {
    object Idle : AISearchUiState()
    object Loading : AISearchUiState()
    data class Success(val summary: String, val matchedMemories: List<MemoryWithMedia>) : AISearchUiState()
    data class Error(val message: String) : AISearchUiState()
}

class AISearchViewModel(private val repository: MemoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AISearchUiState>(AISearchUiState.Idle)
    val uiState: StateFlow<AISearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _uiState.value = AISearchUiState.Idle
        }
    }

    fun performSearch() {
        val query = _searchQuery.value
        if (query.isBlank()) return

        _uiState.value = AISearchUiState.Loading

        viewModelScope.launch {
            try {
                // Fetch all memories (in a real app, maybe only fetch recent or use embeddings)
                val allMemories = repository.allMemories.first()
                
                val memoriesContext = allMemories.joinToString("\n") { memoryWithMedia ->
                    val mem = memoryWithMedia.memory
                    val tags = memoryWithMedia.media.filter { it.type == "tag" }.joinToString { it.label }
                    "ID: ${mem.id}, Title: ${mem.title}, Notes: ${mem.notes}, Date: ${mem.timestamp}, Location: ${mem.locationName.ifEmpty { "Unknown" }}, Tags: $tags"
                }

                val prompt = """
                    You are an AI assistant helping a user search their personal memories.
                    User Query: "$query"
                    
                    Here are the user's memories:
                    $memoriesContext
                    
                    Instructions:
                    1. Find the memories that best match the query.
                    2. First, output a JSON array of the matching memory IDs on a single line, exactly like this: [1, 5, 8]
                    3. Then, on a new line, provide a helpful and friendly summary answering the user's query based on the matching memories.
                    
                    If no memories match, output [] followed by a friendly message saying you couldn't find any matching memories.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(temperature = 0.2f)
                )

                val apiKey = BuildConfig.GEMINI_API_KEY
                
                // Using streaming to get the response
                var fullText = ""
                var parsedIds: List<Long>? = null
                var matchedMemories = emptyList<MemoryWithMedia>()
                var summaryText = ""

                val response = RetrofitClient.service.generateContentStream(apiKey, request)
                response.byteStream().bufferedReader().use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        try {
                            if (line!!.startsWith("data: ")) {
                                val jsonStr = line!!.substring(6)
                                val chunk = Json.parseToJsonElement(jsonStr).jsonObject
                                val text = chunk["candidates"]?.jsonArray
                                    ?.getOrNull(0)?.jsonObject
                                    ?.get("content")?.jsonObject
                                    ?.get("parts")?.jsonArray
                                    ?.getOrNull(0)?.jsonObject
                                    ?.get("text")?.jsonPrimitive?.content

                                if (text != null) {
                                    fullText += text
                                    
                                    // Try to parse the IDs if we haven't yet
                                    if (parsedIds == null && fullText.contains("]")) {
                                        val arrayEndIndex = fullText.indexOf("]")
                                        val arrayStartIndex = fullText.indexOf("[")
                                        if (arrayStartIndex != -1 && arrayEndIndex > arrayStartIndex) {
                                            val arrayStr = fullText.substring(arrayStartIndex, arrayEndIndex + 1)
                                            try {
                                                val ids = Json.parseToJsonElement(arrayStr).jsonArray.map { it.jsonPrimitive.content.toLong() }
                                                parsedIds = ids
                                                matchedMemories = allMemories.filter { it.memory.id in ids }
                                                
                                                // Extract summary text
                                                summaryText = fullText.substring(arrayEndIndex + 1).trimStart()
                                                _uiState.value = AISearchUiState.Success(summaryText, matchedMemories)
                                            } catch (e: Exception) {
                                                // Failed to parse array, might not be complete yet
                                            }
                                        }
                                    } else if (parsedIds != null) {
                                        // We already parsed the IDs, just append to summary
                                        val arrayEndIndex = fullText.indexOf("]")
                                        summaryText = fullText.substring(arrayEndIndex + 1).trimStart()
                                        _uiState.value = AISearchUiState.Success(summaryText, matchedMemories)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Skip parse errors for partial chunks
                        }
                    }
                }
                
                // Fallback if streaming didn't parse correctly or array wasn't found
                if (parsedIds == null) {
                    _uiState.value = AISearchUiState.Success(fullText.trim(), emptyList())
                }

            } catch (e: Exception) {
                _uiState.value = AISearchUiState.Error(e.message ?: "An error occurred during search")
            }
        }
    }
}
