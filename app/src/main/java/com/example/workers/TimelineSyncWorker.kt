package com.example.workers

import android.content.Context
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ChronovaApplication
import com.example.data.local.Media
import com.example.data.local.Memory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class TimelineSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val appContainer = (applicationContext as ChronovaApplication).container
        val repository = appContainer.memoryRepository

        // Try reading MediaStore for recent photos (e.g. from the last 7 days)
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA
        )
        val sevenDaysAgo = (System.currentTimeMillis() / 1000) - (7 * 24 * 60 * 60)
        val selection = "${MediaStore.Images.Media.DATE_ADDED} >= ?"
        val selectionArgs = arrayOf(sevenDaysAgo.toString())
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        try {
            applicationContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                // Group by day
                val photosByDay = mutableMapOf<Long, MutableList<String>>()

                while (cursor.moveToNext()) {
                    val dateAdded = cursor.getLong(dateAddedColumn) * 1000
                    val data = cursor.getString(dataColumn)

                    // Normalize to start of day
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = dateAdded
                        set(Calendar.HOUR_OF_DAY, 12) // set to noon to be safe
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val dayStart = cal.timeInMillis

                    if (!photosByDay.containsKey(dayStart)) {
                        photosByDay[dayStart] = mutableListOf()
                    }
                    photosByDay[dayStart]?.add(data)
                }

                // Create a draft memory for each day that has photos
                for ((dayMs, photos) in photosByDay) {
                    val memory = Memory(
                        timestamp = dayMs,
                        title = "Auto-Generated Timeline",
                        notes = "Found ${photos.size} photos from this day.",
                        isDraft = true,
                        source = "Photos",
                        confidenceScore = 85
                    )
                    
                    val mediaList = photos.take(5).map { path ->
                        Media(
                            memoryId = 0,
                            type = "image",
                            url = path
                        )
                    }

                    repository.insertMemoryWithMedia(memory, mediaList)
                }
            }
        } catch (e: SecurityException) {
            // Permission denied
        } catch (e: Exception) {
            // Other error
        }

        Result.success()
    }
}
