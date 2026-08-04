package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class MemoryWithMedia(
    @Embedded val memory: Memory,
    @Relation(
        parentColumn = "id",
        entityColumn = "memoryId"
    )
    val media: List<Media>
)
