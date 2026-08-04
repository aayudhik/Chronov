package com.example.data.repository

import com.example.data.local.ChronovaDatabase
import com.example.data.local.Media
import com.example.data.local.Memory
import com.example.data.local.MemoryWithMedia
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class MemoryRepository(private val database: ChronovaDatabase) {
    val allMemories: Flow<List<MemoryWithMedia>> = database.memoryDao().getAllMemoriesWithMedia()

    suspend fun insertMemoryWithMedia(memory: Memory, media: List<Media>) {
        val memoryId = database.memoryDao().insertMemory(memory)
        val mediaWithMemoryId = media.map { it.copy(memoryId = memoryId) }
        database.memoryDao().insertMedia(mediaWithMemoryId)
    }

    suspend fun populateInitialDataIfNeeded() {
        if (database.memoryDao().getMemoryCount() == 0) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            // Hero Memory
            insertMemoryWithMedia(
                Memory(
                    timestamp = now,
                    title = "Today, Oct 24",
                    isHero = true,
                    notes = "A quiet morning reflecting on past adventures. The AI suggests revisiting your trip to Kyoto.",
                    temperature = "72° Clear"
                ),
                listOf(
                    Media(
                        memoryId = 0,
                        type = "image",
                        url = "https://lh3.googleusercontent.com/aida-public/AB6AXuBQpKkmVIXb-bYySbq1T819_zkH-QIroENM3e5_YmQQQocx9Z6IUEUby9ysFVTkFFdCNuWJYbi9GMTTlbeDlufVsvLtuAo4jo4v-AF7nXTJ_8KYqRFNhOsdRIkBU98btcvL4yQ6mVHaU3O8feAcsm_xEl3CLlWOYgs13ZS9u-hkfGhcLYI0ASmKuBu_4J8NRqIlDN9dwddDhvo0ZILFvwvnZ2lL0pUU1kNfa2FJ0h7tnn0uGDE1nS7e"
                    )
                )
            )

            // Yesterday
            insertMemoryWithMedia(
                Memory(
                    timestamp = now - dayMs,
                    title = "Yesterday",
                    locationName = "Downtown Cafe",
                    sentiment = "Joyful",
                    score = 92,
                    notes = "Productive afternoon session. Reconnected with Sarah over coffee and brainstormed the new project structure. Felt incredibly energized afterwards."
                ),
                listOf(
                    Media(
                        memoryId = 0,
                        type = "image",
                        url = "https://lh3.googleusercontent.com/aida-public/AB6AXuBu7JIsQuZQgANYL4IH26Iu9S4i2RAI5aMvplX3uTD0kH-50wBgXFfOUzHkVRhFwmp91Ht9cVWfMsrA5Xh1wNRS40Eq8tzz9F9FN3D3nzSzQqJvPl5ByR32I8mDpX3WHRa0Itzx9Ju7Dj83Zpv12BQK8cCYWASOb6VK8EzNN_AkUFam95Mg8DXXMz5Us7evOLUZbSuInIUywXLYCEzg_-KAreYtWUH7u7pu7UTSpN_KzWCN3iPt6dGS"
                    ),
                    Media(memoryId = 0, type = "tag", label = "2m Audio"),
                    Media(memoryId = 0, type = "tag", label = "Meeting")
                )
            )

            // Oct 22
            insertMemoryWithMedia(
                Memory(
                    timestamp = now - 2 * dayMs,
                    title = "Oct 22",
                    locationName = "City Park",
                    sentiment = "Peaceful",
                    notes = "Took a long walk to clear my head. The autumn leaves are just starting to turn. Listened to a new podcast and found some much-needed clarity on the upcoming changes."
                ),
                listOf(
                    Media(
                        memoryId = 0,
                        type = "image",
                        url = "https://lh3.googleusercontent.com/aida-public/AB6AXuACl4DQrv1t9OH8vXxZYp0KkaYPjDsmeCjUdYfw-dodV1DWAAmCHYZ17elfQ_btCpz8EuvAZJcnKfkFdTnBPJTPSnL1e1FAw-a571h_GGdGxt-bCUQ8KEc--VdqYpvgh551Y7AY8tUZ63BeFojz_DTLY8LnSUbkDWZsJNJriwDU31-8HmtgI5RUzEFWZ2by6LHMpgvpBb2xDs8UQBgFXHcmUtIPp_zrwWVgMarV-WDgqfsGk2TL6ZG_"
                    )
                )
            )

            // Oct 20
            insertMemoryWithMedia(
                Memory(
                    timestamp = now - 4 * dayMs,
                    title = "Oct 20",
                    locationName = "Luigi's Italian",
                    sentiment = "Excited",
                    notes = "Mom's birthday dinner! The food was incredible, but the stories shared were even better. AI highlighted this as a significant social bonding moment for the month."
                ),
                listOf(
                    Media(
                        memoryId = 0,
                        type = "image",
                        url = "https://lh3.googleusercontent.com/aida-public/AB6AXuATm0q-GUTp_djBoEPayz5yhrXJO0t5IAj0xU3UpnrRlL4q2CZW7UMywx3H0B_ZApKAvS0GMNusltTBz9l-c7v-0773xNQLqvC4Yxl-0z6LPF1PXbWMcmJFgkUfKPlTslcw6LWPeU6bmJSPJsApQ_6ceu25JBI3GcFpZBc0u2mYNtv8Pp8bF8fycXEEMWBGqZLYrcsNTk0SHK6Dnnub2mdj5lJtK5mOYZw4hE_Jk1qkoQA_tZ58nvWj"
                    ),
                    Media(
                        memoryId = 0,
                        type = "image",
                        url = "https://lh3.googleusercontent.com/aida-public/AB6AXuCHdAqJ6EGLY8mNs8FXh_6Ugc2hkkeccbMORTTNUWahRUgW47jCGdBHgKAMWlttgskywsMa5HuIRGaXHQMtMxvCb998Sqi8yi1nwx0zPrhxHepUqXgxk1fcpZvzIEoVGtIBRsDS3dqob_X8At8CjqRQbdS8-_lzKYZcThtdI__7QTj_eqk-JxibpgvGTpwH17Em8JmUo1vuc4xBKcvZKkBNgeXfwji2GQeXJ2AjOIanM9m_xEfaejr-"
                    )
                )
            )
        }
    }
}
