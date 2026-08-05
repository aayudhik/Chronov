package com.example.data.ai

import com.example.data.local.Memory
import com.example.data.local.MemoryWithMedia
import com.example.data.repository.MemoryRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import android.util.Log

@Serializable
data class AIMemoryAnalysis(
    val aiSummary: String = "",
    val aiTitleSuggestion: String = "",
    val aiEmotionDetection: String = "",
    val aiActivityDetection: String = "",
    val aiPlaceDetection: String = "",
    val aiWeatherSummary: String = "",
    val aiImportanceScore: Int = 0,
    val aiCategory: String = "",
    val aiStory: String = "",
    val suggestedTags: List<String> = emptyList()
)

class AIMemoryEngine(private val memoryRepository: MemoryRepository) {

    private val generativeModel = Firebase.ai.generativeModel(
        modelName = "gemini-2.5-flash",
        generationConfig = generationConfig {
            temperature = 0.4f
            responseMimeType = "application/json"
        }
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeMemory(memoryId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val memoryWithMedia = memoryRepository.getMemoryWithMedia(memoryId).firstOrNull()
                if (memoryWithMedia == null) return@withContext

                val memory = memoryWithMedia.memory
                val media = memoryWithMedia.media

                val prompt = """
                    Analyze the following memory and provide detailed insights.
                    Title: ${memory.title}
                    Notes: ${memory.notes}
                    Location: ${memory.locationName}
                    Sentiment: ${memory.sentiment}
                    Temperature: ${memory.temperature}
                    
                    Return a JSON object with the following fields:
                    - aiSummary (string): A short summary of the memory.
                    - aiTitleSuggestion (string): A better title for this memory.
                    - aiEmotionDetection (string): The detected emotion.
                    - aiActivityDetection (string): What activity is happening.
                    - aiPlaceDetection (string): What kind of place this is.
                    - aiWeatherSummary (string): A summary of the weather.
                    - aiImportanceScore (number): Score from 0 to 100 based on emotional impact or uniqueness.
                    - aiCategory (string): A broad category (e.g., Travel, Family, Work).
                    - aiStory (string): A beautifully written 2-3 sentence story about this memory.
                    - suggestedTags (array of strings): 3-5 relevant tags.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val text = response.text
                if (text != null) {
                    val analysis = json.decodeFromString<AIMemoryAnalysis>(text)
                    
                    val updatedMemory = memory.copy(
                        aiSummary = analysis.aiSummary,
                        aiTitleSuggestion = analysis.aiTitleSuggestion,
                        aiEmotionDetection = analysis.aiEmotionDetection,
                        aiActivityDetection = analysis.aiActivityDetection,
                        aiPlaceDetection = analysis.aiPlaceDetection,
                        aiWeatherSummary = analysis.aiWeatherSummary,
                        aiImportanceScore = analysis.aiImportanceScore,
                        aiCategory = analysis.aiCategory,
                        aiStory = analysis.aiStory
                    )
                    
                    memoryRepository.updateMemory(updatedMemory)
                    
                    // We also need to add the suggested tags to the media table if they don't exist
                    val existingTags = media.filter { it.type == "tag" }.map { it.label }
                    val newTags = analysis.suggestedTags.filter { !existingTags.contains(it) }
                    
                    if (newTags.isNotEmpty()) {
                        val newMedia = newTags.map { 
                            com.example.data.local.Media(memoryId = memoryId, type = "tag", label = it) 
                        }
                        memoryRepository.insertMediaOnly(newMedia)
                    }
                }

            } catch (e: Exception) {
                Log.e("AIMemoryEngine", "Error analyzing memory", e)
            }
        }
    }
}
