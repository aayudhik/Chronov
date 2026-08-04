package com.example.data.network

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface GeminiApiService {
    @Streaming
    @POST("v1beta/models/gemini-2.5-flash:streamGenerateContent?alt=sse")
    suspend fun generateContentStream(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): ResponseBody
}
