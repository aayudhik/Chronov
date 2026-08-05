import re

with open("app/src/main/java/com/example/ChronovaApplication.kt", "r") as f:
    content = f.read()

imports = """import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.OnThisDayWorker
import java.util.concurrent.TimeUnit
"""
content = content.replace("import com.example.di.AppContainer", "import com.example.di.AppContainer\n" + imports)

work_setup = """        container = AppContainer(this)
        
        val workRequest = PeriodicWorkRequestBuilder<OnThisDayWorker>(24, TimeUnit.HOURS)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "OnThisDayNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
"""
content = content.replace("        container = AppContainer(this)", work_setup)

with open("app/src/main/java/com/example/ChronovaApplication.kt", "w") as f:
    f.write(content)
