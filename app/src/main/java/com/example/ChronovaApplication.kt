package com.example

import android.app.Application
import com.example.di.AppContainer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.OnThisDayWorker
import java.util.concurrent.TimeUnit


class ChronovaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        
        val workRequest = PeriodicWorkRequestBuilder<OnThisDayWorker>(24, TimeUnit.HOURS)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "OnThisDayNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

    }
}
