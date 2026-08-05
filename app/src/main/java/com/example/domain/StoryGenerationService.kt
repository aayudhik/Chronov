package com.example.domain

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.local.MemoryWithMedia
import com.example.data.local.Story
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoryGenerationService {

    suspend fun generateStory(
        memories: List<MemoryWithMedia>,
        type: String, // "Daily recap", "Weekly recap", "Monthly recap", "Year in review", "Travel summary", "Festival summary", "Birthday summary"
        title: String,
        startTime: Long,
        endTime: Long
    ): Story? = withContext(Dispatchers.IO) {
        if (memories.isEmpty()) return@withContext null

        val prompt = buildPrompt(memories, type, title)
        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are an expert storyteller writing in a natural, personal, and nostalgic tone. Summarize the user's memories into a beautiful flowing narrative. Format the output with clean markdown. Do not include introductory text, just output the story."))
            )
        )
        
        try {
            val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
            val generatedContent = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return@withContext null

            // Find a cover image from the memories
            val coverImage = memories.flatMap { it.media }.firstOrNull { it.type == "image" }?.url ?: ""

            Story(
                timestamp = System.currentTimeMillis(),
                type = type,
                title = title,
                content = generatedContent.trim(),
                coverImageUrl = coverImage,
                timeRangeStart = startTime,
                timeRangeEnd = endTime
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildPrompt(memories: List<MemoryWithMedia>, type: String, title: String): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("Generate a '$type' story titled '$title' based on these journal entries and memories:\n\n")
        
        memories.forEach { mem ->
            val dateStr = dateFormat.format(Date(mem.memory.timestamp))
            sb.append("Date: $dateStr\n")
            sb.append("Title: ${mem.memory.title}\n")
            if (mem.memory.locationName.isNotBlank()) sb.append("Location: ${mem.memory.locationName}\n")
            if (mem.memory.sentiment.isNotBlank()) sb.append("Mood: ${mem.memory.sentiment}\n")
            if (mem.memory.notes.isNotBlank()) sb.append("Notes: ${mem.memory.notes}\n")
            val tags = mem.media.filter { it.type == "tag" }.map { it.label }
            if (tags.isNotEmpty()) sb.append("Tags/People: ${tags.joinToString(", ")}\n")
            sb.append("\n---\n\n")
        }
        
        sb.append("Create a cohesive, emotional narrative summarizing these events. Make it read like a beautiful chapter of a memoir.")
        return sb.toString()
    }
}
