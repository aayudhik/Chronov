package com.example.domain

import com.example.data.local.MemoryWithMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class MemoryScores(
    val memoryId: Long,
    val memoryTitle: String,
    val memoryDate: Long,
    val coverImage: String?,
    val importanceScore: Int,
    val emotionalScore: Int,
    val happinessScore: Int,
    val confidenceScore: Int,
    val activityScore: Int,
    val socialScore: Int,
    val travelScore: Int,
    val photographyScore: Int,
    val overallScore: Int
)

class MemoryIntelligenceService {

    suspend fun analyzeMemories(memories: List<MemoryWithMedia>): List<MemoryScores> = withContext(Dispatchers.Default) {
        memories.map { analyzeMemory(it) }.sortedByDescending { it.overallScore }
    }

    private fun analyzeMemory(memoryWithMedia: MemoryWithMedia): MemoryScores {
        val memory = memoryWithMedia.memory
        val media = memoryWithMedia.media
        
        val text = "${memory.title} ${memory.notes} ${memory.aiSummary} ${memory.aiEmotionDetection} ${memory.aiActivityDetection}".lowercase(Locale.getDefault())

        // Photography Score
        val images = media.filter { it.type == "image" }
        val photographyScore = ((images.size * 20) + (if (memory.isHero) 30 else 0)).coerceIn(10, 100)
        
        // Social Score
        val tags = media.filter { it.type == "tag" }
        val socialWords = listOf("friend", "family", "together", "we", "us", "party", "people", "meet", "crowd", "wedding")
        val socialWordCount = socialWords.count { text.contains(it) }
        val socialScore = ((tags.size * 15) + (socialWordCount * 10)).coerceIn(5, 100)

        // Travel Score
        val travelWords = listOf("travel", "trip", "flight", "visit", "vacation", "journey", "hotel", "beach", "mountain", "abroad", "tour")
        val travelWordCount = travelWords.count { text.contains(it) }
        val travelScore = (if (memory.locationName.isNotBlank() && memory.locationName.length > 3) 40 else 10) + (travelWordCount * 15).coerceIn(0, 100)

        // Activity Score
        val activityWords = listOf("hike", "walk", "run", "play", "dance", "work", "sport", "gym", "swim", "explore")
        val activityWordCount = activityWords.count { text.contains(it) }
        val activityScore = ((activityWordCount * 20) + (if (text.length > 100) 20 else 0)).coerceIn(10, 100)

        // Happiness Score
        val happyWords = listOf("happy", "joy", "smile", "love", "great", "amazing", "wonderful", "beautiful", "fun", "glad", "laugh")
        val happyWordCount = happyWords.count { text.contains(it) }
        val happinessScore = ((happyWordCount * 15) + (if (memory.sentiment == "Joy" || memory.sentiment == "Awe") 40 else 0)).coerceIn(10, 100)

        // Emotional Score
        val emotionWords = listOf("cry", "sad", "angry", "fear", "anxious", "overwhelmed", "love", "hate", "passion", "deep", "feel")
        val emotionWordCount = emotionWords.count { text.contains(it) }
        val emotionalScore = ((emotionWordCount * 15) + (happyWordCount * 5) + (if (memory.sentiment.isNotBlank()) 20 else 0)).coerceIn(10, 100)

        // Confidence Score
        val confidenceScore = memory.confidenceScore.coerceIn(20, 100)

        // Importance Score
        val importanceScore = if (memory.aiImportanceScore > 0) memory.aiImportanceScore else {
            ((text.length / 5) + (images.size * 10) + (if (memory.isHero) 20 else 0)).coerceIn(10, 100)
        }

        // Overall
        val overallScore = (importanceScore + emotionalScore + happinessScore + activityScore + socialScore + travelScore + photographyScore) / 7

        return MemoryScores(
            memoryId = memory.id,
            memoryTitle = memory.title.ifBlank { "Untitled Memory" },
            memoryDate = memory.timestamp,
            coverImage = images.firstOrNull()?.url,
            importanceScore = importanceScore.coerceIn(0, 100),
            emotionalScore = emotionalScore.coerceIn(0, 100),
            happinessScore = happinessScore.coerceIn(0, 100),
            confidenceScore = confidenceScore.coerceIn(0, 100),
            activityScore = activityScore.coerceIn(0, 100),
            socialScore = socialScore.coerceIn(0, 100),
            travelScore = travelScore.coerceIn(0, 100),
            photographyScore = photographyScore.coerceIn(0, 100),
            overallScore = overallScore.coerceIn(0, 100)
        )
    }
}
