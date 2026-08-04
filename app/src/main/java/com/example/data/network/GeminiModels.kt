package com.example.data.network

import kotlinx.serialization.Serializable

@Serializable
data class Part(
    val text: String
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val topK: Int? = null,
    val topP: Float? = null,
    val candidateCount: Int? = null,
    val maxOutputTokens: Int? = null
)

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)
